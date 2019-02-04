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
package tech.pegasys.pantheon.consensus.ibft.validation;

import tech.pegasys.pantheon.consensus.ibft.IbftContext;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.Commit;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.Prepare;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.Proposal;
import tech.pegasys.pantheon.ethereum.BlockValidator;
import tech.pegasys.pantheon.ethereum.BlockValidator.BlockProcessingOutputs;
import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.mainnet.HeaderValidationMode;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageValidator {

  private static final Logger LOG = LogManager.getLogger();

  private final SignedDataValidator signedDataValidator;
  private final ProposalBlockConsistencyValidator proposalConsistencyChecker;
  private final BlockValidator<IbftContext> blockValidator;
  private final ProtocolContext<IbftContext> protocolContext;

  public MessageValidator(
      final SignedDataValidator signedDataValidator,
      final ProposalBlockConsistencyValidator proposalConsistencyChecker,
      final BlockValidator<IbftContext> blockValidator,
      final ProtocolContext<IbftContext> protocolContext) {
    this.signedDataValidator = signedDataValidator;
    this.proposalConsistencyChecker = proposalConsistencyChecker;
    this.blockValidator = blockValidator;
    this.protocolContext = protocolContext;
  }

  public boolean validateProposal(final Proposal msg) {
    if (!signedDataValidator.addSignedProposalPayload(msg.getSignedPayload())) {
      LOG.debug("Illegal Proposal message, embedded signed data failed validation");
      return false;
    }

    if (!validateBlock(msg.getBlock())) {
      return false;
    }

    return proposalConsistencyChecker.validateProposalMatchesBlock(
        msg.getSignedPayload(), msg.getBlock());
  }

  private boolean validateBlock(final Block block) {
    final Optional<BlockProcessingOutputs> validationResult =
        blockValidator.validateAndProcessBlock(
            protocolContext, block, HeaderValidationMode.LIGHT, HeaderValidationMode.FULL);

    if (!validationResult.isPresent()) {
      LOG.info("Invalid Proposal message, block did not pass validation.");
      return false;
    }

    return true;
  }

  public boolean validatePrepare(final Prepare msg) {
    return signedDataValidator.validatePrepareMessage(msg.getSignedPayload());
  }

  public boolean validateCommit(final Commit msg) {
    return signedDataValidator.validateCommmitMessage(msg.getSignedPayload());
  }
}
