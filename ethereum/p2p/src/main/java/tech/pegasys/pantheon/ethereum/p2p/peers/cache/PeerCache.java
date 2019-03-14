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
package tech.pegasys.pantheon.ethereum.p2p.peers.cache;

import tech.pegasys.pantheon.util.enode.EnodeURL;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableList;

public class PeerCache {

  private final Set<EnodeURL> nodes = new HashSet<>();

  public PeerCache(final Set<EnodeURL> initialNodes) {
    nodes.addAll(initialNodes);
  }

  public boolean add(final EnodeURL enode) throws IllegalArgumentException {
    return nodes.add(enode);
  }

  public boolean remove(final EnodeURL enode) {
    return nodes.remove(enode);
  }

  public ImmutableList<EnodeURL> getStaticNodes() {
    return ImmutableList.copyOf(nodes);
  }
}
