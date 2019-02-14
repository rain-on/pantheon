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

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.TestHelpers;
import tech.pegasys.pantheon.consensus.ibft.payload.CommitPayload;
import tech.pegasys.pantheon.consensus.ibft.payload.PreparePayload;
import tech.pegasys.pantheon.consensus.ibft.payload.ProposalPayload;
import tech.pegasys.pantheon.consensus.ibft.payload.SignedData;
import tech.pegasys.pantheon.consensus.ibft.payload.SignedDataFactory;
import tech.pegasys.pantheon.crypto.SECP256K1;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.Util;

import java.util.List;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SignedDataValidatorTest {

  private final KeyPair proposerKey = KeyPair.generate();
  private final KeyPair validatorKey = KeyPair.generate();
  private final KeyPair nonValidatorKey = KeyPair.generate();
  private final SignedDataFactory proposerDataFactory = new SignedDataFactory(proposerKey);
  private final SignedDataFactory validatorDataFactory = new SignedDataFactory(validatorKey);
  private final SignedDataFactory nonValidatorDataFactory = new SignedDataFactory(nonValidatorKey);

  private final List<Address> validators = Lists.newArrayList();

  private final ConsensusRoundIdentifier roundIdentifier = new ConsensusRoundIdentifier(2, 0);
  private SignedDataValidator validator;

  private final Block block = mock(Block.class);

  @Before
  public void setup() {
    validators.add(Util.publicKeyToAddress(proposerKey.getPublicKey()));
    validators.add(Util.publicKeyToAddress(validatorKey.getPublicKey()));

    validator =
        new SignedDataValidator(
            validators, Util.publicKeyToAddress(proposerKey.getPublicKey()), roundIdentifier);

    when(block.getHash()).thenReturn(Hash.fromHexStringLenient("1"));
  }

  @Test
  public void receivingAPrepareMessageBeforeProposalFails() {
    final SignedData<PreparePayload> prepareMsg =
        proposerDataFactory.createPrepare(roundIdentifier, Hash.ZERO);

    assertThat(validator.validatePrepare(prepareMsg)).isFalse();
  }

  @Test
  public void receivingACommitMessageBeforeProposalFails() {
    final SignedData<CommitPayload> commit =
        proposerDataFactory.createCommit(
            roundIdentifier, Hash.ZERO, SECP256K1.sign(block.getHash(), proposerKey));

    assertThat(validator.validateCommit(commit)).isFalse();
  }

  @Test
  public void receivingProposalMessageFromNonProposerFails() {
    final Block block = TestHelpers.createProposalBlock(emptyList(), roundIdentifier);
    final SignedData<ProposalPayload> proposalMsg =
        validatorDataFactory.createProposal(roundIdentifier, block);

    assertThat(validator.validateProposal(proposalMsg)).isFalse();
  }

  @Test
  public void receivingPrepareFromProposerFails() {
    final SignedData<ProposalPayload> proposalMsg =
        proposerDataFactory.createProposal(roundIdentifier, block);

    final SignedData<PreparePayload> prepareMsg =
        proposerDataFactory.createPrepare(roundIdentifier, block.getHash());

    assertThat(validator.validateProposal(proposalMsg)).isTrue();
    assertThat(validator.validatePrepare(prepareMsg)).isFalse();
  }

  @Test
  public void receivingPrepareFromNonValidatorFails() {
    final SignedData<ProposalPayload> proposalMsg =
        proposerDataFactory.createProposal(roundIdentifier, block);

    final SignedData<PreparePayload> prepareMsg =
        nonValidatorDataFactory.createPrepare(roundIdentifier, block.getHash());

    assertThat(validator.validateProposal(proposalMsg)).isTrue();
    assertThat(validator.validatePrepare(prepareMsg)).isFalse();
  }

  @Test
  public void receivingMessagesWithDifferentRoundIdFromProposalFails() {
    final SignedData<ProposalPayload> proposalMsg =
        proposerDataFactory.createProposal(roundIdentifier, block);

    final ConsensusRoundIdentifier invalidRoundIdentifier =
        new ConsensusRoundIdentifier(
            roundIdentifier.getSequenceNumber(), roundIdentifier.getRoundNumber() + 1);
    final SignedData<PreparePayload> prepareMsg =
        validatorDataFactory.createPrepare(invalidRoundIdentifier, block.getHash());
    final SignedData<CommitPayload> commitMsg =
        validatorDataFactory.createCommit(
            invalidRoundIdentifier, block.getHash(), SECP256K1.sign(block.getHash(), proposerKey));

    assertThat(validator.validateProposal(proposalMsg)).isTrue();
    assertThat(validator.validatePrepare(prepareMsg)).isFalse();
    assertThat(validator.validateCommit(commitMsg)).isFalse();
  }

  @Test
  public void receivingPrepareNonProposerValidatorWithCorrectRoundIsSuccessful() {
    final SignedData<ProposalPayload> proposalMsg =
        proposerDataFactory.createProposal(roundIdentifier, block);
    final SignedData<PreparePayload> prepareMsg =
        validatorDataFactory.createPrepare(roundIdentifier, block.getHash());

    assertThat(validator.validateProposal(proposalMsg)).isTrue();
    assertThat(validator.validatePrepare(prepareMsg)).isTrue();
  }

  @Test
  public void receivingACommitMessageWithAnInvalidCommitSealFails() {
    final SignedData<ProposalPayload> proposalMsg =
        proposerDataFactory.createProposal(roundIdentifier, block);

    final SignedData<CommitPayload> commitMsg =
        proposerDataFactory.createCommit(
            roundIdentifier, block.getHash(), SECP256K1.sign(block.getHash(), nonValidatorKey));

    assertThat(validator.validateProposal(proposalMsg)).isTrue();
    assertThat(validator.validateCommit(commitMsg)).isFalse();
  }

  @Test
  public void commitMessageContainingValidSealFromValidatorIsSuccessful() {
    final SignedData<ProposalPayload> proposalMsg =
        proposerDataFactory.createProposal(roundIdentifier, block);

    final SignedData<CommitPayload> proposerCommitMsg =
        proposerDataFactory.createCommit(
            roundIdentifier, block.getHash(), SECP256K1.sign(block.getHash(), proposerKey));

    final SignedData<CommitPayload> validatorCommitMsg =
        validatorDataFactory.createCommit(
            roundIdentifier, block.getHash(), SECP256K1.sign(block.getHash(), validatorKey));

    assertThat(validator.validateProposal(proposalMsg)).isTrue();
    assertThat(validator.validateCommit(proposerCommitMsg)).isTrue();
    assertThat(validator.validateCommit(validatorCommitMsg)).isTrue();
  }

  @Test
  public void subsequentProposalHasDifferentSenderFails() {
    final SignedData<ProposalPayload> proposalMsg =
        proposerDataFactory.createProposal(roundIdentifier, block);
    assertThat(validator.validateProposal(proposalMsg)).isTrue();

    final SignedData<ProposalPayload> secondProposalMsg =
        validatorDataFactory.createProposal(roundIdentifier, block);
    assertThat(validator.validateProposal(secondProposalMsg)).isFalse();
  }

  @Test
  public void subsequentProposalHasDifferentContentFails() {
    final SignedData<ProposalPayload> proposalMsg =
        proposerDataFactory.createProposal(roundIdentifier, block);
    assertThat(validator.validateProposal(proposalMsg)).isTrue();

    final ConsensusRoundIdentifier newRoundIdentifier = new ConsensusRoundIdentifier(3, 0);
    final SignedData<ProposalPayload> secondProposalMsg =
        proposerDataFactory.createProposal(newRoundIdentifier, block);
    assertThat(validator.validateProposal(secondProposalMsg)).isFalse();
  }

  @Test
  public void subsequentProposalHasIdenticalSenderAndContentIsSuccessful() {
    final SignedData<ProposalPayload> proposalMsg =
        proposerDataFactory.createProposal(roundIdentifier, block);
    assertThat(validator.validateProposal(proposalMsg)).isTrue();

    final SignedData<ProposalPayload> secondProposalMsg =
        proposerDataFactory.createProposal(roundIdentifier, block);
    assertThat(validator.validateProposal(secondProposalMsg)).isTrue();
  }
}
