/*
 * Copyright 2018 ConsenSys AG.
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
package tech.pegasys.pantheon.ethereum.eth.manager.task;

import static com.google.common.base.Preconditions.checkArgument;

import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockBody;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.Transaction;
import tech.pegasys.pantheon.ethereum.eth.manager.EthContext;
import tech.pegasys.pantheon.ethereum.eth.manager.EthPeer;
import tech.pegasys.pantheon.ethereum.eth.manager.PendingPeerRequest;
import tech.pegasys.pantheon.ethereum.eth.messages.BlockBodiesMessage;
import tech.pegasys.pantheon.ethereum.eth.messages.EthPV62;
import tech.pegasys.pantheon.ethereum.mainnet.BodyValidation;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.ethereum.p2p.rlpx.wire.MessageData;
import tech.pegasys.pantheon.plugin.services.MetricsSystem;
import tech.pegasys.pantheon.util.bytes.Bytes32;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Requests bodies from a peer by header, matches up headers to bodies, and returns blocks.
 *
 * @param <C> the consensus algorithm context
 */
public class GetBodiesFromPeerTask<C> extends AbstractPeerRequestTask<List<Block>> {
  private static final Logger LOG = LogManager.getLogger();

  private final ProtocolSchedule<C> protocolSchedule;
  private final List<BlockHeader> headers;
  private final Map<BodyIdentifier, List<BlockHeader>> bodyToHeaders = new HashMap<>();

  private GetBodiesFromPeerTask(
      final ProtocolSchedule<C> protocolSchedule,
      final EthContext ethContext,
      final List<BlockHeader> headers,
      final MetricsSystem metricsSystem) {
    super(ethContext, EthPV62.GET_BLOCK_BODIES, metricsSystem);
    checkArgument(headers.size() > 0);
    this.protocolSchedule = protocolSchedule;

    this.headers = headers;
    headers.forEach(
        (header) -> {
          final BodyIdentifier bodyId = new BodyIdentifier(header);
          bodyToHeaders.putIfAbsent(bodyId, new ArrayList<>());
          bodyToHeaders.get(bodyId).add(header);
        });
  }

  public static <C> GetBodiesFromPeerTask<C> forHeaders(
      final ProtocolSchedule<C> protocolSchedule,
      final EthContext ethContext,
      final List<BlockHeader> headers,
      final MetricsSystem metricsSystem) {
    return new GetBodiesFromPeerTask<>(protocolSchedule, ethContext, headers, metricsSystem);
  }

  @Override
  protected PendingPeerRequest sendRequest() {
    final List<Hash> blockHashes =
        headers.stream().map(BlockHeader::getHash).collect(Collectors.toList());
    final long minimumRequiredBlockNumber = headers.get(headers.size() - 1).getNumber();

    return sendRequestToPeer(
        peer -> {
          LOG.debug("Requesting {} bodies from peer {}.", blockHashes.size(), peer);
          return peer.getBodies(blockHashes);
        },
        minimumRequiredBlockNumber);
  }

  @Override
  protected Optional<List<Block>> processResponse(
      final boolean streamClosed, final MessageData message, final EthPeer peer) {
    if (streamClosed) {
      // All outstanding requests have been responded to and we still haven't found the response
      // we wanted. It must have been empty or contain data that didn't match.
      peer.recordUselessResponse("bodies");
      return Optional.of(Collections.emptyList());
    }

    final BlockBodiesMessage bodiesMessage = BlockBodiesMessage.readFrom(message);
    final List<BlockBody> bodies = bodiesMessage.bodies(protocolSchedule);
    if (bodies.size() == 0) {
      // Message contains no data - nothing to do
      return Optional.empty();
    } else if (bodies.size() > headers.size()) {
      // Message doesn't match our request - nothing to do
      return Optional.empty();
    }

    final List<Block> blocks = new ArrayList<>();
    for (final BlockBody body : bodies) {
      final List<BlockHeader> headers = bodyToHeaders.get(new BodyIdentifier(body));
      if (headers == null) {
        // This message contains unrelated bodies - exit
        return Optional.empty();
      }
      headers.forEach(h -> blocks.add(new Block(h, body)));
      // Clear processed headers
      headers.clear();
    }
    return Optional.of(blocks);
  }

  private static class BodyIdentifier {
    private final Bytes32 transactionsRoot;
    private final Bytes32 ommersHash;

    public BodyIdentifier(final Bytes32 transactionsRoot, final Bytes32 ommersHash) {
      this.transactionsRoot = transactionsRoot;
      this.ommersHash = ommersHash;
    }

    public BodyIdentifier(final BlockBody body) {
      this(body.getTransactions(), body.getOmmers());
    }

    public BodyIdentifier(final List<Transaction> transactions, final List<BlockHeader> ommers) {
      this(BodyValidation.transactionsRoot(transactions), BodyValidation.ommersHash(ommers));
    }

    public BodyIdentifier(final BlockHeader header) {
      this(header.getTransactionsRoot(), header.getOmmersHash());
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      final BodyIdentifier that = (BodyIdentifier) o;
      return Objects.equals(transactionsRoot, that.transactionsRoot)
          && Objects.equals(ommersHash, that.ommersHash);
    }

    @Override
    public int hashCode() {
      return Objects.hash(transactionsRoot, ommersHash);
    }
  }
}
