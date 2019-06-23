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
package tech.pegasys.pantheon.consensus.ibftlegacy;

import java.util.function.Function;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderFunctions;
import tech.pegasys.pantheon.ethereum.core.Hash;

public class LegacyIbftBlockHeaderFunctions implements BlockHeaderFunctions {

  private static final LegacyIbftBlockHeaderFunctions COMMITTED_SEAL =
      new LegacyIbftBlockHeaderFunctions(IbftBlockHashing::calculateDataHashForCommittedSeal);

  private final Function<BlockHeader, Hash> hashFunction;

  private LegacyIbftBlockHeaderFunctions(final Function<BlockHeader, Hash> hashFunction) {
    this.hashFunction = hashFunction;
  }

  private static final LegacyIbftBlockHeaderFunctions ON_CHAIN =
      new LegacyIbftBlockHeaderFunctions(IbftBlockHashing::calculateHashOfIbftBlockOnChain);

  public static BlockHeaderFunctions forOnChainBlock() {
    return ON_CHAIN;
  }

  @Override
  public Hash hash(final BlockHeader header) {
    return hashFunction.apply(header);
  }

  @Override
  public IbftExtraData parseExtraData(final BlockHeader header) {
    return IbftExtraData.decodeRaw(header.getExtraData());
  }

  public static BlockHeaderFunctions forCommittedSeal() {
    return COMMITTED_SEAL;
  }
}
