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
import tech.pegasys.pantheon.consensus.ibft.payload.PreparePayload;
import tech.pegasys.pantheon.consensus.ibft.payload.PreparedCertificate;
import tech.pegasys.pantheon.consensus.ibft.payload.ProposalPayload;
import tech.pegasys.pantheon.consensus.ibft.payload.RoundChangePayload;
import tech.pegasys.pantheon.consensus.ibft.payload.SignedData;
import tech.pegasys.pantheon.ethereum.core.Address;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RoundChangeSignedDataValidator {

  private static final Logger LOG = LogManager.getLogger();

  private final Collection<Address> validators;
  private final long minimumPrepareMessages;
  private final long chainHeight;
  private final SignedDataValidatorForHeightFactory signedDataValidatorFactory;

  public RoundChangeSignedDataValidator(
      final SignedDataValidatorForHeightFactory signedDataValidatorFactory,
      final Collection<Address> validators,
      final long minimumPrepareMessages,
      final long chainHeight) {
    this.validators = validators;
    this.minimumPrepareMessages = minimumPrepareMessages;
    this.chainHeight = chainHeight;
    this.signedDataValidatorFactory = signedDataValidatorFactory;
  }

  public boolean validatePayload(final SignedData<RoundChangePayload> signedPayload) {
    final RoundChangePayload payload = signedPayload.getPayload();
    if (!validators.contains(signedPayload.getAuthor())) {
      LOG.info(
          "Invalid RoundChange payload, was not transmitted by a validator for the associated"
              + " round.");
      return false;
    }

    final ConsensusRoundIdentifier targetRound = payload.getRoundIdentifier();

    if (targetRound.getSequenceNumber() != chainHeight) {
      LOG.info("Invalid RoundChange payload, not valid for local chain height.");
      return false;
    }

    if (!payload.getPreparedCertificate().isPresent()) {
      return true;
    }

    final PreparedCertificate certificate = payload.getPreparedCertificate().get();

    if(!validatePrepareCertificate(certificate, targetRound)) {
      LOG.info("Invalid RoundChange payload, prepare certificate is illegal.");
      return false;
    }

    return true;
  }

  private boolean validatePrepareCertificate(
      final PreparedCertificate certificate, final ConsensusRoundIdentifier roundChangeTarget) {
    final SignedData<ProposalPayload> proposalPayload = certificate.getProposalPayload();

    final ConsensusRoundIdentifier proposalRoundIdentifier =
        proposalPayload.getPayload().getRoundIdentifier();

    if (!validatePreparedCertificateRound(proposalRoundIdentifier, roundChangeTarget)) {
      LOG.info("Invalid RoundChange payload, proposal does not match roundChangeTarget.");
      return false;
    }

    final SignedDataValidator signedDataValidator =
        signedDataValidatorFactory.createAt(proposalRoundIdentifier);

    if(!validateConsistencyOfPrepareCertificateMessages(certificate, signedDataValidator)) {
      LOG.info("Invalid RoundChange payload, prepare certificate message are not consistent.");
      return false;
    }

    return true;
  }

  private boolean validateConsistencyOfPrepareCertificateMessages(
      final PreparedCertificate certificate, final SignedDataValidator dataValidator) {

    if (!dataValidator.addSignedProposalPayload(certificate.getProposalPayload())) {
      LOG.info("Invalid RoundChange payload, embedded Proposal message failed validation.");
      return false;
    }

    if (certificate.getPreparePayloads().size() < minimumPrepareMessages) {
      LOG.info(
          "Invalid RoundChange payload, insufficient Prepare messages exist to justify "
              + "prepare certificate.");
      return false;
    }

    for (final SignedData<PreparePayload> prepareMsg : certificate.getPreparePayloads()) {
      if(!dataValidator.validatePreparePayload(prepareMsg)) {
        LOG.info("Invalid RoundChange payload, embedded Prepare message failed validation.");
        return false;
      }
    }

    return true;
  }

  private boolean validatePreparedCertificateRound(
      final ConsensusRoundIdentifier prepareCertRound,
      final ConsensusRoundIdentifier roundChangeTarget) {

    if (prepareCertRound.getSequenceNumber() != roundChangeTarget.getSequenceNumber()) {
      LOG.info("Invalid RoundChange payload, PreparedCertificate is not for local chain height.");
      return false;
    }

    if (prepareCertRound.getRoundNumber() >= roundChangeTarget.getRoundNumber()) {
      LOG.info(
          "Invalid RoundChange payload, PreparedCertificate not older than RoundChange target.");
      return false;
    }
    return true;
  }

  @FunctionalInterface
  public interface SignedDataValidatorForHeightFactory {

    SignedDataValidator createAt(ConsensusRoundIdentifier roundIdentifier);
  }
}