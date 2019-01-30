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

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.IbftContext;
import tech.pegasys.pantheon.consensus.ibft.IbftExtraData;
import tech.pegasys.pantheon.consensus.ibft.payload.CommitMessage;
import tech.pegasys.pantheon.consensus.ibft.payload.IbftMessage;
import tech.pegasys.pantheon.consensus.ibft.payload.PrepareMessage;
import tech.pegasys.pantheon.consensus.ibft.payload.ProposalMessage;
import tech.pegasys.pantheon.ethereum.BlockValidator;
import tech.pegasys.pantheon.ethereum.BlockValidator.BlockProcessingOutputs;
import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.ethereum.mainnet.HeaderValidationMode;

import java.util.Collection;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageValidator {

  private static final Logger LOG = LogManager.getLogger();

  private final Collection<Address> validators;
  private final Address expectedProposer;
  private final ConsensusRoundIdentifier roundIdentifier;
  private final BlockValidator<IbftContext> blockValidator;
  private final ProtocolContext<IbftContext> protocolContext;
  private final BlockHeader parentHeader;

  private Optional<ProposalMessage> proposal = Optional.empty();

  public MessageValidator(
      final Collection<Address> validators,
      final Address expectedProposer,
      final ConsensusRoundIdentifier roundIdentifier,
      final BlockValidator<IbftContext> blockValidator,
      final ProtocolContext<IbftContext> protocolContext,
      final BlockHeader parentHeader) {
    this.validators = validators;
    this.expectedProposer = expectedProposer;
    this.roundIdentifier = roundIdentifier;
    this.blockValidator = blockValidator;
    this.protocolContext = protocolContext;
    this.parentHeader = parentHeader;
  }

  public boolean addSignedProposalPayload(final ProposalMessage msg) {

    if (proposal.isPresent()) {
      return handleSubsequentProposal(proposal.get(), msg);
    }

    if (!validateSignedProposalPayload(msg)) {
      return false;
    }

    if (!validateBlockMatchesProposalRound(msg)) {
      return false;
    }

    proposal = Optional.of(msg);
    return true;
  }

  private boolean validateSignedProposalPayload(final ProposalMessage msg) {

    if (!msg.getConsensusRound().equals(roundIdentifier)) {
      LOG.info("Invalid Proposal message, does not match current round.");
      return false;
    }

    if (!msg.getAuthor().equals(expectedProposer)) {
      LOG.info(
          "Invalid Proposal message, was not created by the proposer expected for the "
              + "associated round.");
      return false;
    }

    final Block proposedBlock = msg.getBlock();

    final Optional<BlockProcessingOutputs> validationResult =
        blockValidator.validateAndProcessBlock(
            protocolContext, proposedBlock, HeaderValidationMode.LIGHT, HeaderValidationMode.FULL);

    if (!validationResult.isPresent()) {
      LOG.info("Invalid Proposal message, block did not pass validation.");
      return false;
    }

    return true;
  }

  private boolean handleSubsequentProposal(
      final ProposalMessage existingMsg, final ProposalMessage newMsg) {
    if (!existingMsg.getAuthor().equals(newMsg.getAuthor())) {
      LOG.debug("Received subsequent invalid Proposal message; sender differs from original.");
      return false;
    }

    if (!proposalMessagesAreIdentical(existingMsg, newMsg)) {
      LOG.debug("Received subsequent invalid Proposal message; content differs from original.");
      return false;
    }

    return true;
  }

  public boolean validatePrepareMessage(final PrepareMessage msg) {
    final String msgType = "Prepare";

    if (!isMessageForCurrentRoundFromValidatorAndProposalAvailable(msg, msgType)) {
      return false;
    }

    if (msg.getAuthor().equals(expectedProposer)) {
      LOG.info("Illegal Prepare message; was sent by the round's proposer.");
      return false;
    }

    return validateDigestMatchesProposal(msg.getDigest(), msgType);
  }

  public boolean validateCommmitMessage(final CommitMessage msg) {
    final String msgType = "Commit";

    if (!isMessageForCurrentRoundFromValidatorAndProposalAvailable(msg, msgType)) {
      return false;
    }

    final Block proposedBlock = proposal.get().getBlock();
    final Address commitSealCreator =
        Util.signatureToAddress(msg.getCommitSeal(), proposedBlock.getHash());

    if (!commitSealCreator.equals(msg.getAuthor())) {
      LOG.info("Invalid Commit message. Seal was not created by the message transmitter.");
      return false;
    }

    return validateDigestMatchesProposal(msg.getDigest(), msgType);
  }

  private boolean isMessageForCurrentRoundFromValidatorAndProposalAvailable(
      final IbftMessage msg, final String msgType) {

    if (!msg.getConsensusRound().equals(roundIdentifier)) {
      LOG.info("Invalid {} message, does not match current round.", msgType);
      return false;
    }

    if (!validators.contains(msg.getAuthor())) {
      LOG.info(
          "Invalid {} message, was not transmitted by a validator for the " + "associated round.",
          msgType);
      return false;
    }

    if (!proposal.isPresent()) {
      LOG.info(
          "Unable to validate {} message. No Proposal exists against which to validate "
              + "block digest.",
          msgType);
      return false;
    }
    return true;
  }

  private boolean validateDigestMatchesProposal(final Hash digest, final String msgType) {
    final Block proposedBlock = proposal.get().getBlock();
    if (!digest.equals(proposedBlock.getHash())) {
      LOG.info(
          "Illegal {} message, digest does not match the block in the Prepare Message.", msgType);
      return false;
    }
    return true;
  }

  private boolean proposalMessagesAreIdentical(
      final ProposalMessage right, final ProposalMessage left) {
    return right.getBlock().getHash().equals(left.getBlock().getHash())
        && right.getConsensusRound().equals(left.getConsensusRound());
  }

  private boolean validateBlockMatchesProposalRound(final ProposalMessage msg) {
    final int msgRound = msg.getRound();
    final IbftExtraData extraData = IbftExtraData.decode(msg.getBlock().getHeader().getExtraData());
    if (extraData.getRound() != msgRound) {
      LOG.info("Invalid Proposal message, round number in block does not match that in message.");
      return false;
    }
    return true;
  }
}
