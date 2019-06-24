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
package tech.pegasys.pantheon.consensus.ibft.blockcreation;

import tech.pegasys.pantheon.consensus.ibft.IbftBlockHashing;
import tech.pegasys.pantheon.consensus.ibft.IbftBlockHeaderFunctions;
import tech.pegasys.pantheon.consensus.ibft.IbftExtraData;
import tech.pegasys.pantheon.crypto.SECP256K1;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderBuilder;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderFunctions;
import tech.pegasys.pantheon.ethereum.core.Hash;

import java.util.Collection;

public class IbftBlockOperations implements BlockOperations {

  private KeyPair nodeKeys;

  public IbftBlockOperations(final KeyPair nodeKeys) {
    this.nodeKeys = nodeKeys;
  }

  @Override
  public Signature createCommitSealForBlock(final BlockHeader header) {
    final IbftExtraData extraData = IbftExtraData.decode(header);
    final Hash commitHash = IbftBlockHashing.calculateDataHashForCommittedSeal(header, extraData);
    return SECP256K1.sign(commitHash, nodeKeys);
  }

  @Override
  public Block createSealedBlock(final Block block, final Collection<Signature> commitSeals) {
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

  public Block replaceRoundInBlock(
      final Block block, final int round, final BlockHeaderFunctions blockHeaderFunctions) {
    final IbftExtraData prevExtraData = IbftExtraData.decode(block.getHeader());
    final IbftExtraData substituteExtraData =
        new IbftExtraData(
            prevExtraData.getVanityData(),
            prevExtraData.getSeals(),
            prevExtraData.getVote(),
            round,
            prevExtraData.getValidators());

    final BlockHeaderBuilder headerBuilder = BlockHeaderBuilder.fromHeader(block.getHeader());
    headerBuilder
        .extraData(substituteExtraData.encode())
        .blockHeaderFunctions(blockHeaderFunctions);

    final BlockHeader newHeader = headerBuilder.buildBlockHeader();

    return new Block(newHeader, block.getBody());
  }
}
