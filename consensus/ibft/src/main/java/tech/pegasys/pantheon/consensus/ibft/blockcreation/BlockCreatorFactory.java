package tech.pegasys.pantheon.consensus.ibft.blockcreation;

import tech.pegasys.pantheon.ethereum.blockcreation.AbstractBlockCreator;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.Wei;
import tech.pegasys.pantheon.util.bytes.BytesValue;

public abstract class BlockCreatorFactory {

  protected volatile BytesValue vanityData;
  private volatile Wei minTransactionGasPrice;

  public BlockCreatorFactory(BytesValue vanityData,
      Wei minTransactionGasPrice) {
    this.vanityData = vanityData;
    this.minTransactionGasPrice = minTransactionGasPrice;
  }

  public abstract AbstractBlockCreator<?> create(final BlockHeader parentHeader, final int round);

  public void setExtraData(final BytesValue extraData) {
    this.vanityData = extraData.copy();
  }

  public void setMinTransactionGasPrice(final Wei minTransactionGasPrice) {
    this.minTransactionGasPrice = minTransactionGasPrice.copy();
  }

  public Wei getMinTransactionGasPrice() {
    return minTransactionGasPrice;
  }

}
