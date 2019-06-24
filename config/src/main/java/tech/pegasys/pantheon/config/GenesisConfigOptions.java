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
package tech.pegasys.pantheon.config;

import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

public interface GenesisConfigOptions {

  boolean isEthHash();

  boolean isIbftLegacy();

  boolean isIbft2();

  boolean isClique();

  boolean isCrossBft();

  IbftConfigOptions getIbftLegacyConfigOptions();

  CliqueConfigOptions getCliqueConfigOptions();

  IbftConfigOptions getIbft2ConfigOptions();

  EthashConfigOptions getEthashConfigOptions();

  CrossBftConfigOptions getCrossBfrConfigOptions();

  OptionalLong getHomesteadBlockNumber();

  OptionalLong getDaoForkBlock();

  OptionalLong getTangerineWhistleBlockNumber();

  OptionalLong getSpuriousDragonBlockNumber();

  OptionalLong getByzantiumBlockNumber();

  OptionalLong getConstantinopleBlockNumber();

  OptionalLong getConstantinopleFixBlockNumber();

  Optional<BigInteger> getChainId();

  OptionalInt getContractSizeLimit();

  OptionalInt getEvmStackSize();

  Map<String, Object> asMap();
}
