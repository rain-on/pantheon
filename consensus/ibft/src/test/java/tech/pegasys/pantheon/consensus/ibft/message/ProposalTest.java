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
package tech.pegasys.pantheon.consensus.ibft.message;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.pegasys.pantheon.consensus.ibft.TestHelpers.createProposalWithRound;

import tech.pegasys.pantheon.consensus.ibft.messagewrappers.Proposal;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import org.junit.Test;

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
