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
package tech.pegasys.pantheon.consensus.ibft.validation;

import tech.pegasys.pantheon.consensus.ibft.messagewrappers.Commit;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.Prepare;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.Proposal;
import tech.pegasys.pantheon.consensus.ibft.payload.ProposalPayload;
import tech.pegasys.pantheon.consensus.ibft.payload.SignedData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageValidator {

  private static final Logger LOG = LogManager.getLogger();
  private final SignedDataValidator dataValidator;
  private final ProposalBlockConsistencyChecker consistencyChecker;

  public MessageValidator(
      final SignedDataValidator dataValidator,
      final ProposalBlockConsistencyChecker consistencyChecker) {
    this.dataValidator = dataValidator;
    this.consistencyChecker = consistencyChecker;
  }

  public boolean addProposalMessage(final Proposal msg) {
    final SignedData<ProposalPayload> signedPayload = msg.getSignedPayload();

    if (!consistencyChecker.validateProposalMatchesBlock(msg.getSignedPayload(), msg.getBlock())) {
      LOG.info("Invalid Proposal, Block does not validate, or does not align with proposal data.");
    }

    return dataValidator.addSignedProposalPayload(signedPayload);
  }

  public boolean validatePrepareMessage(final Prepare msg) {
    return dataValidator.validatePreparePayload(msg.getSignedPayload());
  }

  public boolean validateCommitMessage(final Commit msg) {
    return dataValidator.validateCommitPayload(msg.getSignedPayload());
  }
}
