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
package tech.pegasys.pantheon.consensus.crossbft;

import static tech.pegasys.pantheon.consensus.crossbft.CrossBftHeaderValidationlRulestetFactory.postCrossBftValidationRules;
import static tech.pegasys.pantheon.consensus.crossbft.CrossBftHeaderValidationlRulestetFactory.preCrossBftValidationRules;

import tech.pegasys.pantheon.config.CrossBftConfigOptions;
import tech.pegasys.pantheon.config.GenesisConfigOptions;
import tech.pegasys.pantheon.consensus.ibft.IbftContext;
import tech.pegasys.pantheon.consensus.ibftlegacy.LegacyIbftBlockHeaderFunctions;
import tech.pegasys.pantheon.ethereum.MainnetBlockValidator;
import tech.pegasys.pantheon.ethereum.core.PrivacyParameters;
import tech.pegasys.pantheon.ethereum.core.Wei;
import tech.pegasys.pantheon.ethereum.mainnet.MainnetBlockBodyValidator;
import tech.pegasys.pantheon.ethereum.mainnet.MainnetBlockImporter;
import tech.pegasys.pantheon.ethereum.mainnet.MainnetProtocolSpecs;
import tech.pegasys.pantheon.ethereum.mainnet.MutableProtocolSchedule;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolScheduleBuilder;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSpecBuilder;

import java.math.BigInteger;

/** Defines the protocol behaviours for a blockchain using CrossBft */
public class CrossBftProtocolSchedule {

  private static final BigInteger DEFAULT_CHAIN_ID = BigInteger.ONE;

  public static ProtocolSchedule<IbftContext> create(
      final GenesisConfigOptions genesisConfig, final PrivacyParameters privacyParameters) {
    final CrossBftConfigOptions crossBftConfig = genesisConfig.getCrossBfrConfigOptions();
    final long blockPeriod = crossBftConfig.getBlockPeriodSeconds();

    ProtocolScheduleBuilder<IbftContext> scheduleBuilder =
        new ProtocolScheduleBuilder<>(
            genesisConfig,
            DEFAULT_CHAIN_ID,
            builder -> applyCrossBftChanges(blockPeriod, builder),
            privacyParameters);

    ProtocolSchedule<IbftContext> protSchedule = scheduleBuilder.createProtocolSchedule();
    MutableProtocolSchedule<IbftContext> mutableProtSchedule =
        (MutableProtocolSchedule<IbftContext>) protSchedule;

    mutableProtSchedule.putMilestone(
        crossBftConfig.getCrossOverBlock(),
        applyPostCrossOverBftChanges(
                blockPeriod,
                MainnetProtocolSpecs.constantinopleFixDefinition(
                    genesisConfig.getChainId(),
                    genesisConfig.getContractSizeLimit(),
                    genesisConfig.getEvmStackSize()).privacyParameters(privacyParameters))
            .build(mutableProtSchedule));

    return mutableProtSchedule;
  }

  public static ProtocolSchedule<IbftContext> create(final GenesisConfigOptions config) {
    return create(config, PrivacyParameters.DEFAULT);
  }

  private static ProtocolSpecBuilder<IbftContext> applyCrossBftChanges(
      final long secondsBetweenBlocks, final ProtocolSpecBuilder<Void> builder) {
    return builder
        .<IbftContext>changeConsensusContextType(
            difficultyCalculator -> preCrossBftValidationRules(secondsBetweenBlocks),
            difficultyCalculator -> preCrossBftValidationRules(secondsBetweenBlocks),
            MainnetBlockBodyValidator::new,
            MainnetBlockValidator::new,
            MainnetBlockImporter::new,
            (time, parent, protocolContext) -> BigInteger.ONE)
        .blockReward(Wei.ZERO)
        .blockHeaderFunctions(LegacyIbftBlockHeaderFunctions.forOnChainBlock());
  }

  private static ProtocolSpecBuilder<IbftContext> applyPostCrossOverBftChanges(
      final long secondsBetweenBlocks, final ProtocolSpecBuilder<Void> builder) {
    return builder
        .<IbftContext>changeConsensusContextType(
            difficultyCalculator -> postCrossBftValidationRules(secondsBetweenBlocks),
            difficultyCalculator -> postCrossBftValidationRules(secondsBetweenBlocks),
            MainnetBlockBodyValidator::new,
            MainnetBlockValidator::new,
            MainnetBlockImporter::new,
            (time, parent, protocolContext) -> BigInteger.ONE)
        .blockReward(Wei.ZERO)
        .blockHeaderFunctions(LegacyIbftBlockHeaderFunctions.forOnChainBlock());
  }
}
