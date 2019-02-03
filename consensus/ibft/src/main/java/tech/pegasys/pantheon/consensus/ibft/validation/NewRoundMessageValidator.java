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

import static tech.pegasys.pantheon.consensus.ibft.IbftHelpers.findLatestPreparedCertificate;
import static tech.pegasys.pantheon.consensus.ibft.IbftHelpers.prepareMessageCountForQuorum;

import java.util.Collection;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.IbftBlockHashing;
import tech.pegasys.pantheon.consensus.ibft.IbftBlockInterface;
import tech.pegasys.pantheon.consensus.ibft.blockcreation.ProposerSelector;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.NewRound;
import tech.pegasys.pantheon.consensus.ibft.payload.NewRoundPayload;
import tech.pegasys.pantheon.consensus.ibft.payload.PreparedCertificate;
import tech.pegasys.pantheon.consensus.ibft.payload.ProposalPayload;
import tech.pegasys.pantheon.consensus.ibft.payload.RoundChangeCertificate;
import tech.pegasys.pantheon.consensus.ibft.payload.RoundChangePayload;
import tech.pegasys.pantheon.consensus.ibft.payload.SignedData;
import tech.pegasys.pantheon.consensus.ibft.validation.RoundChangeSignedDataValidator.SignedDataValidatorForHeightFactory;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.Hash;

public class NewRoundMessageValidator {

  private static final Logger LOG = LogManager.getLogger();

  private final Collection<Address> validators;
  private final ProposerSelector proposerSelector;
  private final SignedDataValidatorForHeightFactory signedDataForHeightValidator;
  private ProposalBlockConsistencyChecker proposalBlockConsistencyChecker;
  private final long quorum;
  private final long chainHeight;

  public NewRoundMessageValidator(
      final Collection<Address> validators,
      final ProposerSelector proposerSelector,
      final SignedDataValidatorForHeightFactory signedDataForHeightValidator,
      final ProposalBlockConsistencyChecker proposalBlockConsistencyChecker,
      final long quorum,
      final long chainHeight) {
    this.validators = validators;
    this.proposerSelector = proposerSelector;
    this.signedDataForHeightValidator = signedDataForHeightValidator;
    this.proposalBlockConsistencyChecker = proposalBlockConsistencyChecker;
    this.quorum = quorum;
    this.chainHeight = chainHeight;
  }

  public boolean validateNewRoundMessage(final NewRound msg) {
    final SignedData<NewRoundPayload> signedPayload = msg.getSignedPayload();
    final NewRoundPayload payload = signedPayload.getPayload();
    final ConsensusRoundIdentifier rootRoundIdentifier = payload.getRoundIdentifier();
    final Address expectedProposer = proposerSelector.selectProposerForRound(rootRoundIdentifier);
    final RoundChangeCertificate roundChangeCert = payload.getRoundChangeCertificate();

    if (!expectedProposer.equals(msg.getAuthor())) {
      LOG.info("Invalid NewRound message, did not originate from expected proposer.");
      return false;
    }

    if (msg.getRoundIdentifier().getSequenceNumber() != chainHeight) {
      LOG.info("Invalid NewRound message, not valid for local chain height.");
      return false;
    }

    if (msg.getRoundIdentifier().getRoundNumber() == 0) {
      LOG.info("Invalid NewRound message, illegally targets a new round of 0.");
      return false;
    }

    final SignedData<ProposalPayload> proposalPayload = payload.getProposalPayload();
    final SignedDataValidator proposalValidator =
        signedDataForHeightValidator.createAt(rootRoundIdentifier);
    if (!proposalValidator.addSignedProposalPayload(proposalPayload)) {
      LOG.info("Invalid NewRound message, embedded proposal failed validation");
      return false;
    }

    if(!proposalBlockConsistencyChecker.validateProposalMatchesBlock(proposalPayload, msg.getBlock())) {
      LOG.info("Invalid New Round, proposal payload did not align with supplied block.");
      return false;
    }

    if (!validateRoundChangeMessagesAndEnsureTargetRoundMatchesRoot(
        rootRoundIdentifier, roundChangeCert)) {
      return false;
    }

    return validateProposalMessageMatchesLatestPrepareCertificate(payload, msg.getBlock());
  }

  private boolean validateRoundChangeMessagesAndEnsureTargetRoundMatchesRoot(
      final ConsensusRoundIdentifier expectedRound, final RoundChangeCertificate roundChangeCert) {

    final Collection<SignedData<RoundChangePayload>> roundChangePayloads =
        roundChangeCert.getRoundChangePayloads();

    if (roundChangePayloads.size() < quorum) {
      LOG.info(
          "Invalid NewRound message, RoundChange certificate has insufficient "
              + "RoundChange messages.");
      return false;
    }

    if (!roundChangeCert
        .getRoundChangePayloads()
        .stream()
        .allMatch(p -> p.getPayload().getRoundIdentifier().equals(expectedRound))) {
      LOG.info(
          "Invalid NewRound message, not all embedded RoundChange messages have a "
              + "matching target round.");
      return false;
    }

    for (final SignedData<RoundChangePayload> roundChangePayload :
        roundChangeCert.getRoundChangePayloads()) {
      final RoundChangeSignedDataValidator roundChangeValidator =
          new RoundChangeSignedDataValidator(
              signedDataForHeightValidator,
              validators,
              prepareMessageCountForQuorum(quorum),
              chainHeight);

      if (!roundChangeValidator.validatePayload(roundChangePayload)) {
        LOG.info("Invalid NewRound message, embedded RoundChange message failed validation.");
        return false;
      }
    }
    return true;
  }

  private boolean validateProposalMessageMatchesLatestPrepareCertificate(
      final NewRoundPayload payload, final Block proposedBlock) {

    final RoundChangeCertificate roundChangeCert = payload.getRoundChangeCertificate();
    final Collection<SignedData<RoundChangePayload>> roundChangeMsgs =
        roundChangeCert.getRoundChangePayloads();

    final Optional<PreparedCertificate> latestPreparedCertificate =
        findLatestPreparedCertificate(roundChangeMsgs);

    if (!latestPreparedCertificate.isPresent()) {
      LOG.info(
          "No round change messages have a preparedCertificate, any valid block may be proposed.");
      return true;
    }

    // Need to check that if we substitute the LatestedPrepareCert round number into the supplied
    // block that we get the SAME hash as PreparedCert.
    final Block currentBlockWithOldRound = IbftBlockInterface.replaceRoundInBlock(
        proposedBlock,
        latestPreparedCertificate.get().getProposalPayload().getPayload().getRoundIdentifier()
            .getRoundNumber());

    final Hash oldRoundHash =
        IbftBlockHashing.calculateDataHashForCommittedSeal(currentBlockWithOldRound.getHeader());

    if(oldRoundHash != latestPreparedCertificate.get().getProposalPayload().getPayload().getDigest()) {
      LOG.info(
          "Invalid NewRound message, block in latest RoundChange does not match proposed block.");
      return false;
    }

    return true;
  }
}
