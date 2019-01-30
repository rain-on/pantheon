package tech.pegasys.pantheon.consensus.ibft.payload;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Address;

public interface IbftMessage {

  ///////////// EXPOSED ON BASE INTERFACE
  int getRound();

  long getSequence();

  ConsensusRoundIdentifier getConsensusRound();

  Address getAuthor();

  Signature getSignature();

  long getMessageType();
}
