package tech.pegasys.pantheon.consensus.ibft.statemachine;

import java.util.Collection;
import java.util.stream.Collectors;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.Prepare;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.Proposal;
import tech.pegasys.pantheon.consensus.ibft.payload.PreparedCertificate;
import tech.pegasys.pantheon.ethereum.core.Block;

public class TerminatedRoundArtefacts {

  private Proposal proposal;
  private Collection<Prepare> prepares;

  public TerminatedRoundArtefacts(
      Proposal proposal,
      Collection<Prepare> prepares) {
    this.proposal = proposal;
    this.prepares = prepares;
  }

  public Block getBlock() {
    return proposal.getBlock();
  }

  public PreparedCertificate getPreparedCertificate() {
    return new PreparedCertificate(
        proposal.getSignedPayload(),
        prepares
            .stream()
            .map(Prepare::getSignedPayload)
            .collect(Collectors.toList()));
  }
}
