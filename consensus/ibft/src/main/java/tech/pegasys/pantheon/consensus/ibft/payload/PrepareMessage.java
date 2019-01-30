package tech.pegasys.pantheon.consensus.ibft.payload;

import javax.annotation.Signed;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Hash;

public class PrepareMessage implements Message {

  private final SignedData<PreparePayload> underlyingPayload;

  public PrepareMessage(
      SignedData<PreparePayload> underlyingPayload) {
    this.underlyingPayload = underlyingPayload;
  }

  @Override
  public int getRound() {
    return 0;
  }

  @Override
  public long getSequence() {
    return 0;
  }

  @Override
  public Address author() {
    return null;
  }

  public Hash getDigest() {
    return underlyingPayload.getPayload().getDigest();
  }
  
  public SignedData<PreparePayload> getRaw() { return underlyingPayload; }
}
