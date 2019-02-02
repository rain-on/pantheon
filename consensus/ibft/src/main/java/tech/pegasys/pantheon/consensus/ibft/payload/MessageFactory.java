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
import tech.pegasys.pantheon.consensus.ibft.statemachine.TerminatedRoundArtefacts;
import tech.pegasys.pantheon.crypto.SECP256K1;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.util.bytes.BytesValues;

import java.util.Optional;

public class MessageFactory {

  private final KeyPair validatorKeyPair;

  public MessageFactory(final KeyPair validatorKeyPair) {
    this.validatorKeyPair = validatorKeyPair;
  }

  public Proposal createSignedProposalPayload(
      final ConsensusRoundIdentifier roundIdentifier, final Block block) {

    final ProposalPayload payload = new ProposalPayload(roundIdentifier, block.getHash());

    return new Proposal(createSignedMessage(payload), block);
  }

  public Prepare createSignedPreparePayload(
      final ConsensusRoundIdentifier roundIdentifier, final Hash digest) {

    final PreparePayload payload = new PreparePayload(roundIdentifier, digest);

    return new Prepare(createSignedMessage(payload));
  }

  public Commit createSignedCommitPayload(
      final ConsensusRoundIdentifier roundIdentifier,
      final Hash digest,
      final Signature commitSeal) {

    final CommitPayload payload = new CommitPayload(roundIdentifier, digest, commitSeal);

    return new Commit(createSignedMessage(payload));
  }

  public RoundChange createSignedRoundChangePayload(
      final ConsensusRoundIdentifier roundIdentifier,
      final Optional<TerminatedRoundArtefacts> artefacts) {

    if (artefacts.isPresent()) {
      final RoundChangePayload payload = new RoundChangePayload(roundIdentifier,
          Optional.of(artefacts.get().getPreparedCertificate()));
      return new RoundChange(createSignedMessage(payload), Optional.of(artefacts.get().getBlock()));
    }

    final RoundChangePayload payload = new RoundChangePayload(roundIdentifier, Optional.empty());
    return new RoundChange(createSignedMessage(payload), Optional.empty());

  }

  public NewRound createSignedNewRoundPayload(
      final ConsensusRoundIdentifier roundIdentifier,
      final RoundChangeCertificate roundChangeCertificate,
      final SignedData<ProposalPayload> proposalPayload,
      final Block proposedBlock) {

    final NewRoundPayload payload =
        new NewRoundPayload(roundIdentifier, roundChangeCertificate, proposalPayload);

    return new NewRound(createSignedMessage(payload), proposedBlock);
  }

  private <M extends Payload> SignedData<M> createSignedMessage(final M payload) {
    final Signature signature = sign(payload, validatorKeyPair);

    return new SignedData<>(
        payload, Util.publicKeyToAddress(validatorKeyPair.getPublicKey()), signature);
  }

  public static Hash hashForSignature(final Payload unsignedMessageData) {
    return Hash.hash(
        BytesValues.concatenate(
            BytesValues.ofUnsignedByte(unsignedMessageData.getMessageType()),
            unsignedMessageData.encoded()));
  }

  private static Signature sign(final Payload unsignedMessageData, final KeyPair nodeKeys) {
    return SECP256K1.sign(hashForSignature(unsignedMessageData), nodeKeys);
  }
}
