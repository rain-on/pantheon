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
package tech.pegasys.pantheon.tests.acceptance.dsl.transaction.ibft;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.JsonRequestFactories;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.ResponseTypes.ProposeResponse;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.Transaction;

import java.io.IOException;

public class IbftPropose implements Transaction<Boolean> {
  private final String address;
  private final boolean auth;

  public IbftPropose(final String address, final boolean auth) {
    this.address = address;
    this.auth = auth;
  }

  @Override
  public Boolean execute(final JsonRequestFactories node) {
    try {
      final ProposeResponse result = node.ibft().ibftPropose(address, auth).send();
      assertThat(result).isNotNull();
      assertThat(result.hasError()).isFalse();
      return result.getResult();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
}