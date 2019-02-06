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
package tech.pegasys.pantheon.consensus.ibft.statemachine;

import static java.util.Optional.empty;

import tech.pegasys.pantheon.consensus.ibft.messagewrappers.RoundChange;
import tech.pegasys.pantheon.consensus.ibft.payload.PreparedCertificate;
import tech.pegasys.pantheon.consensus.ibft.payload.RoundChangeCertificate;
import tech.pegasys.pantheon.consensus.ibft.payload.RoundChangePayload;
import tech.pegasys.pantheon.consensus.ibft.payload.SignedData;
import tech.pegasys.pantheon.ethereum.core.Block;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class RoundChangeArtifacts {

  private final Optional<Block> block;
  private final Collection<SignedData<RoundChangePayload>> roundChangePayloads;

  public RoundChangeArtifacts(
      final Optional<Block> block,
      final Collection<SignedData<RoundChangePayload>> roundChangePayloads) {
    this.block = block;
    this.roundChangePayloads = roundChangePayloads;
  }

  public Optional<Block> getBlock() {
    return block;
  }

  public RoundChangeCertificate getRoundChangeCertificate() {
    return new RoundChangeCertificate(roundChangePayloads);
  }

  public static RoundChangeArtifacts create(final Collection<RoundChange> roundChanges) {

    final Collection<SignedData<RoundChangePayload>> payloads =
        roundChanges.stream().map(RoundChange::getSignedPayload).collect(Collectors.toList());

    Optional<RoundChange> newestRoundChange = empty();
    for (final RoundChange roundChange : roundChanges) {
      if (roundChange.getPreparedCertificate().isPresent()) {
        if (!newestRoundChange.isPresent()) {
          newestRoundChange = Optional.of(roundChange);
        } else {
          newestRoundChange =
              Optional.of(selectNewestRoundChange(newestRoundChange.get(), roundChange));
        }
      }
    }
    if (!newestRoundChange.isPresent()) {
      return new RoundChangeArtifacts(empty(), payloads);
    } else {
      return new RoundChangeArtifacts(newestRoundChange.get().getProposedBlock(), payloads);
    }
  }

  private static RoundChange selectNewestRoundChange(
      final RoundChange first, final RoundChange second) {
    final PreparedCertificate firstCert = first.getPreparedCertificate().get();
    final PreparedCertificate secondCert = second.getPreparedCertificate().get();

    if (secondCert.getProposalPayload().getPayload().getRoundIdentifier().getRoundNumber()
        > firstCert.getProposalPayload().getPayload().getRoundIdentifier().getRoundNumber()) {
      return second;
    }
    return first;
  }
}
