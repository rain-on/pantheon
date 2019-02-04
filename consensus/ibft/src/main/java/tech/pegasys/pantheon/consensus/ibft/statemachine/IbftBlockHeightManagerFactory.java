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
package tech.pegasys.pantheon.consensus.ibft.statemachine;

import tech.pegasys.pantheon.consensus.ibft.IbftHelpers;
import tech.pegasys.pantheon.consensus.ibft.validation.MessageValidatorFactory;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;

public class IbftBlockHeightManagerFactory {

  private final IbftRoundFactory roundFactory;
  private final IbftFinalState finalState;
  private final MessageValidatorFactory messageValidatorFactory;

  public IbftBlockHeightManagerFactory(
      final IbftFinalState finalState,
      final IbftRoundFactory roundFactory,
      final MessageValidatorFactory messageValidatorFactory) {
    this.roundFactory = roundFactory;
    this.finalState = finalState;
    this.messageValidatorFactory = messageValidatorFactory;
  }

  public BlockHeightManager create(final BlockHeader parentHeader) {
    if (finalState.isLocalNodeValidator()) {
      return createFullBlockHeightManager(parentHeader);
    } else {
      return createNoOpBlockHeightManager(parentHeader);
    }
  }

  private BlockHeightManager createNoOpBlockHeightManager(final BlockHeader parentHeader) {
    return new NoOpBlockHeightManager(parentHeader);
  }

  private BlockHeightManager createFullBlockHeightManager(final BlockHeader parentHeader) {
    return new IbftBlockHeightManager(
        parentHeader,
        finalState,
        new RoundChangeManager(
            IbftHelpers.calculateRequiredValidatorQuorum(finalState.getValidators().size()),
            messageValidatorFactory.createRoundChangeMessageValidator(
                parentHeader.getNumber() + 1)),
        roundFactory,
        finalState.getClock(),
        messageValidatorFactory);
  }
}
