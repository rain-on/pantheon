package tech.pegasys.pantheon.consensus.ibft.payload;

import tech.pegasys.pantheon.ethereum.core.Address;

public interface Message {

  ///////////// EXPOSED ON BASE INTERFACE
  int getRound();

  long getSequence();

  Address author();

  Signature signature();
}
