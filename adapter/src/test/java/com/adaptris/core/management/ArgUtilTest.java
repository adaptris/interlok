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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ArgUtilTest {

  private static final String[] ARGV =
  {
      "-param1", "param1",
      "-flag",
 "--param2", "param2"
  };


  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testArgUtilStaticMethods() throws Exception {
    String[] flag = new String[] { "-flag" };
    String[] param = new String[]
    {
      "--param2"
    };
    assertTrue(ArgUtil.hasArgument(ARGV, flag));
    assertEquals("TRUE", ArgUtil.getArgument(ARGV, flag).toUpperCase());
    assertFalse(ArgUtil.hasArgument(ARGV, new String[]
    {
      "-blah"
    }));
    assertTrue(ArgUtil.hasArgument(ARGV, param));
    assertEquals("param2", ArgUtil.getArgument(ARGV, param));
  }


  @Test
  public void testArgUtilInstanceMethods() throws Exception {
    ArgUtil au = ArgUtil.getInstance(ARGV);
    String[] flag = new String[]
    {
      "-flag"
    };
    String[] param = new String[]
    {
      "--param2"
    };
    assertTrue(au.hasArgument(flag));
    assertEquals("TRUE", au.getArgument(flag).toUpperCase());
    assertFalse(ArgUtil.hasArgument(ARGV, new String[]
    {
      "-blah"
    }));
    assertTrue(au.hasArgument(param));
    assertEquals("param2", au.getArgument(param));

  }

}
