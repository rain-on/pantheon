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
package tech.pegasys.pantheon.consensus.ibft.payload;

import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.ethereum.rlp.BytesValueRLPOutput;
import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.ethereum.rlp.RLPOutput;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.Objects;
import java.util.StringJoiner;

public class SignedData<M extends Payload> {
  private final Address sender;
  private final Signature signature;
  private final M unsignedPayload;

  public SignedData(final M unsignedPayload, final Address sender, final Signature signature) {
    this.unsignedPayload = unsignedPayload;
    this.sender = sender;
    this.signature = signature;
  }

  public Address getSender() {
    return sender;
  }

  public Signature getSignature() {
    return signature;
  }

  public M getPayload() {
    return unsignedPayload;
  }

  public void writeTo(final RLPOutput output) {

    output.startList();
    unsignedPayload.writeTo(output);
    output.writeBytesValue(getSignature().encodedBytes());
    output.endList();
  }

  public BytesValue encode() {
    final BytesValueRLPOutput rlpEncode = new BytesValueRLPOutput();
    writeTo(rlpEncode);
    return rlpEncode.encoded();
  }

  public static SignedData<ProposalPayload> readSignedProposalPayloadFrom(final RLPInput rlpInput) {

    rlpInput.enterList();
    final ProposalPayload unsignedMessageData = ProposalPayload.readFrom(rlpInput);
    final Signature signature = readSignature(rlpInput);
    rlpInput.leaveList();

    return from(unsignedMessageData, signature);
  }

  public static SignedData<PreparePayload> readSignedPreparePayloadFrom(final RLPInput rlpInput) {

    rlpInput.enterList();
    final PreparePayload unsignedMessageData = PreparePayload.readFrom(rlpInput);
    final Signature signature = readSignature(rlpInput);
    rlpInput.leaveList();

    return from(unsignedMessageData, signature);
  }

  public static SignedData<CommitPayload> readSignedCommitPayloadFrom(final RLPInput rlpInput) {

    rlpInput.enterList();
    final CommitPayload unsignedMessageData = CommitPayload.readFrom(rlpInput);
    final Signature signature = readSignature(rlpInput);
    rlpInput.leaveList();

    return from(unsignedMessageData, signature);
  }

  public static SignedData<RoundChangePayload> readSignedRoundChangePayloadFrom(
      final RLPInput rlpInput) {

    rlpInput.enterList();
    final RoundChangePayload unsignedMessageData = RoundChangePayload.readFrom(rlpInput);
    final Signature signature = readSignature(rlpInput);
    rlpInput.leaveList();

    return from(unsignedMessageData, signature);
  }

  public static SignedData<NewRoundPayload> readSignedNewRoundPayloadFrom(final RLPInput rlpInput) {

    rlpInput.enterList();
    final NewRoundPayload unsignedMessageData = NewRoundPayload.readFrom(rlpInput);
    final Signature signature = readSignature(rlpInput);
    rlpInput.leaveList();

    return from(unsignedMessageData, signature);
  }

  protected static <M extends Payload> SignedData<M> from(
      final M unsignedMessageData, final Signature signature) {

    final Address sender = recoverSender(unsignedMessageData, signature);

    return new SignedData<>(unsignedMessageData, sender, signature);
  }

  protected static Signature readSignature(final RLPInput signedMessage) {
    return signedMessage.readBytesValue(Signature::decode);
  }

  protected static Address recoverSender(
      final Payload unsignedMessageData, final Signature signature) {

    return Util.signatureToAddress(signature, MessageFactory.hashForSignature(unsignedMessageData));
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final SignedData<?> that = (SignedData<?>) o;
    return Objects.equals(sender, that.sender)
        && Objects.equals(signature, that.signature)
        && Objects.equals(unsignedPayload, that.unsignedPayload);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sender, signature, unsignedPayload);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", SignedData.class.getSimpleName() + "[", "]")
        .add("sender=" + sender)
        .add("signature=" + signature)
        .add("unsignedPayload=" + unsignedPayload)
        .toString();
  }
}
