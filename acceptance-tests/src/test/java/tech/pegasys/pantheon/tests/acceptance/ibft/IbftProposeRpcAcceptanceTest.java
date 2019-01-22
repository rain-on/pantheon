package tech.pegasys.pantheon.tests.acceptance.ibft;

import java.io.IOException;
import org.junit.Test;
import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;

public class IbftProposeRpcAcceptanceTest extends AcceptanceTestBase {

  @Test
  public void shouldAddNewValidators() throws IOException {
    final String[] validators = {"miner1", "miner2", "miner3"};
    final PantheonNode minerNode1 = pantheon.createIbftNode("miner1");
    final PantheonNode minerNode2 = pantheon.createIbftNode("miner2");
    final PantheonNode minerNode3 = pantheon.createIbftNode("miner3");
    final PantheonNode nonValidatorNode =
        pantheon.createIbftNodeWithValidators("non-validator", validators);
    cluster.start(minerNode1, minerNode2, minerNode3, nonValidatorNode);

    cluster.verify(ibft.validatorsEqual(minerNode1, minerNode2));



  }

}
