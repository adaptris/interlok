package com.adaptris.core.interceptor;

import java.util.concurrent.TimeUnit;

import com.adaptris.core.Channel;
import com.adaptris.core.ExampleWorkflowCase;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.util.TimeInterval;

public class MessageThresholdNotificationWorkflowTest extends ExampleWorkflowCase {

  static final String DEFAULT_XML_COMMENT = "<!-- The interceptor here emits a JMX notification whenever\n"
          + "the message count exceeds 20 for a given time-interval (30 seconds)\n"
          + "In addition to message counts; you can emit notifications on error counts\n"
          + "and total message size."
          + "You can subscribe to notifications against the the ObjectName \n"
          + "'com.adaptris:type=Notifications,adapter=XXX,channel=YYY,workflow=ZZZ,id=MessageCount_For_MyWorkflowName'\n"
          + "Check the Advanced Topics manual for more information" + "\n-->\n";

  public MessageThresholdNotificationWorkflowTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() {

  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    Channel c = new Channel();
    StandardWorkflow wf = new StandardWorkflow();
    MessageThresholdNotification ti =
        new MessageThresholdNotification("MessageCount_For_MyWorkflowName");
    ti.setTimesliceDuration(new TimeInterval(30L, TimeUnit.SECONDS));
    ti.setCountThreshold(20L);
    wf.addInterceptor(ti);
    c.getWorkflowList().add(wf);
    return c;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return "Workflow-with-" + MessageThresholdNotification.class.getSimpleName();
  }

  @Override
  protected StandardWorkflow createWorkflowForGenericTests() {
    return new StandardWorkflow();
  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj) + DEFAULT_XML_COMMENT;
  }
}