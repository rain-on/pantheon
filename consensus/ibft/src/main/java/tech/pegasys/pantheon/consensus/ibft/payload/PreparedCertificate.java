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

import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.ethereum.rlp.RLPOutput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.StringJoiner;

public class PreparedCertificate {
  private final SignedData<ProposalPayload> proposalPayload;
  private final Collection<SignedData<PreparePayload>> preparePayloads;

  public PreparedCertificate(
      final SignedData<ProposalPayload> proposalPayload,
      final Collection<SignedData<PreparePayload>> preparePayloads) {
    this.proposalPayload = proposalPayload;
    this.preparePayloads = preparePayloads;
  }

  public static PreparedCertificate readFrom(final RLPInput rlpInput) {
    final SignedData<ProposalPayload> proposalMessage;
    final Collection<SignedData<PreparePayload>> prepareMessages;

    rlpInput.enterList();
    proposalMessage = SignedData.readSignedProposalPayloadFrom(rlpInput);
    prepareMessages = rlpInput.readList(SignedData::readSignedPreparePayloadFrom);
    rlpInput.leaveList();

    return new PreparedCertificate(proposalMessage, prepareMessages);
  }

  public void writeTo(final RLPOutput rlpOutput) {
    rlpOutput.startList();
    proposalPayload.writeTo(rlpOutput);
    rlpOutput.writeList(preparePayloads, SignedData::writeTo);
    rlpOutput.endList();
  }

  public SignedData<ProposalPayload> getProposalPayload() {
    return proposalPayload;
  }

  public Collection<SignedData<PreparePayload>> getPreparePayloads() {
    return preparePayloads;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final PreparedCertificate that = (PreparedCertificate) o;
    return Objects.equals(proposalPayload, that.proposalPayload)
        && Objects.equals(new ArrayList<>(preparePayloads), new ArrayList<>(that.preparePayloads));
  }

  @Override
  public int hashCode() {
    return Objects.hash(proposalPayload, preparePayloads);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", PreparedCertificate.class.getSimpleName() + "[", "]")
        .add("proposalPayload=" + proposalPayload)
        .add("preparePayloads=" + preparePayloads)
        .toString();
  }
}
