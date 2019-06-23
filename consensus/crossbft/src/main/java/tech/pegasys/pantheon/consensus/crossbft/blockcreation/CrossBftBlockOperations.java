package tech.pegasys.pantheon.consensus.crossbft.blockcreation;

import java.util.Collection;
import tech.pegasys.pantheon.consensus.ibft.blockcreation.BlockOperations;
import tech.pegasys.pantheon.consensus.ibftlegacy.IbftBlockHashing;
import tech.pegasys.pantheon.consensus.ibftlegacy.IbftExtraData;
import tech.pegasys.pantheon.consensus.ibftlegacy.LegacyIbftBlockHeaderFunctions;
import tech.pegasys.pantheon.crypto.SECP256K1;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderBuilder;

public class CrossBftBlockOperations implements BlockOperations {


  private KeyPair nodeKeys;

  public CrossBftBlockOperations(KeyPair nodeKeys) {
    this.nodeKeys = nodeKeys;
  }

  @Override
  public Signature createCommitSealForBlock(final BlockHeader header) {
    return SECP256K1.sign(IbftBlockHashing.calculateDataHashForCommittedSeal(header), nodeKeys);
  }

  @Override
  public Block createSealedBlock(final Block block, final Collection<Signature> commitSeals) {
    final BlockHeader initialHeader = block.getHeader();
    final IbftExtraData initialExtraData = IbftExtraData.decode(initialHeader);

    final IbftExtraData sealedExtraData =
        new IbftExtraData(
            initialExtraData.getVanityData(),
            commitSeals,
            initialExtraData.getProposerSeal(),
            initialExtraData.getValidators());

    final BlockHeader sealedHeader =
        BlockHeaderBuilder.fromHeader(initialHeader)
            .extraData(sealedExtraData.encode())
            .blockHeaderFunctions(LegacyIbftBlockHeaderFunctions.forOnChainBlock())
            .buildBlockHeader();

    return new Block(sealedHeader, block.getBody());
  }
}
