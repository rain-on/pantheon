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
package tech.pegasys.pantheon.consensus.ibft.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.TestHelpers;
import tech.pegasys.pantheon.consensus.ibft.blockcreation.ProposerSelector;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.NewRound;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.Proposal;
import tech.pegasys.pantheon.consensus.ibft.payload.MessageFactory;
import tech.pegasys.pantheon.consensus.ibft.payload.NewRoundPayload;
import tech.pegasys.pantheon.consensus.ibft.payload.RoundChangeCertificate;
import tech.pegasys.pantheon.consensus.ibft.payload.SignedData;
import tech.pegasys.pantheon.consensus.ibft.payload.SignedDataFactory;
import tech.pegasys.pantheon.consensus.ibft.statemachine.PreparedRoundArtifacts;
import tech.pegasys.pantheon.consensus.ibft.validation.RoundChangePayloadValidator.MessageValidatorForHeightFactory;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.Util;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

public class NewRoundSignedDataValidatorTest {

  private final KeyPair proposerKey = KeyPair.generate();
  private final KeyPair validatorKey = KeyPair.generate();
  private final KeyPair otherValidatorKey = KeyPair.generate();
  private final MessageFactory proposerMessageFactory =
      new MessageFactory(new SignedDataFactory(proposerKey));
  private final MessageFactory validatorMessageFactory =
      new MessageFactory(new SignedDataFactory(validatorKey));
  private final Address proposerAddress = Util.publicKeyToAddress(proposerKey.getPublicKey());
  private final List<Address> validators = Lists.newArrayList();
  private final long chainHeight = 2;
  private final ConsensusRoundIdentifier roundIdentifier =
      new ConsensusRoundIdentifier(chainHeight, 4);
  private NewRoundPayloadValidator validator;

  private final ProposerSelector proposerSelector = mock(ProposerSelector.class);
  private final MessageValidatorForHeightFactory validatorFactory =
      mock(MessageValidatorForHeightFactory.class);
  private final SignedDataValidator signedDataValidator = mock(SignedDataValidator.class);

  private Block proposedBlock;
  private SignedData<NewRoundPayload> signedPayload;
  private NewRoundPayload payload;
  private NewRoundPayload.Builder payloadBuilder;

  @Before
  public void setup() {
    validators.add(Util.publicKeyToAddress(proposerKey.getPublicKey()));
    validators.add(Util.publicKeyToAddress(validatorKey.getPublicKey()));
    validators.add(Util.publicKeyToAddress(otherValidatorKey.getPublicKey()));

    proposedBlock = TestHelpers.createProposalBlock(validators, roundIdentifier);
    signedPayload = createValidNewRoundMessageSignedBy(proposerKey);
    payload = signedPayload.getPayload();
    payloadBuilder = NewRoundPayload.Builder.fromExisting(payload);

    when(proposerSelector.selectProposerForRound(any())).thenReturn(proposerAddress);

    when(validatorFactory.createAt(any())).thenReturn(signedDataValidator);
    when(signedDataValidator.validateProposal(any())).thenReturn(true);
    when(signedDataValidator.validatePrepare(any())).thenReturn(true);

    validator =
        new NewRoundPayloadValidator(
            validators, proposerSelector, validatorFactory, 1, chainHeight);
  }

  /* NOTE: All test herein assume that the Proposer is the expected transmitter of the NewRound
   * message.
   */

  private SignedData<NewRoundPayload> createValidNewRoundMessageSignedBy(final KeyPair signingKey) {
    final SignedDataFactory messageCreator = new SignedDataFactory(signingKey);

    final RoundChangeCertificate.Builder builder = new RoundChangeCertificate.Builder();
    builder.appendRoundChangeMessage(
        proposerMessageFactory.createRoundChange(roundIdentifier, Optional.empty()));

    return messageCreator.createNewRound(
        roundIdentifier,
        builder.buildCertificate(),
        messageCreator.createProposal(roundIdentifier, proposedBlock));
  }

  private SignedData<NewRoundPayload> signPayload(
      final NewRoundPayload payload, final KeyPair signingKey) {

    final SignedDataFactory messageCreator = new SignedDataFactory(signingKey);

    return messageCreator.createNewRound(
        payload.getRoundIdentifier(),
        payload.getRoundChangeCertificate(),
        payload.getProposalPayload());
  }

  @Test
  public void basicNewRoundMessageIsValid() {
    assertThat(validator.validateNewRoundMessage(signedPayload)).isTrue();
  }

  @Test
  public void newRoundFromNonProposerFails() {
    final SignedData<NewRoundPayload> signedPayload = signPayload(payload, validatorKey);

    assertThat(validator.validateNewRoundMessage(signedPayload)).isFalse();
  }

  @Test
  public void newRoundTargetingRoundZeroFails() {
    payloadBuilder.setRoundChangeIdentifier(
        new ConsensusRoundIdentifier(roundIdentifier.getSequenceNumber(), 0));

    final SignedData<NewRoundPayload> invalidPayload =
        signPayload(payloadBuilder.build(), proposerKey);

    assertThat(validator.validateNewRoundMessage(invalidPayload)).isFalse();
  }

  @Test
  public void newRoundTargetingDifferentSequenceNumberFails() {
    final ConsensusRoundIdentifier futureRound = TestHelpers.createFrom(roundIdentifier, 1, 0);
    payloadBuilder.setRoundChangeIdentifier(futureRound);

    final SignedData<NewRoundPayload> invalidPayload =
        signPayload(payloadBuilder.build(), proposerKey);

    assertThat(validator.validateNewRoundMessage(invalidPayload)).isFalse();
  }

  @Test
  public void newRoundWithEmptyRoundChangeCertificateFails() {
    payloadBuilder.setRoundChangeCertificate(new RoundChangeCertificate(Collections.emptyList()));

    final SignedData<NewRoundPayload> invalidPayload =
        signPayload(payloadBuilder.build(), proposerKey);

    assertThat(validator.validateNewRoundMessage(invalidPayload)).isFalse();
  }

  @Test
  public void roundChangeMessagesDoNotAllTargetRoundOfNewRoundMsgFails() {
    final ConsensusRoundIdentifier prevRound = TestHelpers.createFrom(roundIdentifier, 0, -1);

    final RoundChangeCertificate.Builder roundChangeBuilder = new RoundChangeCertificate.Builder();
    roundChangeBuilder.appendRoundChangeMessage(
        proposerMessageFactory.createRoundChange(roundIdentifier, Optional.empty()));
    roundChangeBuilder.appendRoundChangeMessage(
        proposerMessageFactory.createRoundChange(prevRound, Optional.empty()));

    payloadBuilder.setRoundChangeCertificate(roundChangeBuilder.buildCertificate());

    final SignedData<NewRoundPayload> signedPayload =
        signPayload(payloadBuilder.build(), proposerKey);

    assertThat(validator.validateNewRoundMessage(signedPayload)).isFalse();
  }

  @Test
  public void invalidEmbeddedRoundChangeMessageFails() {
    final ConsensusRoundIdentifier prevRound = TestHelpers.createFrom(roundIdentifier, 0, -1);

    final RoundChangeCertificate.Builder roundChangeBuilder = new RoundChangeCertificate.Builder();
    roundChangeBuilder.appendRoundChangeMessage(
        proposerMessageFactory.createRoundChange(
            roundIdentifier,
            Optional.of(
                new PreparedRoundArtifacts(
                    proposerMessageFactory.createProposal(prevRound, proposedBlock),
                    Lists.newArrayList(
                        validatorMessageFactory.createPrepare(
                            prevRound, proposedBlock.getHash()))))));

    payloadBuilder.setRoundChangeCertificate(roundChangeBuilder.buildCertificate());

    // The prepare Message in the RoundChange Cert will be deemed illegal.
    when(signedDataValidator.validatePrepare(any())).thenReturn(false);

    final SignedData<NewRoundPayload> signedPayload =
        signPayload(payloadBuilder.build(), proposerKey);

    assertThat(validator.validateNewRoundMessage(signedPayload)).isFalse();
  }

  @Test
  public void embeddedProposalFailsValidation() {
    when(signedDataValidator.validateProposal(any())).thenReturn(false, true);

    final Proposal proposal = proposerMessageFactory.createProposal(roundIdentifier, proposedBlock);

    final NewRound msg =
        proposerMessageFactory.createNewRound(
            roundIdentifier,
            new RoundChangeCertificate(
                Lists.newArrayList(
                    proposerMessageFactory
                        .createRoundChange(roundIdentifier, Optional.empty())
                        .getSignedPayload(),
                    validatorMessageFactory
                        .createRoundChange(roundIdentifier, Optional.empty())
                        .getSignedPayload())),
            proposal.getSignedPayload(),
            proposedBlock);

    assertThat(validator.validateNewRoundMessage(msg.getSignedPayload())).isFalse();
  }
}
