/*
 * Copyright 2018 ConsenSys AG.
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
package tech.pegasys.pantheon.consensus.ibft.messagedata;

import tech.pegasys.pantheon.consensus.ibft.messagewrappers.NewRound;
import tech.pegasys.pantheon.consensus.ibft.payload.SignedData;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;
import tech.pegasys.pantheon.ethereum.rlp.RLP;
import tech.pegasys.pantheon.util.bytes.BytesValue;

public class NewRoundMessageData extends AbstractIbftMessageData {

  private static final int MESSAGE_CODE = IbftV2.NEW_ROUND;

  private NewRoundMessageData(final BytesValue data) {
    super(data);
  }

  public static NewRoundMessageData fromMessageData(final MessageData messageData) {
    return fromMessageData(
        messageData, MESSAGE_CODE, NewRoundMessageData.class, NewRoundMessageData::new);
  }

  public NewRound decode() {
    return NewRound.decode(data);
  }

  public static NewRoundMessageData create(final NewRound newRound) {
    return new NewRoundMessageData(newRound.encode());
  }

  @Override
  public int getCode() {
    return MESSAGE_CODE;
  }
}
