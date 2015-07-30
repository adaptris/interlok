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
