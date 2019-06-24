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
package tech.pegasys.pantheon.consensus.crossbft;

import tech.pegasys.pantheon.consensus.crossbft.headervalidationrules.CrossBftCommitSealsValidationRule;
import tech.pegasys.pantheon.consensus.ibft.IbftContext;
import tech.pegasys.pantheon.consensus.ibftlegacy.IbftBlockHeaderValidationRulesetFactory;
import tech.pegasys.pantheon.consensus.ibftlegacy.IbftHelpers;
import tech.pegasys.pantheon.consensus.ibftlegacy.headervalidationrules.IbftExtraDataValidationRule;
import tech.pegasys.pantheon.consensus.ibftlegacy.headervalidationrules.VoteValidationRule;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.mainnet.BlockHeaderValidator;
import tech.pegasys.pantheon.ethereum.mainnet.headervalidationrules.AncestryValidationRule;
import tech.pegasys.pantheon.ethereum.mainnet.headervalidationrules.ConstantFieldValidationRule;
import tech.pegasys.pantheon.ethereum.mainnet.headervalidationrules.GasLimitRangeAndDeltaValidationRule;
import tech.pegasys.pantheon.ethereum.mainnet.headervalidationrules.GasUsageValidationRule;
import tech.pegasys.pantheon.ethereum.mainnet.headervalidationrules.TimestampBoundedByFutureParameter;
import tech.pegasys.pantheon.ethereum.mainnet.headervalidationrules.TimestampMoreRecentThanParent;
import tech.pegasys.pantheon.util.uint.UInt256;

public class CrossBftHeaderValidationlRulestetFactory {

  public static BlockHeaderValidator<IbftContext> preCrossBftValidationRules(
      final long secondsBetweenBlocks) {
    return IbftBlockHeaderValidationRulesetFactory.ibftBlockHeaderValidator(secondsBetweenBlocks);
  }

  public static BlockHeaderValidator<IbftContext> postCrossBftValidationRules(
      final long secondsBetweenBlocks) {
    return new BlockHeaderValidator.Builder<IbftContext>()
        .addRule(new AncestryValidationRule())
        .addRule(new GasUsageValidationRule())
        .addRule(new GasLimitRangeAndDeltaValidationRule(5000, 0x7fffffffffffffffL))
        .addRule(new TimestampBoundedByFutureParameter(1))
        .addRule(new TimestampMoreRecentThanParent(secondsBetweenBlocks))
        .addRule(
            new ConstantFieldValidationRule<>(
                "MixHash", BlockHeader::getMixHash, IbftHelpers.EXPECTED_MIX_HASH))
        .addRule(
            new ConstantFieldValidationRule<>(
                "OmmersHash", BlockHeader::getOmmersHash, Hash.EMPTY_LIST_HASH))
        .addRule(
            new ConstantFieldValidationRule<>(
                "Difficulty", BlockHeader::getDifficulty, UInt256.ONE))
        .addRule(new VoteValidationRule())
        .addRule(new IbftExtraDataValidationRule())
        .addRule(new CrossBftCommitSealsValidationRule())
        .build();
  }
}
