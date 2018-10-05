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
package com.adaptris.core.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExecutionTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testConstructors() throws Exception {
    Execution execution = new Execution();
    assertNull(execution.getSource());
    assertNull(execution.getTarget());

    execution = new Execution(new StringPayloadDataInputParameter(), new StringPayloadDataOutputParameter());
    assertNotNull(execution.getSource());
    assertNotNull(execution.getTarget());
    assertEquals(StringPayloadDataInputParameter.class, execution.getSource().getClass());
    assertEquals(StringPayloadDataOutputParameter.class, execution.getTarget().getClass());
  }

}
