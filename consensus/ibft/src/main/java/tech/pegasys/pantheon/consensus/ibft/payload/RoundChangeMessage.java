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
package tech.pegasys.pantheon.consensus.ibft.payload;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Address;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

public class RoundChangeMessage implements IbftMessage {

  private final SignedData<RoundChangePayload> underlyingPayload;

  public RoundChangeMessage(final SignedData<RoundChangePayload> underlyingPayload) {
    this.underlyingPayload = underlyingPayload;
  }

  @Override
  public int getRound() {
    return underlyingPayload.getPayload().getRoundIdentifier().getRoundNumber();
  }

  @Override
  public long getSequence() {
    return underlyingPayload.getPayload().getRoundIdentifier().getSequenceNumber();
  }

  @Override
  public ConsensusRoundIdentifier getConsensusRound() {
    return underlyingPayload.getPayload().getRoundIdentifier();
  }

  @Override
  public Address getAuthor() {
    return underlyingPayload.getSender();
  }

  @Override
  public Signature getSignature() {
    return underlyingPayload.getSignature();
  }

  @Override
  public int getMessageType() {
    return underlyingPayload.getPayload().getMessageType();
  }

  public Optional<PreparedCertificate> getPreparedCertificate() {
    return underlyingPayload.getPayload().getPreparedCertificate();
  }

  public SignedData<RoundChangePayload> getRaw() {
    return underlyingPayload;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", RoundChangeMessage.class.getSimpleName() + "[", "]")
        .add("underlyingPayload=" + underlyingPayload)
        .toString();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final RoundChangeMessage that = (RoundChangeMessage) o;
    return Objects.equals(underlyingPayload, that.underlyingPayload);
  }

  @Override
  public int hashCode() {
    return Objects.hash(underlyingPayload);
  }
}
