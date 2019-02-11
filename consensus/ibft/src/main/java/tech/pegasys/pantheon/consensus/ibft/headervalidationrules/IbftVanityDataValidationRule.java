package tech.pegasys.pantheon.consensus.ibft.headervalidationrules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.pegasys.pantheon.consensus.ibft.IbftExtraData;
import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.mainnet.AttachedBlockHeaderValidationRule;

public class IbftVanityDataValidationRule implements AttachedBlockHeaderValidationRule {

  private static final Logger LOG = LogManager.getLogger();

  @Override
  public boolean validate(final BlockHeader header, final BlockHeader parent,
      final ProtocolContext protocolContext) {
    final IbftExtraData extraData = IbftExtraData.decode(header.getExtraData());

    if(extraData.getVanityData().size() != IbftExtraData.EXTRA_VANITY_LENGTH) {
      LOG.trace("Ibft Extra Data does not contain 32 bytes of vanity data.");
      return false;
    }
    return true;
  }
}
