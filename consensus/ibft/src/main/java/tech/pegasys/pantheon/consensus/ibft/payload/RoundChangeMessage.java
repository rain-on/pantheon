package tech.pegasys.pantheon.consensus.ibft.payload;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Address;

public class RoundChangeMessage implements IbftMessage {

  private final SignedData<RoundChangePayload> underlyingPayload;

  public RoundChangeMessage(
      SignedData<RoundChangePayload> underlyingPayload) {
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

  public Optional<PreparedCertificate> getPreparedCertificate() {
    return underlyingPayload.getPayload().getPreparedCertificate();
  }

  public SignedData<RoundChangePayload> getRaw() { return underlyingPayload; }

  public int getMessageType() { return underlyingPayload.getPayload().getMessageType(); }

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
