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
import tech.pegasys.pantheon.consensus.ibft.payload.CommitPayload;
import tech.pegasys.pantheon.consensus.ibft.payload.Payload;
import tech.pegasys.pantheon.consensus.ibft.payload.PreparePayload;
import tech.pegasys.pantheon.consensus.ibft.payload.ProposalPayload;
import tech.pegasys.pantheon.consensus.ibft.payload.SignedData;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.Util;

import java.util.Collection;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SignedDataValidator {

  private static final Logger LOG = LogManager.getLogger();

  private final Collection<Address> validators;
  private final Address expectedProposer;
  private final ConsensusRoundIdentifier roundIdentifier;

  private Optional<SignedData<ProposalPayload>> proposal;

  public SignedDataValidator(
      final Collection<Address> validators,
      final Address expectedProposer,
      final ConsensusRoundIdentifier roundIdentifier) {
    this.validators = validators;
    this.expectedProposer = expectedProposer;
    this.roundIdentifier = roundIdentifier;
  }

  public boolean addSignedProposalPayload(final SignedData<ProposalPayload> msg) {

    if (!validateSignedProposalPayload(msg)) {
      return false;
    }

    proposal = Optional.of(msg);
    return true;
  }

  private boolean validateSignedProposalPayload(final SignedData<ProposalPayload> msg) {

    if (!msg.getPayload().getRoundIdentifier().equals(roundIdentifier)) {
      LOG.info("Invalid Proposal message, does not match current round.");
      return false;
    }

    if (!msg.getAuthor().equals(expectedProposer)) {
      LOG.info(
          "Invalid Proposal message, was not created by the proposer expected for the "
              + "associated round.");
      return false;
    }

    return true;
  }

  public boolean validatePreparePayload(final SignedData<PreparePayload> msg) {
    final String payloadType = "Prepare";

    if (!isMessageForCurrentRoundFromValidatorAndProposalAvailable(msg, payloadType)) {
      return false;
    }

    if (msg.getAuthor().equals(expectedProposer)) {
      LOG.info("Illegal Prepare message; was sent by the round's proposer.");
      return false;
    }

    return validateDigestMatchesProposal(msg.getPayload().getDigest(), payloadType);
  }

  public boolean validateCommitPayload(final SignedData<CommitPayload> msg) {
    final String payloadType = "Commit";

    if (!isMessageForCurrentRoundFromValidatorAndProposalAvailable(msg, payloadType)) {
      return false;
    }

    final Address commitSealCreator =
        Util.signatureToAddress(
            msg.getPayload().getCommitSeal(), proposal.get().getPayload().getDigest());

    if (!commitSealCreator.equals(msg.getAuthor())) {
      LOG.info("Invalid Commit message. Seal was not created by the message transmitter.");
      return false;
    }

    return validateDigestMatchesProposal(msg.getPayload().getDigest(), payloadType);
  }

  private boolean isMessageForCurrentRoundFromValidatorAndProposalAvailable(
      final SignedData<? extends Payload> msg, final String msgType) {

    if (!msg.getPayload().getRoundIdentifier().equals(roundIdentifier)) {
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
    if (!digest.equals(proposal.get().getPayload().getDigest())) {
      LOG.info(
          "Illegal {} message, digest does not match the block in the Prepare Message.", msgType);
      return false;
    }
    return true;
  }
}
