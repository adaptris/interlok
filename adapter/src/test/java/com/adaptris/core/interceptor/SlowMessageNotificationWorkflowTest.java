package com.adaptris.core.interceptor;

import java.util.concurrent.TimeUnit;

import com.adaptris.core.Channel;
import com.adaptris.core.ExampleWorkflowCase;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.util.TimeInterval;

public class SlowMessageNotificationWorkflowTest extends ExampleWorkflowCase {

  static final String DEFAULT_XML_COMMENT = "<!-- The interceptor here emits a JMX notification whenever\n"
          + "a message takes > then 30 seconds to pass through the workflow.\n"
          + "You can subscribe to notifications against the the ObjectName \n"
          + "'com.adaptris:type=Notifications,adapter=XXX,channel=YYY,workflow=ZZZ,id=SlowMessages_For_MyWorkflowName'\n"
          + "Check the Advanced Topics manual for more information" + "\n-->\n";

  public SlowMessageNotificationWorkflowTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() {

  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    Channel c = new Channel();
    StandardWorkflow wf = new StandardWorkflow();
    SlowMessageNotification ti =
        new SlowMessageNotification("SlowMessages_For_MyWorkflowName", new TimeInterval(30L,
            TimeUnit.SECONDS));
    wf.addInterceptor(ti);
    c.getWorkflowList().add(wf);
    return c;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return "Workflow-with-" + SlowMessageNotification.class.getSimpleName();
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