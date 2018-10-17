/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.naming.adapter;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.NullConnection;

public class AdapterSchemeJndiNameCreatorTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test(expected = AdapterNamingException.class)
  public void testCreateName_NotConnection() throws Exception {
    AdapterSchemeJndiNameCreator creator = new AdapterSchemeJndiNameCreator();
    creator.createName(new Object());
  }

  @Test
  public void testCreateName() throws Exception {
    AdapterSchemeJndiNameCreator creator = new AdapterSchemeJndiNameCreator();
    String s = creator.createName(new NullConnection());
    assertTrue(s.startsWith("adapter:comp/env/"));
  }

}
