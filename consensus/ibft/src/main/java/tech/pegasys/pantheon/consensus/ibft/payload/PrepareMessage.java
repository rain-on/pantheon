package tech.pegasys.pantheon.consensus.ibft.payload;

import javax.annotation.Signed;
import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Hash;

public class PrepareMessage implements Message {

  private final SignedData<PreparePayload> underlyingPayload;

  public PrepareMessage(
      final SignedData<PreparePayload> underlyingPayload) {
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

  public Hash getDigest() {
    return underlyingPayload.getPayload().getDigest();
  }
  
  public SignedData<PreparePayload> getRaw() { return underlyingPayload; }
}
