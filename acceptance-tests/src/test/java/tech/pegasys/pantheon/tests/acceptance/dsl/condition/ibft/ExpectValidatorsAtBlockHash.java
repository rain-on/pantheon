/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.tests.acceptance.dsl.condition.ibft;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.pegasys.pantheon.tests.acceptance.dsl.WaitUtils.waitFor;

import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.Condition;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.Node;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.ibft.IbftTransactions;

public class ExpectValidatorsAtBlockHash implements Condition {
  private final IbftTransactions ibft;
  private final Hash blockHash;
  private final Address[] validators;

  public ExpectValidatorsAtBlockHash(
      final IbftTransactions ibft, final Hash blockHash, final Address... validators) {
    this.ibft = ibft;
    this.blockHash = blockHash;
    this.validators = validators;
  }

  @Override
  public void verify(final Node node) {
    waitFor(
        () ->
            assertThat(node.execute(ibft.createGetValidatorsAtHash(blockHash)))
                .containsExactly(validators));
  }
}
