package tech.pegasys.pantheon.consensus.ibft.payload;

import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Address;

public class NewRoundMessage implements Message {

  private final SignedData<NewRoundPayload> underlyingPayload;

  public NewRoundMessage(
      SignedData<NewRoundPayload> underlyingPayload) {
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
  public Address getAuthor() {
    return underlyingPayload.getSender();
  }

  @Override
  public Signature getSignature() {
    return underlyingPayload.getSignature();
  }

  public RoundChangeCertificate getRoundChangeCertificate() {
    return underlyingPayload.getPayload().getRoundChangeCertificate();
  }

  public ProposalMessage getProposalMessage() {
    return new ProposalMessage(underlyingPayload.getPayload().getProposalPayload());
  }
}
