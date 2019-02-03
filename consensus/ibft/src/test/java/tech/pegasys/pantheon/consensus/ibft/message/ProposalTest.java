package tech.pegasys.pantheon.consensus.ibft.message;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.pegasys.pantheon.consensus.ibft.TestHelpers.createProposalWithRound;

import org.junit.Test;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.Proposal;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.util.bytes.BytesValue;

public class ProposalTest {

  @Test
  public void serialisedMessageDeserialises() {
    final KeyPair keyPair = KeyPair.generate();

    final Proposal proposal = createProposalWithRound(keyPair, 1);

    final BytesValue serialisedBytes = proposal.encode();

    final Proposal deserialisedProposal = Proposal.decode(serialisedBytes);

    assertThat(proposal).isEqualToComparingFieldByField(deserialisedProposal);
  }
}
