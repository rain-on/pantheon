package tech.pegasys.pantheon.consensus.ibft.validation;

import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.payload.PreparePayload;
import tech.pegasys.pantheon.consensus.ibft.payload.PreparedCertificate;
import tech.pegasys.pantheon.consensus.ibft.payload.ProposalPayload;
import tech.pegasys.pantheon.consensus.ibft.payload.RoundChangePayload;
import tech.pegasys.pantheon.consensus.ibft.payload.SignedData;
import tech.pegasys.pantheon.ethereum.core.Address;

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
          "Invalid RoundChange message, was not transmitted by a validator for the associated"
              + " round.");
      return false;
    }

    final ConsensusRoundIdentifier targetRound = payload.getRoundIdentifier();

    if (targetRound.getSequenceNumber() != chainHeight) {
      LOG.info("Invalid RoundChange message, not valid for local chain height.");
      return false;
    }

    if (!payload.getPreparedCertificate().isPresent()) {
      return true;
    }

    final PreparedCertificate certificate = payload.getPreparedCertificate().get();
    return validatePrepareCertificate(certificate, targetRound);
  }

  private boolean validatePrepareCertificate(
      final PreparedCertificate certificate,
      final ConsensusRoundIdentifier roundChangeTarget) {
    final SignedData<ProposalPayload> proposalPayload = certificate.getProposalPayload();

    final ConsensusRoundIdentifier proposalRoundIdentifier =
        proposalPayload.getPayload().getRoundIdentifier();

    if (!validatePreparedCertificateRound(proposalRoundIdentifier, roundChangeTarget)) {
      return false;
    }

    final SignedDataValidator signedDataValidator =
        signedDataValidatorFactory.createAt(proposalRoundIdentifier);

    return validateConsistencyOfPrepareCertificateMessages(certificate, signedDataValidator);
  }

  private boolean validateConsistencyOfPrepareCertificateMessages(
      final PreparedCertificate certificate, final SignedDataValidator dataValidator) {

    if (!dataValidator.addSignedProposalPayload(certificate.getProposalPayload())) {
      LOG.info("Invalid RoundChange message, embedded Proposal message failed validation.");
      return false;
    }

    if (certificate.getPreparePayloads().size() < minimumPrepareMessages) {
      LOG.info(
          "Invalid RoundChange message, insufficient Prepare messages exist to justify "
              + "prepare certificate.");
      return false;
    }

    for (final SignedData<PreparePayload> prepareMsg : certificate.getPreparePayloads()) {
      dataValidator.validatePreparePayload(prepareMsg);
      LOG.info("Invalid RoundChange message, embedded Prepare message failed validation.");
      return false;
    }

    return true;
  }

  private boolean validatePreparedCertificateRound(
      final ConsensusRoundIdentifier prepareCertRound,
      final ConsensusRoundIdentifier roundChangeTarget) {

    if (prepareCertRound.getSequenceNumber() != roundChangeTarget.getSequenceNumber()) {
      LOG.info("Invalid RoundChange message, PreparedCertificate is not for local chain height.");
      return false;
    }

    if (prepareCertRound.getRoundNumber() >= roundChangeTarget.getRoundNumber()) {
      LOG.info(
          "Invalid RoundChange message, PreparedCertificate not older than RoundChange target.");
      return false;
    }
    return true;
  }

  @FunctionalInterface
  public interface SignedDataValidatorForHeightFactory {

    SignedDataValidator createAt(ConsensusRoundIdentifier roundIdentifier);
  }


}
