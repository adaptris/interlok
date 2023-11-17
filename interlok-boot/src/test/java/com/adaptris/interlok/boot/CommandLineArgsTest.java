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

package com.adaptris.interlok.boot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CommandLineArgsTest {


  @BeforeEach
  public void setUp() throws Exception {
  }

  @AfterEach
  public void tearDown() throws Exception {
  }

  @Test
  public void testParse() throws Exception {
    String[] argv =
    {
        "-config", "bootstrap.properties"
    };
    CommandLineArgs args = CommandLineArgs.parse(argv);
    assertFalse(args.hasArgument("-c"));
    assertTrue(args.hasArgument("-c", "-config"));
    assertEquals("bootstrap.properties", args.getArgument("-c", "-config"));
  }

  @Test
  public void testParseNoArgs() throws Exception {
    String[] args = CommandLineArgs.parse(null).render();
    assertEquals(0, args.length);
  }

  @Test
  public void testRender() throws Exception {
    String[] argv =
    {
        "-config", "bootstrap.properties"
    };
    String[] args = CommandLineArgs.parse(argv).render();
    assertEquals(2, args.length);
    assertEquals("bootstrap.properties", args[1]);
    args = CommandLineArgs.parse(new String[]
    {
        "my.properties"
    }).render();
    assertEquals(1, args.length);
    assertEquals("my.properties", args[0]);
  }

  @Test
  public void testRemove() throws Exception {
    String[] argv =
    {
        "-config", "bootstrap.properties"
    };
    String[] args = CommandLineArgs.parse(argv).remove("-config").render();
    assertEquals(0, args.length);
  }

  @Test
  public void testConvertToNormal() throws Exception {
    String[] argv =
    {
        "-first", "first.properties",
        "-second", "second.properties",
        "last.properties"
    };
    String[] args = CommandLineArgs.parse(argv).convertToNormal("-second").convertToNormal("-first").render();
    assertEquals(3, args.length);
    assertEquals("first.properties", args[0]);
    assertEquals("second.properties", args[1]);
    assertEquals("last.properties", args[2]);
  }
}
