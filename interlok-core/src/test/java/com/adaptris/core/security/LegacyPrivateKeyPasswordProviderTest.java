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
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;

import com.adaptris.security.password.Password;

public class LegacyPrivateKeyPasswordProviderTest {

  @Test
  public void testGetPassword() throws Exception {
    LegacyPrivateKeyPasswordProvider pkpp = new LegacyPrivateKeyPasswordProvider();
    assertEquals(getPassword(), new String(pkpp.retrievePrivateKeyPassword()));
    // Do it twice to do all coverage... lame but hey.
    assertEquals(getPassword(), new String(pkpp.retrievePrivateKeyPassword()));
  }

  private String getPassword() throws Exception {
    String result = null;
    InputStream is = this.getClass().getClassLoader().getResourceAsStream("security.properties");
    if (is != null) {
      Properties p = new Properties();
      p.load(is);
      result = Password.decode(p.getProperty("adaptris.privatekey.password"));
    }
    else {
      fail("Couldn't find security.properties");
    }
    return result;
  }
}
