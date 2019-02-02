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
package tech.pegasys.pantheon.consensus.ibft.messagewrappers;

import java.util.Optional;
import tech.pegasys.pantheon.consensus.ibft.IbftBlockHashing;
import tech.pegasys.pantheon.consensus.ibft.payload.NewRoundPayload;
import tech.pegasys.pantheon.consensus.ibft.payload.PreparedCertificate;
import tech.pegasys.pantheon.consensus.ibft.payload.RoundChangePayload;
import tech.pegasys.pantheon.consensus.ibft.payload.SignedData;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.rlp.BytesValueRLPOutput;
import tech.pegasys.pantheon.ethereum.rlp.RLP;
import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.util.bytes.BytesValue;

public class RoundChange extends IbftMessage<RoundChangePayload> {

  final private Optional<Block> proposedBlock;

  public RoundChange(final SignedData<RoundChangePayload> payload,
      final Optional<Block> proposedBlock) {
    super(payload);
    this.proposedBlock = proposedBlock;
  }

  public Optional<Block> getProposedBlock() {
    return proposedBlock;
  }

  public Optional<PreparedCertificate> getPreparedCertificate() {
    return getPayload().getPreparedCertificate();
  }

  @Override
  public BytesValue encode() {
    final BytesValueRLPOutput rlpOut = new BytesValueRLPOutput();
    rlpOut.startList();
    rlpOut.writeBytesValue(getSignedPayload().encode());
    if(proposedBlock.isPresent()) {
      rlpOut.writeBytesValue(proposedBlock.get().toRlp());
    }
    else {
      rlpOut.writeNull();
    }
    rlpOut.endList();
    return rlpOut.encoded();
  }

  public static RoundChange decode(final BytesValue data) {
    RLPInput rlpIn = RLP.input(data);
    rlpIn.enterList();
    final SignedData<RoundChangePayload> payload =
        SignedData.readSignedRoundChangePayloadFrom(rlpIn);
    Optional<Block> proposedBlock = Optional.empty();
    if (!rlpIn.nextIsNull()) {
      proposedBlock =
          Optional.of(Block.readFrom(rlpIn, IbftBlockHashing::calculateDataHashForCommittedSeal));
    }
    rlpIn.leaveList();
    return new RoundChange(payload, proposedBlock);
  }
}
