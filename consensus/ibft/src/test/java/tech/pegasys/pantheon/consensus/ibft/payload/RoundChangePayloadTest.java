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

import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.TestHelpers;
import tech.pegasys.pantheon.consensus.ibft.messagedata.IbftV2;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.AddressHelpers;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.rlp.BytesValueRLPOutput;
import tech.pegasys.pantheon.ethereum.rlp.RLP;
import tech.pegasys.pantheon.ethereum.rlp.RLPInput;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Optional;

import org.assertj.core.util.Lists;
import org.junit.Test;

public class RoundChangePayloadTest {

  private static final ConsensusRoundIdentifier ROUND_IDENTIFIER =
      new ConsensusRoundIdentifier(0x1234567890ABCDEFL, 0xFEDCBA98);
  private static final Signature SIGNATURE =
      Signature.create(BigInteger.ONE, BigInteger.TEN, (byte) 0);

  @Test
  public void roundTripRlpWithNoPreparedCertificate() {
    final RoundChangePayload roundChangePayload = new RoundChangePayload(ROUND_IDENTIFIER, empty());
    final BytesValueRLPOutput rlpOut = new BytesValueRLPOutput();
    roundChangePayload.writeTo(rlpOut);

    final RLPInput rlpInput = RLP.input(rlpOut.encoded());
    RoundChangePayload actualRoundChangePayload = RoundChangePayload.readFrom(rlpInput);
    assertThat(actualRoundChangePayload.getRoundIdentifier()).isEqualTo(ROUND_IDENTIFIER);
    assertThat(actualRoundChangePayload.getPreparedCertificate()).isEqualTo(Optional.empty());
    assertThat(actualRoundChangePayload.getMessageType()).isEqualTo(IbftV2.ROUND_CHANGE);
  }

  @Test
  public void roundTripRlpWithEmptyPreparedCertificate() {
    final ProposalMessage signedProposal = new ProposalMessage(signedProposal());

    final PreparedCertificate preparedCertificate =
        new PreparedCertificate(signedProposal, Collections.emptyList());

    final RoundChangePayload roundChangePayload =
        new RoundChangePayload(ROUND_IDENTIFIER, Optional.of(preparedCertificate));
    final BytesValueRLPOutput rlpOut = new BytesValueRLPOutput();
    roundChangePayload.writeTo(rlpOut);

    final RLPInput rlpInput = RLP.input(rlpOut.encoded());
    RoundChangePayload actualRoundChangePayload = RoundChangePayload.readFrom(rlpInput);
    assertThat(actualRoundChangePayload.getRoundIdentifier()).isEqualTo(ROUND_IDENTIFIER);
    assertThat(actualRoundChangePayload.getPreparedCertificate())
        .isEqualTo(Optional.of(preparedCertificate));
    assertThat(actualRoundChangePayload.getMessageType()).isEqualTo(IbftV2.ROUND_CHANGE);
  }

  @Test
  public void roundTripRlpWithPreparedCertificate() {
    final ProposalMessage signedProposal = new ProposalMessage(signedProposal());

    final PreparePayload preparePayload =
        new PreparePayload(ROUND_IDENTIFIER, Hash.fromHexStringLenient("0x8523ba6e7c5f59ae87"));
    final PrepareMessage signedPrepare = new PrepareMessage(SignedData.from(preparePayload, SIGNATURE));
    final PreparedCertificate preparedCert =
        new PreparedCertificate(signedProposal, Lists.newArrayList(signedPrepare));

    final RoundChangePayload roundChangePayload =
        new RoundChangePayload(ROUND_IDENTIFIER, Optional.of(preparedCert));
    final BytesValueRLPOutput rlpOut = new BytesValueRLPOutput();
    roundChangePayload.writeTo(rlpOut);

    final RLPInput rlpInput = RLP.input(rlpOut.encoded());
    RoundChangePayload actualRoundChangePayload = RoundChangePayload.readFrom(rlpInput);
    assertThat(actualRoundChangePayload.getRoundIdentifier()).isEqualTo(ROUND_IDENTIFIER);
    assertThat(actualRoundChangePayload.getPreparedCertificate())
        .isEqualTo(Optional.of(preparedCert));
    assertThat(actualRoundChangePayload.getMessageType()).isEqualTo(IbftV2.ROUND_CHANGE);
  }

  private SignedData<ProposalPayload> signedProposal() {
    final Block block =
        TestHelpers.createProposalBlock(singletonList(AddressHelpers.ofValue(1)), 0);
    final ProposalPayload proposalPayload = new ProposalPayload(ROUND_IDENTIFIER, block);
    final Signature signature = Signature.create(BigInteger.ONE, BigInteger.TEN, (byte) 0);
    return SignedData.from(proposalPayload, signature);
  }
}
