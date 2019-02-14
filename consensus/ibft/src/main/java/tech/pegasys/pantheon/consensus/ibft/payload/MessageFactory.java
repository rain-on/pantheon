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
package tech.pegasys.pantheon.consensus.ibft.payload;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.Commit;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.NewRound;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.Prepare;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.Proposal;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.RoundChange;
import tech.pegasys.pantheon.consensus.ibft.statemachine.PreparedRoundArtifacts;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.Hash;

import java.util.Optional;

public class MessageFactory {

  private final SignedDataFactory signedDataFactory;

  public MessageFactory(final SignedDataFactory signedDataFactory) {
    this.signedDataFactory = signedDataFactory;
  }

  public Proposal createProposal(
      final ConsensusRoundIdentifier roundIdentifier, final Block block) {
    return new Proposal(signedDataFactory.createProposal(roundIdentifier, block), block);
  }

  public Prepare createPrepare(final ConsensusRoundIdentifier roundIdentifier, final Hash digest) {
    return new Prepare(signedDataFactory.createPrepare(roundIdentifier, digest));
  }

  public Commit createCommit(
      final ConsensusRoundIdentifier roundIdentifier,
      final Hash digest,
      final Signature commitSeal) {

    return new Commit(signedDataFactory.createCommit(roundIdentifier, digest, commitSeal));
  }

  public RoundChange createRoundChange(
      final ConsensusRoundIdentifier roundIdentifier,
      final Optional<PreparedRoundArtifacts> preparedRoundArtifacts) {

    final SignedData<RoundChangePayload> payload =
        signedDataFactory.createRoundChange(
            roundIdentifier,
            preparedRoundArtifacts.map(PreparedRoundArtifacts::getPreparedCertificate));

    return new RoundChange(payload, preparedRoundArtifacts.map(PreparedRoundArtifacts::getBlock));
  }

  public NewRound createNewRound(
      final ConsensusRoundIdentifier roundIdentifier,
      final RoundChangeCertificate roundChangeCertificate,
      final SignedData<ProposalPayload> proposalPayload,
      final Block block) {

    final SignedData<NewRoundPayload> payload =
        signedDataFactory.createNewRound(roundIdentifier, roundChangeCertificate, proposalPayload);

    return new NewRound(payload, block);
  }
}
