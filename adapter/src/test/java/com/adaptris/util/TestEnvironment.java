/*
 * $Id: TestEnvironment.java,v 1.1 2004/05/26 13:56:33 lchan Exp $
 */

package com.adaptris.util;

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.adaptris.util.system.Environment;

/**
 *
 * @author lchan
 */
public class TestEnvironment {

  private static Log logR = LogFactory.getLog(TestEnvironment.class);

  @Test
  public void testEnvironment() throws Exception {
    Environment e = Environment.getInstance();
    assertTrue("Path environment exists", e.exists("PATH"));
  }

}
