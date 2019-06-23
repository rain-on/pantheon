package tech.pegasys.pantheon.consensus.ibft.blockcreation;

import java.util.Collection;
import tech.pegasys.pantheon.consensus.ibft.IbftBlockHashing;
import tech.pegasys.pantheon.consensus.ibft.IbftBlockHeaderFunctions;
import tech.pegasys.pantheon.consensus.ibft.IbftExtraData;
import tech.pegasys.pantheon.crypto.SECP256K1;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderBuilder;
import tech.pegasys.pantheon.ethereum.core.Hash;

public class IbftBlockOperations implements BlockOperations {

  private KeyPair nodeKeys;

  public IbftBlockOperations(KeyPair nodeKeys) {
    this.nodeKeys = nodeKeys;
  }

  @Override
  public Signature createCommitSealForBlock(final BlockHeader header) {
    final IbftExtraData extraData = IbftExtraData.decode(header);
    final Hash commitHash =
        IbftBlockHashing.calculateDataHashForCommittedSeal(header, extraData);
    return SECP256K1.sign(commitHash, nodeKeys);
  }

  @Override
  public  Block createSealedBlock(final Block block, final Collection<Signature> commitSeals) {
    final BlockHeader initialHeader = block.getHeader();
    final IbftExtraData initialExtraData = IbftExtraData.decode(initialHeader);

    final IbftExtraData sealedExtraData =
        new IbftExtraData(
            initialExtraData.getVanityData(),
            commitSeals,
            initialExtraData.getVote(),
            initialExtraData.getRound(),
            initialExtraData.getValidators());

    final BlockHeader sealedHeader =
        BlockHeaderBuilder.fromHeader(initialHeader)
            .extraData(sealedExtraData.encode())
            .blockHeaderFunctions(IbftBlockHeaderFunctions.forOnChainBlock())
            .buildBlockHeader();

    return new Block(sealedHeader, block.getBody());
  }
}
