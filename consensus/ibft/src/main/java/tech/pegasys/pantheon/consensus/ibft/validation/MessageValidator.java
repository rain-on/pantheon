package tech.pegasys.pantheon.consensus.ibft.validation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.Commit;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.Prepare;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.Proposal;
import tech.pegasys.pantheon.consensus.ibft.payload.ProposalPayload;
import tech.pegasys.pantheon.consensus.ibft.payload.SignedData;

public class MessageValidator {

  private static final Logger LOG = LogManager.getLogger();
  private final SignedDataValidator dataValidator;
  private final ProposalBlockConsistencyChecker consistencyChecker;

  public MessageValidator(
      final SignedDataValidator dataValidator,
      final ProposalBlockConsistencyChecker consistencyChecker) {
    this.dataValidator = dataValidator;
    this.consistencyChecker = consistencyChecker;
  }

  public boolean addProposalMessage(final Proposal msg) {
    final SignedData<ProposalPayload> signedPayload = msg.getSignedPayload();

    if(!consistencyChecker.validateProposalMatchesBlock(msg.getSignedPayload(), msg.getBlock())) {
      LOG.info("Invalid Proposal, Block does not validate, or does not align with proposal data.");
    }

    return dataValidator.addSignedProposalPayload(signedPayload);
  }

  public boolean validatePrepareMessage(final Prepare msg) {
    return dataValidator.validatePreparePayload(msg.getSignedPayload());
  }

  public boolean validateCommitMessage(final Commit msg) {
    return dataValidator.validateCommitPayload(msg.getSignedPayload());
  }


}
