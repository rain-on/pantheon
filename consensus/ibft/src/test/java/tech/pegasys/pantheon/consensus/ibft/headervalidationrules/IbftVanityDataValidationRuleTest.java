package tech.pegasys.pantheon.consensus.ibft.headervalidationrules;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import tech.pegasys.pantheon.consensus.ibft.IbftExtraData;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderTestFixture;
import tech.pegasys.pantheon.util.bytes.BytesValue;

public class IbftVanityDataValidationRuleTest {

  private final IbftVanityDataValidationRule validationRule = new IbftVanityDataValidationRule();

  @Test
  public void testCases() {
    assertThat(headerWithVanityDataOfSize(0)).isFalse();
    assertThat(headerWithVanityDataOfSize(31)).isFalse();
    assertThat(headerWithVanityDataOfSize(32)).isTrue();
    assertThat(headerWithVanityDataOfSize(33)).isFalse();
  }


  public boolean headerWithVanityDataOfSize(final int extraDataSize) {
    final IbftExtraData extraData = new IbftExtraData(
        BytesValue.wrap(new byte[extraDataSize]),
        emptyList(),
        empty(),
        0,
        emptyList());
    final BlockHeader header =
        new BlockHeaderTestFixture().extraData(extraData.encode()).buildHeader();

    return validationRule.validate(header, null, null);

  }

}