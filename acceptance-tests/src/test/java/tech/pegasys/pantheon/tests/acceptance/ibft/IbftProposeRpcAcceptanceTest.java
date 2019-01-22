package tech.pegasys.pantheon.tests.acceptance.ibft;

import java.io.IOException;
import org.junit.Test;
import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;
import tech.pegasys.pantheon.tests.acceptance.dsl.waitcondition.WaitCondition;

// These tests prove the ibft_proposeValidatorVote and ibft_getValidatorsByBlockNumber (implicitly)
// JSON RPC calls.
public class IbftProposeRpcAcceptanceTest extends AcceptanceTestBase {

  @Test
  public void validatorsCanBeAddedAndThenRemoved() throws IOException {
    final String[] validators = {"validator1", "validator2", "validator3"};
    final PantheonNode validator1 = pantheon.createIbftNodeWithValidators("validator1", validators);
    final PantheonNode validator2 = pantheon.createIbftNodeWithValidators("validator2", validators);
    final PantheonNode validator3 = pantheon.createIbftNodeWithValidators("validator3", validators);
    final PantheonNode nonValidatorNode =
        pantheon.createIbftNodeWithValidators("non-validator", validators);
    cluster.start(validator1, validator2, validator3, nonValidatorNode);

    cluster.verify(ibft.validatorsEqual(validator1, validator2, validator3));
    final WaitCondition addedCondition = wait.ibftValidatorsChanged(validator1);
    validator1.execute(ibftTransactions.createAddProposal(nonValidatorNode));
    validator2.execute(ibftTransactions.createAddProposal(nonValidatorNode));

    cluster.waitUntil(addedCondition);
    cluster.verify(ibft.validatorsEqual(validator1, validator2, validator3, nonValidatorNode));

    final WaitCondition removedCondition = wait.ibftValidatorsChanged(validator1);
    validator2.execute(ibftTransactions.createRemoveProposal(nonValidatorNode));
    validator3.execute(ibftTransactions.createRemoveProposal(nonValidatorNode));
    nonValidatorNode.execute(ibftTransactions.createRemoveProposal(nonValidatorNode));
    cluster.waitUntil(removedCondition);
    cluster.verify(ibft.validatorsEqual(validator1, validator2, validator3));
  }
}
