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

package com.adaptris.core.management.properties;

import static com.adaptris.core.management.SystemPropertiesUtilTest.encode;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PropertyResolverTest {

  private static final String DEFAULT_VALUE = "Back At The Chicken Shack 1960";

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testNoDecoding() throws Exception {
    PropertyResolver resolver = PropertyResolver.getDefaultInstance();
    resolver.init();
    assertEquals(DEFAULT_VALUE, resolver.resolve(DEFAULT_VALUE));
  }

  @Test
  public void testNotDecodable() throws Exception {
    PropertyResolver resolver = PropertyResolver.getDefaultInstance();
    resolver.init();
    String cannotDecode = "{testNotDecodable}" + DEFAULT_VALUE;
    assertEquals(cannotDecode, resolver.resolve(cannotDecode));
  }

  @Test
  public void testNoMatch_LeadingSpaces() throws Exception {
    PropertyResolver resolver = PropertyResolver.getDefaultInstance();
    resolver.init();
    String cannotDecode = "  " + encode(DEFAULT_VALUE);
    assertEquals(cannotDecode, resolver.resolve(cannotDecode));
  }

  @Test
  public void testNoMatch_OpenBraceOnly() throws Exception {
    PropertyResolver resolver = PropertyResolver.getDefaultInstance();
    resolver.init();
    String cannotDecode = "{testNoMatch_OpenBraceOnly]" + DEFAULT_VALUE;
    assertEquals(cannotDecode, resolver.resolve(cannotDecode));
  }

  @Test
  public void testNoMatch_CloseBraceOnly() throws Exception {
    PropertyResolver resolver = PropertyResolver.getDefaultInstance();
    resolver.init();
    String cannotDecode = "[testNoMatch_CloseBraceOnly}" + DEFAULT_VALUE;
    assertEquals(cannotDecode, resolver.resolve(cannotDecode));
  }

  @Test
  public void testDecoding() throws Exception {
    PropertyResolver resolver = PropertyResolver.getDefaultInstance();
    resolver.init();
    assertEquals(DEFAULT_VALUE, resolver.resolve(encode(DEFAULT_VALUE)));
  }


}
