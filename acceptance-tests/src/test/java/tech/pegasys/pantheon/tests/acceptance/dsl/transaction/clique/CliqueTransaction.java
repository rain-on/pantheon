package tech.pegasys.pantheon.tests.acceptance.dsl.transaction.clique;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.web3j.protocol.core.Response;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.PantheonWeb3j;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.Transaction;

public class CliqueTransaction<T> implements Transaction {

  private final String transactionName;
  public final


  @Override
  public T execute(PantheonWeb3j node) {
    try {
      final Response<T> result = node.cliqueDiscard(address).send();
      assertThat(result).isNotNull();
      assertThat(result.hasError()).isFalse();
      return result.getResult();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
}
