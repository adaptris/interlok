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

package com.adaptris.core.management;

import static com.adaptris.core.management.Constants.SYSTEM_PROPERTY_PREFIX;
import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.security.password.Password;

public class SystemPropertiesUtilTest {

  private static final String DEFAULT_VALUE = "Back At The Chicken Shack 1960";

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testAddSystemProperties() throws Exception {
    Properties p = new Properties();
    p.setProperty(SYSTEM_PROPERTY_PREFIX + "zzlc.plain", DEFAULT_VALUE);
    SystemPropertiesUtil.addSystemProperties(p);
    assertEquals(DEFAULT_VALUE, System.getProperty("zzlc.plain"));
  }


  @Test
  public void testAddEncodingSystemProperty() throws Exception {
    Properties p = new Properties();
    p.setProperty(SYSTEM_PROPERTY_PREFIX + "zzlc.encrypted", encode(DEFAULT_VALUE));
    SystemPropertiesUtil.addSystemProperties(p);
    assertEquals(DEFAULT_VALUE, System.getProperty("zzlc.encrypted"));
  }

  public static String encode(String thePassword) throws Exception {
    return "{password}" + Password.encode(thePassword, Password.PORTABLE_PASSWORD);
  }
}
