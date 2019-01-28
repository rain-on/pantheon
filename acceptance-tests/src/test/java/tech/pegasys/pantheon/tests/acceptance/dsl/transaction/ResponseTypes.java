package tech.pegasys.pantheon.tests.acceptance.dsl.transaction;

import java.util.List;
import java.util.Map;
import org.web3j.protocol.core.Response;
import tech.pegasys.pantheon.ethereum.core.Address;

public class ResponseTypes {
  public static class ProposeResponse extends Response<Boolean> {}

  public static class DiscardResponse extends Response<Boolean> {}

  public static class SignersBlockResponse extends Response<List<Address>> {}

  public static class ProposalsResponse extends Response<Map<Address, Boolean>> {}

  public static class AddAccountsToWhitelistResponse extends Response<Boolean> {}

  public static class RemoveAccountsFromWhitelistResponse extends Response<Boolean> {}

  public static class GetAccountsWhitelistResponse extends Response<List<String>> {}

  public static class AddNodeResponse extends Response<Boolean> {}

  public static class RemoveNodeResponse extends Response<Boolean> {}

  public static class GetNodesWhitelistResponse extends Response<List<String>> {}
}
