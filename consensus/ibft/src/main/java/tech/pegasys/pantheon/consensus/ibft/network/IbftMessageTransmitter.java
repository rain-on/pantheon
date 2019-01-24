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
package tech.pegasys.pantheon.consensus.ibft.network;

import static java.util.Collections.emptyList;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.Gossiper;
import tech.pegasys.pantheon.consensus.ibft.messagedata.CommitMessageData;
import tech.pegasys.pantheon.consensus.ibft.messagedata.NewRoundMessageData;
import tech.pegasys.pantheon.consensus.ibft.messagedata.PrepareMessageData;
import tech.pegasys.pantheon.consensus.ibft.messagedata.ProposalMessageData;
import tech.pegasys.pantheon.consensus.ibft.messagedata.RoundChangeMessageData;
import tech.pegasys.pantheon.consensus.ibft.payload.CommitPayload;
import tech.pegasys.pantheon.consensus.ibft.payload.MessageFactory;
import tech.pegasys.pantheon.consensus.ibft.payload.NewRoundPayload;
import tech.pegasys.pantheon.consensus.ibft.payload.PreparePayload;
import tech.pegasys.pantheon.consensus.ibft.payload.PreparedCertificate;
import tech.pegasys.pantheon.consensus.ibft.payload.ProposalPayload;
import tech.pegasys.pantheon.consensus.ibft.payload.RoundChangeCertificate;
import tech.pegasys.pantheon.consensus.ibft.payload.RoundChangePayload;
import tech.pegasys.pantheon.consensus.ibft.payload.SignedData;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.Hash;

import java.util.Optional;

public class IbftMessageTransmitter {

  private final MessageFactory messageFactory;
  private final Gossiper network;

  public IbftMessageTransmitter(final MessageFactory messageFactory, final Gossiper network) {
    this.messageFactory = messageFactory;
    this.network = network;
  }

  public void multicastProposal(final ConsensusRoundIdentifier roundIdentifier, final Block block) {
    final SignedData<ProposalPayload> signedPayload =
        messageFactory.createSignedProposalPayload(roundIdentifier, block);

    final ProposalMessageData message = ProposalMessageData.create(signedPayload);

    network.send(message, emptyList());
  }

  public void multicastPrepare(final ConsensusRoundIdentifier roundIdentifier, final Hash digest) {
    final SignedData<PreparePayload> signedPayload =
        messageFactory.createSignedPreparePayload(roundIdentifier, digest);

    final PrepareMessageData message = PrepareMessageData.create(signedPayload);

    network.send(message, emptyList());
  }

  public void multicastCommit(
      final ConsensusRoundIdentifier roundIdentifier,
      final Hash digest,
      final Signature commitSeal) {
    final SignedData<CommitPayload> signedPayload =
        messageFactory.createSignedCommitPayload(roundIdentifier, digest, commitSeal);

    final CommitMessageData message = CommitMessageData.create(signedPayload);

    network.send(message, emptyList());
  }

  public void multicastRoundChange(
      final ConsensusRoundIdentifier roundIdentifier,
      final Optional<PreparedCertificate> preparedCertificate) {

    final SignedData<RoundChangePayload> signedPayload =
        messageFactory.createSignedRoundChangePayload(roundIdentifier, preparedCertificate);

    final RoundChangeMessageData message = RoundChangeMessageData.create(signedPayload);

    network.send(message, emptyList());
  }

  public void multicastNewRound(
      final ConsensusRoundIdentifier roundIdentifier,
      final RoundChangeCertificate roundChangeCertificate,
      final SignedData<ProposalPayload> proposalPayload) {

    final SignedData<NewRoundPayload> signedPayload =
        messageFactory.createSignedNewRoundPayload(
            roundIdentifier, roundChangeCertificate, proposalPayload);

    final NewRoundMessageData message = NewRoundMessageData.create(signedPayload);

    network.send(message, emptyList());
  }
}
