package tech.pegasys.pantheon.tests.acceptance.dsl.transaction;

import org.web3j.protocol.core.JsonRpc2_0Web3j;

public class JsonRequestFactories {

  private final JsonRpc2_0Web3j netEth;
  private final CliqueJsonRpcRequestFactory clique;
  private final IbftJsonRpcRequestFactory ibft;
  private final PermJsonRpcRequestFactory perm;

  public JsonRequestFactories(JsonRpc2_0Web3j netEth,
      CliqueJsonRpcRequestFactory clique,
      IbftJsonRpcRequestFactory ibft,
      PermJsonRpcRequestFactory perm) {
    this.netEth = netEth;
    this.clique = clique;
    this.ibft = ibft;
    this.perm = perm;
  }

  public JsonRpc2_0Web3j eth() {
    return netEth;
  }

  public JsonRpc2_0Web3j net() {
    return netEth;
  }

  public CliqueJsonRpcRequestFactory clique() {
    return clique;
  }

  public IbftJsonRpcRequestFactory ibft() {
    return ibft;
  }

  public PermJsonRpcRequestFactory perm() {
    return perm;
  }
}
