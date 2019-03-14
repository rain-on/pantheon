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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PersistentJsonPeerCache extends PersistentPeerCache {

  private static final Logger LOG = LogManager.getLogger();
  private static final Charset UTF_8 = Charset.forName("UTF-8");

  private final Path path;

  public PersistentJsonPeerCache(final Set<EnodeURL> initialNodes, final Path path) {
    super(initialNodes);
    this.path = path;
  }

  public static PersistentJsonPeerCache fromPath(final Path path)
      throws IOException, IllegalArgumentException {
    final Set<EnodeURL> result = new HashSet<>();

    final byte[] staticNodesContent;
    try {
      staticNodesContent = Files.readAllBytes(path);
      if (staticNodesContent.length == 0) {
        return new PersistentJsonPeerCache(result, path);
      }
    } catch (FileNotFoundException | NoSuchFileException ex) {
      LOG.info("No StaticNodes file  ({}) exists, creating empty cache.", path);
      return new PersistentJsonPeerCache(result, path);
    } catch (IOException ex) {
      LOG.info("Unable to parse static nodes file ({})", path);
      throw ex;
    }

    try {
      final JsonArray enodeJsonArray = new JsonArray(new String(staticNodesContent, UTF_8));
      for (Object jsonObj : enodeJsonArray.getList()) {
        final String enodeString = (String) jsonObj;
        result.add(decodeString(enodeString));
      }
      return new PersistentJsonPeerCache(result, path);
    } catch (DecodeException ex) {
      LOG.info("Content of ({}} was invalid json, and could not be decoded.", path);
      throw ex;
    } catch (IllegalArgumentException ex) {
      LOG.info("Parsing ({}) has failed due incorrectly formatted enode element.", path);
      throw ex;
    }
  }

  private static EnodeURL decodeString(final String input) {
    try {
      return new EnodeURL(input);
    } catch (IllegalArgumentException ex) {
      LOG.info("Illegally constructed enode supplied ({})", input);
      throw ex;
    }
  }

  // TODO: This should potentially be using an atomicMove to prevent file corruption
  // on sudden terminations.
  @Override
  protected void persist() {
    final JsonArray array =
        new JsonArray(
            getStaticNodes().stream().map(EnodeURL::toString).collect(Collectors.toList()));

    try {
      Files.write(path, array.toString().getBytes(UTF_8));
    } catch (IOException ex) {
      LOG.info("Unable to persist peers to disk, file write failed.");
    }
  }
}
