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
package com.adaptris.jdbc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DefaultStatementCreatorTest {

  @Test
  public void testCreate() {
    DefaultStatementCreator creator = new DefaultStatementCreator();
    assertEquals("{ CALL mystoredProcedure(?, ?); }", creator.createCall("mystoredProcedure", 2));
    assertEquals("{ CALL mystoredProcedure(); }", creator.createCall("mystoredProcedure", 0));
  }
}
