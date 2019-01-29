package tech.pegasys.pantheon.tests.acceptance.dsl.transaction;

import java.util.Collections;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;

public class AdminJsonRpcRequestFactory {

  public static class AdminAddPeerResponse extends Response<Boolean> {}

  private final Web3jService web3jService;

  public AdminJsonRpcRequestFactory(final Web3jService web3jService) {
    this.web3jService = web3jService;
  }

  public Request<?, AdminAddPeerResponse> adminAddPeer(final String enodeAddress) {
    return new Request<>(
        "admin_addPeer",
        Collections.singletonList(enodeAddress),
        web3jService,
        AdminAddPeerResponse.class);
  }
}
