package tech.pegasys.pantheon.tests.acceptance.dsl.transaction;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.util.Arrays;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.PantheonWeb3j.DiscardResponse;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.PantheonWeb3j.ProposalsResponse;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.PantheonWeb3j.ProposeResponse;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.PantheonWeb3j.SignersBlockResponse;

public class CliqueJsonRpcRequestFactory {

  protected final Web3jService web3jService;

  public CliqueJsonRpcRequestFactory(Web3jService web3jService) {
    this.web3jService = web3jService;
  }

  public Request<?, ProposeResponse> cliquePropose(final String address, final Boolean auth) {
    return new Request<>(
        "clique_propose",
        Arrays.asList(address, auth.toString()),
        web3jService,
        ProposeResponse.class);
  }

  public Request<?, DiscardResponse> cliqueDiscard(final String address) {
    return new Request<>(
        "clique_discard", singletonList(address), web3jService, DiscardResponse.class);
  }

  public Request<?, ProposalsResponse> cliqueProposals() {
    return new Request<>("clique_proposals", emptyList(), web3jService, ProposalsResponse.class);
  }

  public Request<?, SignersBlockResponse> cliqueGetSigners(final String blockNumber) {
    return new Request<>(
        "clique_getSigners", singletonList(blockNumber), web3jService, SignersBlockResponse.class);
  }

  public Request<?, SignersBlockResponse> cliqueGetSignersAtHash(final Hash hash) {
    return new Request<>(
        "clique_getSignersAtHash",
        singletonList(hash.toString()),
        web3jService,
        SignersBlockResponse.class);
  }

}
