package tech.pegasys.pantheon.tests.acceptance.dsl.transaction;

import java.util.Collections;
import java.util.List;
import org.assertj.core.util.Lists;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.PantheonWeb3j.AddAccountsToWhitelistResponse;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.PantheonWeb3j.AddNodeResponse;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.PantheonWeb3j.GetAccountsWhitelistResponse;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.PantheonWeb3j.GetNodesWhitelistResponse;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.PantheonWeb3j.RemoveAccountsFromWhitelistResponse;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.PantheonWeb3j.RemoveNodeResponse;

public class PermJsonRpcRequestFactory {
  private final Web3jService web3jService;

  public PermJsonRpcRequestFactory(Web3jService web3jService) {
    this.web3jService = web3jService;
  }

  public Request<?, AddNodeResponse> addNodesToWhitelist(final List<String> enodeList) {
    return new Request<>(
        "perm_addNodesToWhitelist",
        Collections.singletonList(enodeList),
        web3jService,
        AddNodeResponse.class);
  }

  public Request<?, RemoveNodeResponse> removeNodesFromWhitelist(final List<String> enodeList) {
    return new Request<>(
        "perm_removeNodesFromWhitelist",
        Collections.singletonList(enodeList),
        web3jService,
        RemoveNodeResponse.class);
  }

  public Request<?, GetNodesWhitelistResponse> getNodesWhitelist() {
    return new Request<>(
        "perm_getNodesWhitelist", Lists.emptyList(), web3jService, GetNodesWhitelistResponse.class);
  }

  public Request<?, GetAccountsWhitelistResponse> getAccountsWhitelist() {
    return new Request<>(
        "perm_getAccountsWhitelist", null, web3jService, GetAccountsWhitelistResponse.class);
  }

  public Request<?, AddAccountsToWhitelistResponse> addAccountsToWhitelist(
      final List<String> accounts) {
    return new Request<>(
        "perm_addAccountsToWhitelist",
        Collections.singletonList(accounts),
        web3jService,
        AddAccountsToWhitelistResponse.class);
  }

  public Request<?, RemoveAccountsFromWhitelistResponse> removeAccountsFromWhitelist(
      final List<String> accounts) {
    return new Request<>(
        "perm_removeAccountsFromWhitelist",
        Collections.singletonList(accounts),
        web3jService,
        RemoveAccountsFromWhitelistResponse.class);
  }

}
