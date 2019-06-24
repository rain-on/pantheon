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
package tech.pegasys.pantheon.consensus.ibftlegacy.headervalidationrules;

import tech.pegasys.pantheon.consensus.common.ValidatorProvider;
import tech.pegasys.pantheon.consensus.ibft.IbftContext;
import tech.pegasys.pantheon.consensus.ibftlegacy.IbftBlockHashing;
import tech.pegasys.pantheon.consensus.ibftlegacy.IbftExtraData;
import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.mainnet.AttachedBlockHeaderValidationRule;
import tech.pegasys.pantheon.ethereum.rlp.RLPException;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.Iterables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Ensures the byte content of the extraData field can be deserialised into an appropriate
 * structure, and that the structure created contains data matching expectations from preceding
 * blocks.
 */
public class IbftExtraDataValidationRule implements AttachedBlockHeaderValidationRule<IbftContext> {

  private static final Logger LOG = LogManager.getLogger();

  @Override
  public boolean validate(
      final BlockHeader header,
      final BlockHeader parent,
      final ProtocolContext<IbftContext> context) {
    try {
      final ValidatorProvider validatorProvider =
          context.getConsensusState().getVoteTallyCache().getVoteTallyAfterBlock(parent);
      final IbftExtraData ibftExtraData = IbftExtraData.decode(header);

      final Address proposer = IbftBlockHashing.recoverProposerAddress(header, ibftExtraData);

      final Collection<Address> storedValidators = validatorProvider.getValidators();

      if (!storedValidators.contains(proposer)) {
        LOG.trace("Proposer sealing block is not a member of the validators.");
        return false;
      }

      final SortedSet<Address> sortedReportedValidators =
          new TreeSet<>(ibftExtraData.getValidators());

      if (!Iterables.elementsEqual(ibftExtraData.getValidators(), sortedReportedValidators)) {
        LOG.trace(
            "Validators are not sorted in ascending order. Expected {} but got {}.",
            sortedReportedValidators,
            ibftExtraData.getValidators());
        return false;
      }

      if (!Iterables.elementsEqual(ibftExtraData.getValidators(), storedValidators)) {
        LOG.trace(
            "Incorrect validators. Expected {} but got {}.",
            storedValidators,
            ibftExtraData.getValidators());
        return false;
      }

    } catch (final RLPException ex) {
      LOG.trace("ExtraData field was unable to be deserialised into an IBFT Struct.", ex);
      return false;
    } catch (final IllegalArgumentException ex) {
      LOG.trace("Failed to verify extra data", ex);
      return false;
    } catch (final RuntimeException ex) {
      LOG.trace("Failed to find validators at parent");
      return false;
    }

    return true;
  }
}
