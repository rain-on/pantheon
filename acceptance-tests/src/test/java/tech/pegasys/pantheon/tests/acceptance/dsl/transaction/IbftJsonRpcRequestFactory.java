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

public class IbftJsonRpcRequestFactory {

  private final Web3jService web3jService;

  public IbftJsonRpcRequestFactory(Web3jService web3jService) {
    this.web3jService = web3jService;
  }

  public Request<?, ProposeResponse> ibftPropose(final String address, final Boolean auth) {
    return new Request<>(
        "ibft_proposeValidatorVote",
        Arrays.asList(address, auth.toString()),
        web3jService,
        ProposeResponse.class);
  }

  public Request<?, DiscardResponse> ibftDiscard(final String address) {
    return new Request<>(
        "ibft_discardValidatorVote", singletonList(address), web3jService, DiscardResponse.class);
  }

  public Request<?, ProposalsResponse> ibftProposals() {
    return new Request<>(
        "ibft_getPendingVotes", emptyList(), web3jService, ProposalsResponse.class);
  }

  public Request<?, SignersBlockResponse> ibftGetValidators(final String blockNumber) {
    return new Request<>(
        "ibft_getValidatorsByBlockNumber",
        singletonList(blockNumber),
        web3jService,
        SignersBlockResponse.class);
  }

  public Request<?, SignersBlockResponse> ibftGetSignersAtHash(final Hash hash) {
    return new Request<>(
        "ibft_getValidatorsByBlockHash",
        singletonList(hash.toString()),
        web3jService,
        SignersBlockResponse.class);
  }
}
