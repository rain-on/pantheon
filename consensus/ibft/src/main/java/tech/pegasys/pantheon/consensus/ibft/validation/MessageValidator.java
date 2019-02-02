package tech.pegasys.pantheon.consensus.ibft.validation;

import com.fasterxml.jackson.databind.annotation.JsonAppend.Prop;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.IbftContext;
import tech.pegasys.pantheon.consensus.ibft.IbftExtraData;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.Commit;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.Prepare;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.Proposal;
import tech.pegasys.pantheon.consensus.ibft.payload.ProposalPayload;
import tech.pegasys.pantheon.consensus.ibft.payload.SignedData;
import tech.pegasys.pantheon.ethereum.BlockValidator;
import tech.pegasys.pantheon.ethereum.BlockValidator.BlockProcessingOutputs;
import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.mainnet.HeaderValidationMode;

public class MessageValidator {

  private static final Logger LOG = LogManager.getLogger();
  private final SignedDataValidator dataValidator;
  private final BlockValidator<IbftContext> blockValidator;
  private final ProtocolContext<IbftContext> protocolContext;

  public MessageValidator(
      SignedDataValidator dataValidator,
      BlockValidator<IbftContext> blockValidator,
      ProtocolContext<IbftContext> protocolContext,
      BlockHeader parentHeader) {
    this.dataValidator = dataValidator;
    this.blockValidator = blockValidator;
    this.protocolContext = protocolContext;
  }

  public boolean addSignedProposalPayload(final Proposal msg) {
    final SignedData<ProposalPayload> signedPayload = msg.getSignedPayload();

    ProposalBlockConsistencyChecker consistencyChecker = new ProposalBlockConsistencyChecker(
        blockValidator, protocolContext);

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
