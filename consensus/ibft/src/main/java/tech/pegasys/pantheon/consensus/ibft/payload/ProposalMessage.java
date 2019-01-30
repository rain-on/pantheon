package tech.pegasys.pantheon.consensus.ibft.payload;

import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Block;

public class ProposalMessage implements Message {

  private final SignedData<ProposalPayload> underlyingPayload;

  public ProposalMessage(
      SignedData<ProposalPayload> underlyingPayload) {
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

  public Block getBlock() {
    return underlyingPayload.getPayload().getBlock();
  }
}
