package tech.pegasys.pantheon.consensus.ibft.payload;

import java.util.Optional;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Address;

public class RoundChangeMessage implements Message {

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
  public Address author() {
    return underlyingPayload.getSender();
  }

  @Override
  public Signature signature() {
    return underlyingPayload.getSignature();
  }

  public Optional<PreparedCertificate> getPreparedCertificate() {
    return underlyingPayload.getPayload().getPreparedCertificate();
  }
}
