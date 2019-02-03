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

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.RoundChange;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RoundChangeMessageValidator {

  private static final Logger LOG = LogManager.getLogger();

  private final RoundChangeSignedDataValidator signedDataValidator;
  private final ProposalBlockConsistencyChecker proposalBlockConsistencyChecker;

  public RoundChangeMessageValidator(
      final RoundChangeSignedDataValidator signedDataValidator,
      final ProposalBlockConsistencyChecker proposalBlockConsistencyChecker) {
    this.proposalBlockConsistencyChecker = proposalBlockConsistencyChecker;
    this.signedDataValidator = signedDataValidator;
  }

  public boolean validateMessage(final RoundChange msg) {

    if (!signedDataValidator.validatePayload(msg.getSignedPayload())) {
      LOG.info("Invalid RoundChange message, signed data did not validate correctly.");
      return false;
    }

    if (msg.getPreparedCertificate().isPresent() != msg.getProposedBlock().isPresent()) {
      LOG.info(
          "Invalid RoundChange message, availability of certificate does not correlate with"
              + "availability of block.");
      return false;
    }

    if (msg.getPreparedCertificate().isPresent()) {
      if (!proposalBlockConsistencyChecker.validateProposalMatchesBlock(
          msg.getPreparedCertificate().get().getProposalPayload(), msg.getProposedBlock().get())) {
        LOG.info("Invalid RoundChange message, proposal did not align with supplied block.");
        return false;
      }
    }

    return true;
  }

  @FunctionalInterface
  public interface MessageValidatorForHeightFactory {

    MessageValidator createAt(final ConsensusRoundIdentifier roundIdentifier);
  }
}
