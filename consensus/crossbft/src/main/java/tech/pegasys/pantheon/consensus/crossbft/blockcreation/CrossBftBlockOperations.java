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
package tech.pegasys.pantheon.consensus.crossbft.blockcreation;

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

import java.util.Collection;

public class CrossBftBlockOperations implements BlockOperations {

  private KeyPair nodeKeys;

  public CrossBftBlockOperations(final KeyPair nodeKeys) {
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
