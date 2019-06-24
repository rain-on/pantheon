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

import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;

import java.util.Collection;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderFunctions;

public interface BlockOperations {

  Signature createCommitSealForBlock(final BlockHeader header);

  Block createSealedBlock(final Block block, final Collection<Signature> commitSeals);

  Block replaceRoundInBlock(
      final Block block, final int round, final BlockHeaderFunctions blockHeaderFunctions);
}
