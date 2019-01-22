/*
 * Copyright 2019 ConsenSys AG.
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
package tech.pegasys.pantheon.tests.acceptance.dsl.jsonrpc;

import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.clique.ExpectValidators;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.clique.CliqueTransactions;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.eth.EthTransactions;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.ibft.IbftTransactions;

public class Ibft {

  private final EthTransactions eth;
  private final IbftTransactions ibft;

  public Ibft(final EthTransactions eth, final IbftTransactions ibft) {
    this.eth = eth;
    this.ibft = ibft;
  }

  public List<PantheonNode> validators(final PantheonNode[] nodes) {
    final Comparator<PantheonNode> compareByAddress =
        Comparator.comparing(PantheonNode::getAddress);
    List<PantheonNode> pantheonNodes = Arrays.asList(nodes);
    pantheonNodes.sort(compareByAddress);
    return pantheonNodes;
  }

  public ExpectValidators validatorsEqual(final PantheonNode... validators) {
    return new ExpectValidators(ibft, validatorAddresses(validators));
  }

  private Address[] validatorAddresses(final PantheonNode[] validators) {
    return Arrays.stream(validators).map(PantheonNode::getAddress).sorted().toArray(Address[]::new);
  }
}
