/*
 * Copyright 2015 Adaptris Ltd.
 *
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

import com.adaptris.interlok.junit.scaffolding.util.Os;
import org.junit.Assume;
import org.junit.Test;

public class PasswordTest {

  private static final String TEXT = "MYPASSWORD";

  @Test
  public void testMainClass() throws Exception {
    String[] args =
      {
          Password.PORTABLE_PASSWORD, TEXT
      };
    Password.generatePassword(args);
    Password.generatePassword(null);
    Password.generatePassword(new String[0]);
  }

  @Test
  public void testPortable() throws Exception {
    String encoded = Password.encode(TEXT, Password.PORTABLE_PASSWORD);
    assertEquals(TEXT, Password.decode(encoded));
  }

  @Test
  public void testNewPortable() throws Exception {
    String encoded = Password.encode(TEXT, Password.PORTABLE_PASSWORD_2);
    assertEquals(TEXT, Password.decode(encoded));
  }

  @Test
  public void testMicrosoftCrypto() throws Exception {
    Assume.assumeTrue(Os.isFamily(Os.WINDOWS_NT_FAMILY));
    String encoded = Password.encode(TEXT, Password.MSCAPI_STYLE);
    assertEquals(TEXT, Password.decode(encoded));
  }

}
