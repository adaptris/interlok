package com.adaptris.core.services;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.GeneralServiceExample;


public class StopProcessingServiceTest extends GeneralServiceExample {

  public StopProcessingServiceTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testService() throws Exception {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    execute(new StopProcessingService(), msg);
    assertEquals("Stop processing", CoreConstants.STOP_PROCESSING_VALUE, msg.getMetadataValue(CoreConstants.STOP_PROCESSING_KEY));
    assertEquals("Skip producer", CoreConstants.STOP_PROCESSING_VALUE, msg.getMetadataValue(CoreConstants.KEY_WORKFLOW_SKIP_PRODUCER));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StopProcessingService();
  }

}
