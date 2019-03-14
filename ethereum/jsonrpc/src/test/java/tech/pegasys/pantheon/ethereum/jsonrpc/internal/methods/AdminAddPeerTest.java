/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.crypto.SECP256K1.PublicKey;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.parameters.JsonRpcParameter;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcError;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcErrorResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.p2p.P2pDisabledException;
import tech.pegasys.pantheon.ethereum.p2p.PeerNotWhitelistedException;
import tech.pegasys.pantheon.ethereum.p2p.api.P2PNetwork;
import tech.pegasys.pantheon.ethereum.p2p.peers.cache.PeerCache;
import tech.pegasys.pantheon.util.enode.EnodeURL;

import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class AdminAddPeerTest {

  @Mock private P2PNetwork p2pNetwork;
  @Mock private PeerCache peerCache;
  private final JsonRpcParameter parameter = new JsonRpcParameter();

  private AdminAddPeer method;

  private final EnodeURL validEnodeURL =
      new EnodeURL(
          PublicKey.create(BigInteger.valueOf(0)).toString().substring(2), "127.0.0.1", 30303);

  private final JsonRpcRequest validRequest =
      new JsonRpcRequest("2.0", "admin_addPeer", new String[] {validEnodeURL.toString()});

  @Before
  public void setup() {

    method = new AdminAddPeer(p2pNetwork, parameter, peerCache);
    when(peerCache.add(any())).thenReturn(true);
  }

  @Test
  public void requestIsMissingParameter() {
    final JsonRpcRequest request = new JsonRpcRequest("2.0", "admin_addPeer", new String[] {});
    final JsonRpcResponse expectedResponse =
        new JsonRpcErrorResponse(request.getId(), JsonRpcError.INVALID_PARAMS);

    final JsonRpcResponse actualResponse = method.response(request);

    assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse);
  }

  @Test
  public void requestHasNullObjectParameter() {
    final JsonRpcRequest request = new JsonRpcRequest("2.0", "admin_addPeer", null);
    final JsonRpcResponse expectedResponse =
        new JsonRpcErrorResponse(request.getId(), JsonRpcError.INVALID_PARAMS);

    final JsonRpcResponse actualResponse = method.response(request);

    assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse);
  }

  @Test
  public void requestHasNullArrayParameter() {
    final JsonRpcRequest request = new JsonRpcRequest("2.0", "admin_addPeer", new String[] {null});
    final JsonRpcResponse expectedResponse =
        new JsonRpcErrorResponse(request.getId(), JsonRpcError.INVALID_PARAMS);

    final JsonRpcResponse actualResponse = method.response(request);

    assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse);
  }

  @Test
  public void requestHasInvalidEnode() {
    final JsonRpcRequest request =
        new JsonRpcRequest("2.0", "admin_addPeer", new String[] {"asdf"});
    final JsonRpcResponse expectedResponse =
        new JsonRpcErrorResponse(request.getId(), JsonRpcError.PARSE_ERROR);

    final JsonRpcResponse actualResponse = method.response(request);

    assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse);
  }

  @Test
  public void requestAddsValidEnode() {
    when(p2pNetwork.addMaintainConnectionPeer(any())).thenReturn(true);

    final JsonRpcResponse expectedResponse = new JsonRpcSuccessResponse(validRequest.getId(), true);

    final JsonRpcResponse actualResponse = method.response(validRequest);

    assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse);
    verify(peerCache).add(eq(validEnodeURL));
  }

  @Test
  public void ifPeerIsNotAddedToCacheFalseIsReturned() {
    when(p2pNetwork.addMaintainConnectionPeer(any())).thenReturn(true);
    when(peerCache.add(any())).thenReturn(false);

    final JsonRpcRequest request =
        new JsonRpcRequest("2.0", "admin_addPeer", new String[] {validEnodeURL.toString()});

    final JsonRpcResponse expectedResponse = new JsonRpcSuccessResponse(request.getId(), false);

    final JsonRpcResponse actualResponse = method.response(request);

    assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse);
    verify(peerCache).add(eq(validEnodeURL));
  }

  @Test
  public void requestRefusesListOfNodes() {

    final JsonRpcRequest request =
        new JsonRpcRequest(
            "2.0",
            "admin_addPeer",
            new String[] {validEnodeURL.toString(), validEnodeURL.toString()});

    final JsonRpcResponse expectedResponse =
        new JsonRpcErrorResponse(request.getId(), JsonRpcError.INVALID_PARAMS);

    final JsonRpcResponse actualResponse = method.response(request);

    assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse);
  }

  @Test
  public void requestReturnsFalseIfAddFails() {
    when(p2pNetwork.addMaintainConnectionPeer(any())).thenReturn(false);

    final JsonRpcResponse expectedResponse =
        new JsonRpcSuccessResponse(validRequest.getId(), false);

    final JsonRpcResponse actualResponse = method.response(validRequest);

    assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse);
  }

  @Test
  public void requestReturnsErrorWhenP2pDisabled() {
    when(p2pNetwork.addMaintainConnectionPeer(any()))
        .thenThrow(
            new P2pDisabledException("P2P networking disabled.  Unable to connect to add peer."));

    final JsonRpcResponse expectedResponse =
        new JsonRpcErrorResponse(validRequest.getId(), JsonRpcError.P2P_DISABLED);

    final JsonRpcResponse actualResponse = method.response(validRequest);

    assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse);
  }

  @Test
  public void requestReturnsErrorWhenPeerNotWhitelisted() {
    when(p2pNetwork.addMaintainConnectionPeer(any()))
        .thenThrow(new PeerNotWhitelistedException("Cannot add peer that is not whitelisted"));

    final JsonRpcResponse expectedResponse =
        new JsonRpcErrorResponse(
            validRequest.getId(), JsonRpcError.NON_WHITELISTED_NODE_CANNOT_BE_ADDED_AS_A_PEER);

    final JsonRpcResponse actualResponse = method.response(validRequest);

    assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse);
  }
}
