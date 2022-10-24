/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adaptris.security.password;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.adaptris.core.stubs.TempFileUtils;
import com.adaptris.security.exc.PasswordException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.junit.Test;

public class SeededAesPbeCryptoTest {

  private static final String TEXT = "MYPASSWORD";

  private static final String NON_EXISTENT_FILE = "/surely/this/file/does/not/exist";

  @Test
  public void testResolveFromProperties() throws Exception {
    Properties p = new Properties();
    String seedFile = TempFileUtils.createTrackedFile(p, () -> "Hello World").getCanonicalPath();
    p.setProperty("file-exists", seedFile);
    p.setProperty("non-existent-file", NON_EXISTENT_FILE);
    assertEquals(seedFile, SeededAesPbeCrypto.fromProperties("file-exists", p));
    assertThrows(Exception.class, () -> SeededAesPbeCrypto.fromProperties("non-existent-file", p));
    assertThrows(Exception.class, () -> SeededAesPbeCrypto.fromProperties("no-key", p));
  }

  // Technically it's a slightly dodgy test because of the way ServiceLoaders work, so we can't
  // use the Password static functions, we have to use SeededAesPbeCrypto directly.
  @Test
  public void testSeeded() throws Exception {
    Object o = new Object();
    String seedFile = TempFileUtils.createTrackedFile(o, () -> "Hello World").getCanonicalPath();
    SeededAesPbeCrypto passworder = new SeededAesPbeCrypto(seedFile);
    String encoded = passworder.encode(TEXT, StandardCharsets.UTF_8.name());
    assertEquals(TEXT, passworder.decode(encoded));
  }

  @Test
  public void testSeededEncodeException() throws Exception {
    assertThrows(PasswordException.class, () -> new SeededAesPbeCrypto(NON_EXISTENT_FILE).encode(TEXT));
  }

  @Test
  public void testSeededDecodeException() throws Exception {
    assertThrows(PasswordException.class, () -> new SeededAesPbeCrypto(NON_EXISTENT_FILE).decode(TEXT));
  }
}
