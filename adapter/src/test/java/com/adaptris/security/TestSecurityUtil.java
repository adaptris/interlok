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

package com.adaptris.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.SecureRandom;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.security.util.SecurityUtil;

/**
 * @author lchan
 *
 */
public class TestSecurityUtil extends SecurityUtil {

  @Before
  public void setUp() {
    SecurityUtil.addProvider();
  }

  @After
  public void tearDown() {
  }


  @Test
  public void testSecureRandomInstance() throws Exception {
    SecureRandom sr = SecurityUtil.getSecureRandom();
    assertEquals(sr, SecurityUtil.getSecureRandom());
    assertTrue(sr == SecurityUtil.getSecureRandom());
  }

  @Test
  public void testGetAlgorithms() throws Exception {
    assertNotNull(SecurityUtil.getAlgorithms());
  }
}
