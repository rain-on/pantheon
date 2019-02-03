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
package tech.pegasys.pantheon.consensus.ibft.validation;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.IbftContext;
import tech.pegasys.pantheon.consensus.ibft.IbftExtraData;
import tech.pegasys.pantheon.consensus.ibft.payload.ProposalPayload;
import tech.pegasys.pantheon.consensus.ibft.payload.SignedData;
import tech.pegasys.pantheon.ethereum.BlockValidator;
import tech.pegasys.pantheon.ethereum.BlockValidator.BlockProcessingOutputs;
import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.mainnet.HeaderValidationMode;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProposalBlockConsistencyChecker {

  private static final Logger LOG = LogManager.getLogger();

  private final BlockValidator<IbftContext> blockValidator;
  private final ProtocolContext<IbftContext> protocolContext;

  public ProposalBlockConsistencyChecker(
      final BlockValidator<IbftContext> blockValidator,
      final ProtocolContext<IbftContext> protocolContext) {
    this.blockValidator = blockValidator;
    this.protocolContext = protocolContext;
  }

  public boolean validateProposalMatchesBlock(
      final SignedData<ProposalPayload> signedPayload, final Block proposedBlock) {
    if (!validateBlockMatchesProposalRound(signedPayload.getPayload(), proposedBlock)) {
      return false;
    }

    final Optional<BlockProcessingOutputs> validationResult =
        blockValidator.validateAndProcessBlock(
            protocolContext, proposedBlock, HeaderValidationMode.LIGHT, HeaderValidationMode.FULL);

    if (!validationResult.isPresent()) {
      LOG.info("Invalid Proposal message, block did not pass validation.");
      return false;
    }

    return true;
  }

  private boolean validateBlockMatchesProposalRound(
      final ProposalPayload payload, final Block block) {
    final ConsensusRoundIdentifier msgRound = payload.getRoundIdentifier();
    final IbftExtraData extraData = IbftExtraData.decode(block.getHeader().getExtraData());
    if (extraData.getRound() != msgRound.getRoundNumber()) {
      LOG.info("Invalid Proposal message, round number in block does not match that in message.");
      return false;
    }
    return true;
  }
}
