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

package com.adaptris.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.adaptris.security.password.Password;

public class ConfiguredPrivateKeyPasswordProviderTest {

  @Test
  public void testGetPassword() throws Exception {
    ConfiguredPrivateKeyPasswordProvider pkpp = new ConfiguredPrivateKeyPasswordProvider("ABCDE");
    assertEquals("ABCDE", new String(pkpp.retrievePrivateKeyPassword()));
    pkpp = new ConfiguredPrivateKeyPasswordProvider(Password.encode("ABCDE", Password.PORTABLE_PASSWORD));
    assertEquals("ABCDE", new String(pkpp.retrievePrivateKeyPassword()));
  }

  @Test
  public void testGetNullPassword() throws Exception {
    ConfiguredPrivateKeyPasswordProvider pkpp = new ConfiguredPrivateKeyPasswordProvider(null);
    assertNull(pkpp.retrievePrivateKeyPassword());
  }

  @Test
  public void testGetEmpty() throws Exception {
    ConfiguredPrivateKeyPasswordProvider pkpp = new ConfiguredPrivateKeyPasswordProvider("");
    assertNull(pkpp.retrievePrivateKeyPassword());
  }
}
