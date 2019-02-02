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
  private final BlockHeader parentHeader;

  public MessageValidator(
      SignedDataValidator dataValidator,
      BlockValidator<IbftContext> blockValidator,
      ProtocolContext<IbftContext> protocolContext,
      BlockHeader parentHeader) {
    this.dataValidator = dataValidator;
    this.blockValidator = blockValidator;
    this.protocolContext = protocolContext;
    this.parentHeader = parentHeader;
  }

  public boolean addSignedProposalPayload(final Proposal msg) {
    final SignedData<ProposalPayload> signedPayload = msg.getSignedPayload();
    if (!validateBlockMatchesProposalRound(signedPayload.getPayload(), msg.getBlock())) {
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

    return dataValidator.addSignedProposalPayload(signedPayload);
  }

  public boolean validatePrepareMessage(final Prepare msg) {
    return dataValidator.validatePreparePayload(msg.getSignedPayload());
  }

  public boolean validateCommitMessage(final Commit msg) {
    return dataValidator.validateCommitPayload(msg.getSignedPayload());
  }

  private boolean validateBlockMatchesProposalRound(final ProposalPayload payload,
      final Block block) {
    final ConsensusRoundIdentifier msgRound = payload.getRoundIdentifier();
    final IbftExtraData extraData =
        IbftExtraData.decode(block.getHeader().getExtraData());
    if (extraData.getRound() != msgRound.getRoundNumber()) {
      LOG.info("Invalid Proposal message, round number in block does not match that in message.");
      return false;
    }
    return true;
  }
}
