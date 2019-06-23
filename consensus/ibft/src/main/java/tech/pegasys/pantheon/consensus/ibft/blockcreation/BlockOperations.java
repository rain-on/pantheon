package tech.pegasys.pantheon.consensus.ibft.blockcreation;

import java.util.Collection;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;

public interface BlockOperations {

  Signature createCommitSealForBlock(final BlockHeader header);

  Block createSealedBlock(final Block block, final Collection<Signature> commitSeals);

}
