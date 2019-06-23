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
package tech.pegasys.pantheon.consensus.ibftlegacy.blockcreation;

import tech.pegasys.pantheon.consensus.common.ValidatorVote;
import tech.pegasys.pantheon.consensus.common.VoteTally;
import tech.pegasys.pantheon.consensus.common.VoteType;
import tech.pegasys.pantheon.consensus.ibft.IbftContext;
import tech.pegasys.pantheon.consensus.ibft.IbftHelpers;
import tech.pegasys.pantheon.consensus.ibftlegacy.IbftBlockHashing;
import tech.pegasys.pantheon.consensus.ibftlegacy.IbftExtraData;
import tech.pegasys.pantheon.consensus.ibftlegacy.LegacyIbftBlockHeaderFunctions;
import tech.pegasys.pantheon.crypto.SECP256K1;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.blockcreation.AbstractBlockCreator;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderBuilder;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.SealableBlockHeader;
import tech.pegasys.pantheon.ethereum.core.Wei;
import tech.pegasys.pantheon.ethereum.eth.transactions.PendingTransactions;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;

import java.util.Optional;
import java.util.function.Function;

// This class is responsible for creating a block without committer seals (basically it was just
// too hard to coordinate with the state machine).
public class IbftBlockCreator extends AbstractBlockCreator<IbftContext> {

  private final Address localAddress;
  private final KeyPair nodeKeys;

  public IbftBlockCreator(
      final Address localAddress,
      final ExtraDataCalculator extraDataCalculator,
      final PendingTransactions pendingTransactions,
      final ProtocolContext<IbftContext> protocolContext,
      final ProtocolSchedule<IbftContext> protocolSchedule,
      final Function<Long, Long> gasLimitCalculator,
      final Wei minTransactionGasPrice,
      final BlockHeader parentHeader,
      final KeyPair nodeKeys) {
    super(
        localAddress,
        extraDataCalculator,
        pendingTransactions,
        protocolContext,
        protocolSchedule,
        gasLimitCalculator,
        minTransactionGasPrice,
        localAddress,
        parentHeader);
    this.localAddress = localAddress;
    this.nodeKeys = nodeKeys;
  }

  @Override
  protected BlockHeader createFinalBlockHeader(final SealableBlockHeader sealableBlockHeader) {
    final VoteTally voteTally =
        protocolContext
            .getConsensusState()
            .getVoteTallyCache()
            .getVoteTallyAfterBlock(parentHeader);

    final Optional<ValidatorVote> proposal =
        protocolContext.getConsensusState().getVoteProposer().getVote(localAddress, voteTally);
    final Address coinbase = proposal.map(ValidatorVote::getRecipient).orElse(Address.ZERO);
    final VoteType voteDirection =
        proposal.map(ValidatorVote::getVotePolarity).orElse(VoteType.DROP);

    final BlockHeaderBuilder builder =
        BlockHeaderBuilder.create()
            .populateFrom(sealableBlockHeader)
            .coinbase(coinbase)
            .mixHash(IbftHelpers.EXPECTED_MIX_HASH)
            .nonce(voteDirectionToLong(voteDirection))
            .blockHeaderFunctions(LegacyIbftBlockHeaderFunctions.forCommittedSeal());

    final BlockHeader unsignedHeader = builder.buildBlockHeader();
    final IbftExtraData extraDataWithProposerSignature = constructSignedExtraData(unsignedHeader);

    final BlockHeaderBuilder signedBuilder =
        BlockHeaderBuilder.fromBuilder(builder).extraData(extraDataWithProposerSignature.encode());

    return signedBuilder.buildBlockHeader();
  }

  private IbftExtraData constructSignedExtraData(final BlockHeader headerToSign) {
    final IbftExtraData extraData = IbftExtraData.decode(headerToSign);
    final Hash hashToSign =
        IbftBlockHashing.calculateDataHashForProposerSeal(headerToSign, extraData);
    return new IbftExtraData(
        extraData.getVanityData(),
        extraData.getSeals(),
        SECP256K1.sign(hashToSign, nodeKeys),
        extraData.getValidators());
  }

  private long voteDirectionToLong(final VoteType direction) {
    if (direction == VoteType.ADD) {
      return 0xFFFFFFFFFFFFFFFFL;
    }
    return 0;
  }
}
