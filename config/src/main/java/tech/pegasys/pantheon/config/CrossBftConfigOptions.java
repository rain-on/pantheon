/*
 * Copyright 2019 ConsenSys AG.
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
package tech.pegasys.pantheon.config;

import io.vertx.core.json.JsonObject;

public class CrossBftConfigOptions extends IbftConfigOptions {

  public static final CrossBftConfigOptions DEFAULT = new CrossBftConfigOptions(new JsonObject());

  private static final long DEFAULT_CROSS_OVER_BLOCK = 1L;

  CrossBftConfigOptions(final JsonObject configRoot) {
    super(configRoot);
  }

  public long getCrossOverBlock() {
    return configRoot.getLong("crossoverblock", DEFAULT_CROSS_OVER_BLOCK);
  }
}
