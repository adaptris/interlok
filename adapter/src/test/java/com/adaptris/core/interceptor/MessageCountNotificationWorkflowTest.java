package com.adaptris.core.interceptor;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.Channel;
import com.adaptris.core.ExampleWorkflowCase;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.util.TimeInterval;

public class MessageCountNotificationWorkflowTest extends ExampleWorkflowCase {

  static final String DEFAULT_XML_COMMENT = "<!-- The interceptor here emits a JMX notification whenever\n"
      + "the message count first exceeds 10 for a given time-interval (30 seconds)\n"
      + "The text of the message is 'Message Count Below Threshold'.\n"
      + "A maximum of 5 notifications are emitted if the message count exceeds that for 150 seconds.\n"
      + "Once the message count dips below 10, then a further notification is sent \n"
      + "'Message Count Below Threshold'; again a maximum of 5 notifications are sent\n"
      + "if the message count stays below that for 150 seconds\n\n"
          + "You can subscribe to notifications against the the ObjectName \n"
          + "'com.adaptris:type=Notifications,adapter=XXX,channel=YYY,workflow=ZZZ,id=MessageCount_For_MyWorkflowName'\n"
          + "Check the Advanced Topics manual for more information" + "\n-->\n";

  public MessageCountNotificationWorkflowTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() {

  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    Channel c = new Channel();
    StandardWorkflow wf = new StandardWorkflow();
    MessageCountNotification ti = new MessageCountNotification("MessageCount_For_MyWorkflowName");
    ti.setTimesliceDuration(new TimeInterval(30L, TimeUnit.SECONDS));
    ti.setMessageCount(10);
    ti.setMaxNotifications(5);
    wf.addInterceptor(ti);
    c.setUniqueId(UUID.randomUUID().toString());
    wf.setUniqueId(UUID.randomUUID().toString());
    c.getWorkflowList().add(wf);
    return c;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return "Workflow-with-" + MessageCountNotification.class.getSimpleName();
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