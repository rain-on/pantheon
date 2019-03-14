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
package tech.pegasys.pantheon.ethereum.p2p.peers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import tech.pegasys.pantheon.ethereum.p2p.peers.cache.PeerCache;
import tech.pegasys.pantheon.ethereum.p2p.peers.cache.PersistentJsonPeerCache;
import tech.pegasys.pantheon.util.enode.EnodeURL;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.google.common.collect.ImmutableList;
import io.vertx.core.json.DecodeException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PersistentJsonPeerCacheTest {

  // NOTE: The invalid_static_nodes file is identical to the valid, however one node's port is set
  // to "A".

  // First peer ion the valid_static_nodes file.
  private final EnodeURL firstItemInValidList =
      new EnodeURL(
          "50203c6bfca6874370e71aecc8958529fd723feb05013dc1abca8fc1fff845c5259faba05852e9dfe5ce172a7d6e7c2a3a5eaa8b541c8af15ea5518bbff5f2fa",
          "127.0.0.1",
          30303);

  @Rule public TemporaryFolder testFolder = new TemporaryFolder();

  @Test
  public void validFileLoadsWithExpectedNumberOfPeers() throws IOException {
    final URL resource = PersistentJsonPeerCacheTest.class.getResource("valid_static_nodes.json");
    final Path path = Paths.get(resource.getPath());

    final PeerCache cache = PersistentJsonPeerCache.fromPath(path);
    final ImmutableList<EnodeURL> enodes = cache.getStaticNodes();

    assertThat(enodes.size()).isEqualTo(4);
  }

  @Test
  public void invalidFileThrowsAnException() {
    final URL resource = PersistentJsonPeerCacheTest.class.getResource("invalid_static_nodes.json");
    final Path path = Paths.get(resource.getPath());

    assertThatThrownBy(() -> PersistentJsonPeerCache.fromPath(path))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void nonJsonFileThrowsAnException() throws IOException {
    final File tempFile = testFolder.newFile("file.txt");
    tempFile.deleteOnExit();
    Files.write(tempFile.toPath(), "This Is Not Json".getBytes(Charset.forName("UTF-8")));

    assertThatThrownBy(() -> PersistentJsonPeerCache.fromPath(tempFile.toPath()))
        .isInstanceOf(DecodeException.class);
  }

  @Test
  public void anEmptyCacheIsCreatedIfTheFileDoesNotExist() throws IOException {
    final Path path = Paths.get("./arbirtraryFilename.txt");

    final PeerCache cache = PersistentJsonPeerCache.fromPath(path);
    assertThat(cache.getStaticNodes().size()).isZero();
  }

  @Test
  public void cacheIsCreatedIfFileExistsButIsEmpty() throws IOException {
    final File tempFile = testFolder.newFile("file.txt");
    tempFile.deleteOnExit();

    final PeerCache cache = PersistentJsonPeerCache.fromPath(tempFile.toPath());
    final ImmutableList<EnodeURL> enodes = cache.getStaticNodes();

    assertThat(enodes.size()).isEqualTo(0);
  }

  @Test
  public void addPeerReturnsFalseIfSuppliedPeerIsAlreadyInList() throws IOException {
    final URL resource = PersistentJsonPeerCacheTest.class.getResource("valid_static_nodes.json");
    final Path original = Paths.get(resource.getPath());
    final File tempFile = testFolder.newFile("file.txt");
    tempFile.deleteOnExit();
    final Path tempPath = tempFile.toPath();
    Files.copy(original, tempPath, StandardCopyOption.REPLACE_EXISTING);

    final PeerCache cache = PersistentJsonPeerCache.fromPath(tempPath);

    final ImmutableList<EnodeURL> preAddList = cache.getStaticNodes();
    assertThat(cache.add(firstItemInValidList)).isFalse();
    assertThat(cache.getStaticNodes()).containsExactlyElementsOf(preAddList);
  }

  @Test
  public void addPeerWritesToFileIfPeerIsNotAlreadyInList() throws IOException {
    final File tempFile = testFolder.newFile("file.txt");
    tempFile.deleteOnExit();
    final PeerCache cache = PersistentJsonPeerCache.fromPath(tempFile.toPath());

    assertThat(cache.add(firstItemInValidList)).isTrue();
    assertThat(cache.getStaticNodes()).containsExactly(firstItemInValidList);

    // Ensure file has been updated by reloading into a new cache (ensures file validity as well).
    final PersistentJsonPeerCache newCache = PersistentJsonPeerCache.fromPath(tempFile.toPath());
    assertThat(newCache.getStaticNodes()).contains(firstItemInValidList);
  }

  @Test
  public void removePeerReturnsFalseIfSuppliedPeerIsNotInList() throws IOException {
    final File tempFile = testFolder.newFile("file.txt");
    tempFile.deleteOnExit();
    final PeerCache cache = PersistentJsonPeerCache.fromPath(tempFile.toPath());

    assertThat(cache.remove(firstItemInValidList)).isFalse();
    assertThat(cache.getStaticNodes().size()).isZero();
  }

  @Test
  public void removePeerReturnsTrueAndRemovesItemIfPeerIsInList()
      throws IOException {
    final URL resource = PersistentJsonPeerCacheTest.class.getResource("valid_static_nodes.json");
    final Path original = Paths.get(resource.getPath());
    final File tempFile = testFolder.newFile("file.txt");
    tempFile.deleteOnExit();
    final Path tempPath = tempFile.toPath();
    Files.copy(original, tempPath, StandardCopyOption.REPLACE_EXISTING);

    final PeerCache cache = PersistentJsonPeerCache.fromPath(tempPath);
    assertThat(cache.remove(firstItemInValidList)).isTrue();
    assertThat(cache.getStaticNodes()).doesNotContain(firstItemInValidList);

    // Ensure file has been updated by reloading into a new cache (ensures file validity as well).
    final PersistentJsonPeerCache newCache = PersistentJsonPeerCache.fromPath(tempPath);
    assertThat(newCache.getStaticNodes()).doesNotContain(firstItemInValidList);
  }
}
