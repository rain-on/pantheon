package tech.pegasys.pantheon.consensus.ibft.validation;

import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

public class ProposalBlockConsistencyChecker {

  private static final Logger LOG = LogManager.getLogger();

  private final BlockValidator<IbftContext> blockValidator;
  private final ProtocolContext<IbftContext> protocolContext;

  public ProposalBlockConsistencyChecker(
      BlockValidator<IbftContext> blockValidator,
      ProtocolContext<IbftContext> protocolContext) {
    this.blockValidator = blockValidator;
    this.protocolContext = protocolContext;
  }

  public boolean validateProposalMatchesBlock(final SignedData<ProposalPayload> signedPayload,
      final Block proposedBlock) {
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

  private boolean validateBlockMatchesProposalRound(final ProposalPayload payload,
      final Block block) {
    final ConsensusRoundIdentifier msgRound = payload.getRoundIdentifier();
    final IbftExtraData extraData =
        IbftExtraData.decode(block.getHeader().getExtraData());
    if (extraData.getRound() != msgRound.getRoundNumber()) {
      //LOG.info("Invalid Proposal message, round number in block does not match that in message.");
      return false;
    }
    return true;
  }

}
