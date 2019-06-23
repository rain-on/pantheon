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
package tech.pegasys.pantheon.consensus.crossbft.blockcreation;

import tech.pegasys.pantheon.consensus.common.ConsensusHelpers;
import tech.pegasys.pantheon.consensus.common.VoteTally;
import tech.pegasys.pantheon.consensus.ibft.IbftContext;
import tech.pegasys.pantheon.consensus.ibft.blockcreation.BlockCreatorFactory;
import tech.pegasys.pantheon.consensus.ibftlegacy.IbftExtraData;
import tech.pegasys.pantheon.consensus.ibftlegacy.blockcreation.IbftBlockCreator;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.blockcreation.AbstractBlockCreator;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.MiningParameters;
import tech.pegasys.pantheon.ethereum.eth.transactions.PendingTransactions;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class CrossbftBlockCreatorFactory extends BlockCreatorFactory {

  private final Function<Long, Long> gasLimitCalculator;
  private final PendingTransactions pendingTransactions;
  protected final ProtocolContext<IbftContext> protocolContext;
  protected final ProtocolSchedule<IbftContext> protocolSchedule;
  private final KeyPair nodeKeys;

  public CrossbftBlockCreatorFactory(
      final Function<Long, Long> gasLimitCalculator,
      final PendingTransactions pendingTransactions,
      final ProtocolContext<IbftContext> protocolContext,
      final ProtocolSchedule<IbftContext> protocolSchedule,
      final MiningParameters miningParams,
      final KeyPair nodeKeys) {
    super(miningParams.getExtraData(), miningParams.getMinTransactionGasPrice());
    this.gasLimitCalculator = gasLimitCalculator;
    this.pendingTransactions = pendingTransactions;
    this.protocolContext = protocolContext;
    this.protocolSchedule = protocolSchedule;
    this.nodeKeys = nodeKeys;
  }

  @Override
  public AbstractBlockCreator<?> create(final BlockHeader parentHeader, final int round) {
    return new IbftBlockCreator(
        Address.ZERO,
        ph -> createExtraData(ph),
        pendingTransactions,
        protocolContext,
        protocolSchedule,
        gasLimitCalculator,
        getMinTransactionGasPrice(),
        parentHeader,
        nodeKeys);
  }

  // This creates an extraData object containing no proposerSeal.
  private BytesValue createExtraData(final BlockHeader parentHeader) {
    final VoteTally voteTally =
        protocolContext
            .getConsensusState()
            .getVoteTallyCache()
            .getVoteTallyAfterBlock(parentHeader);

    final List<Address> validators = new ArrayList<>(voteTally.getValidators());

    final IbftExtraData extraData =
        new IbftExtraData(
            ConsensusHelpers.zeroLeftPad(vanityData, IbftExtraData.EXTRA_VANITY_LENGTH),
            Collections.emptyList(),
            null,
            validators);

    return extraData.encode();
  }
}
