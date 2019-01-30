package tech.pegasys.pantheon.consensus.ibft.payload;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Hash;

public class CommitMessage implements IbftMessage { //implement (expose RoundChange and

  private final SignedData<CommitPayload> underlyingPayload;

  public CommitMessage(
      final SignedData<CommitPayload> underlyingPayload) {
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

  public Signature getCommitSeal() {
    return underlyingPayload.getPayload().getCommitSeal();
  }

  public SignedData<CommitPayload> getRaw() { return underlyingPayload; }

  public long getMessageType() { return underlyingPayload.getPayload().getMessageType(); }

}
