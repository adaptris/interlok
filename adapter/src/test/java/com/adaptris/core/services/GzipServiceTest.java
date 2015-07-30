/*
 * $RCSfile: GzipServiceTest.java,v $
 * $Revision: 1.6 $
 * $Date: 2008/08/13 13:28:43 $
 * $Author: lchan $
 */
package com.adaptris.core.services;

import java.security.MessageDigest;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.GeneralServiceExample;
import com.adaptris.core.ServiceException;
import com.adaptris.core.stubs.DefectiveMessageFactory;

public class GzipServiceTest extends GeneralServiceExample {

  public static final String LINE = "The quick brown fox jumps over the lazy dog";

  public GzipServiceTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testZipService() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(LINE, "UTF-8");
    execute(new GzipService(), msg);
    assertFalse(MessageDigest.isEqual(LINE.getBytes("UTF-8"), msg.getPayload()));
    execute(new GunzipService(), msg);
    assertEquals("zip then unzip gives same result", LINE, msg
        .getStringPayload());
  }

  public void testZipServiceFailure() throws Exception {
    AdaptrisMessage msg = new DefectiveMessageFactory().newMessage(LINE, "UTF-8");
    try {
    execute(new GzipService(), msg);
      fail();
    }
    catch (ServiceException expected) {
      ;
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new GzipService();
  }

}
