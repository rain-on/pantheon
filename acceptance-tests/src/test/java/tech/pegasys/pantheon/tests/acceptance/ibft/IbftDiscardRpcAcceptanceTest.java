package tech.pegasys.pantheon.tests.acceptance.ibft;

import java.io.IOException;
import org.junit.Test;
import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;

public class IbftDiscardRpcAcceptanceTest extends AcceptanceTestBase {

  @Test
  public void shouldDiscardVotes() throws IOException {
    final String[] validators = {"validator1", "validator2"};
    final PantheonNode validator1 = pantheon.createIbftNodeWithValidators("validator1", validators);
    final PantheonNode validator2 = pantheon.createIbftNodeWithValidators("validator2", validators);
    final PantheonNode validator3 = pantheon.createIbftNodeWithValidators("validator3", validators);
    cluster.start(validator1, validator2, validator3);
    

    validator1.execute(ibftTransactions.createRemoveProposal(validator2));
    validator1.execute(ibftTransactions.createAddProposal(validator3));

    validator2.execute(ibftTransactions.createRemoveProposal(validator2));
    validator2.execute(ibftTransactions.createAddProposal(validator3));

    validator1.execute(ibftTransactions.createDiscardProposal(validator2));
    validator1.execute(ibftTransactions.createDiscardProposal(validator3));

    validator1.waitUntil(wait.chainHeadHasProgressed(validator1, 2));

    cluster.verify(ibft.validatorsEqual(validator1, validator2));
    validator1.verify(ibft.noProposals());
    validator2.verify(
        ibft.proposalsEqual().removeProposal(validator2).addProposal(validator3).build());
  }
}
