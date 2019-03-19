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

import tech.pegasys.pantheon.util.enode.EnodeURL;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import io.vertx.core.json.DecodeException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class StaticeNodesParserTest {

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
    final URL resource = StaticeNodesParserTest.class.getResource("valid_static_nodes.json");
    final Path path = Paths.get(resource.getPath());

    final Set<EnodeURL> enodes = StaticNodesParser.fromPath(path);

    assertThat(enodes.size()).isEqualTo(4);
  }

  @Test
  public void invalidFileThrowsAnException() {
    final URL resource = StaticeNodesParserTest.class.getResource("invalid_static_nodes.json");
    final Path path = Paths.get(resource.getPath());

    assertThatThrownBy(() -> StaticNodesParser.fromPath(path))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void nonJsonFileThrowsAnException() throws IOException {
    final File tempFile = testFolder.newFile("file.txt");
    tempFile.deleteOnExit();
    Files.write(tempFile.toPath(), "This Is Not Json".getBytes(Charset.forName("UTF-8")));

    assertThatThrownBy(() -> StaticNodesParser.fromPath(tempFile.toPath()))
        .isInstanceOf(DecodeException.class);
  }

  @Test
  public void anEmptyCacheIsCreatedIfTheFileDoesNotExist() throws IOException {
    final Path path = Paths.get("./arbirtraryFilename.txt");

    final Set<EnodeURL> enodes = StaticNodesParser.fromPath(path);
    assertThat(enodes.size()).isZero();
  }

  @Test
  public void cacheIsCreatedIfFileExistsButIsEmpty() throws IOException {
    final File tempFile = testFolder.newFile("file.txt");
    tempFile.deleteOnExit();

    final Set<EnodeURL> enodes = StaticNodesParser.fromPath(tempFile.toPath());
    assertThat(enodes.size()).isZero();
  }
}
