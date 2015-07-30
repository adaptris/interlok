package com.adaptris.core.interceptor;

import java.util.concurrent.TimeUnit;

import com.adaptris.core.Channel;
import com.adaptris.core.ExampleWorkflowCase;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.util.TimeInterval;

public class MessageMetricsWorkflowTest extends ExampleWorkflowCase {

  static final String DEFAULT_XML_COMMENT = "<!-- The interceptor here exposes statistics about the workflow via JMX\n"
      + "under the ObjectName 'com.adaptris:type=Metrics,adapter=XXX,channel=YYY,workflow=ZZZ,id=Metrics_For_MyWorkflowName'"
      + "\nwhere the adapter/channel/workflow denote the hierarchy for this interceptor."
      + "\nIt records message throughput and total size per timeslice (60 seconds)\n"
      + "keeping that data for 100 timeslices (i.e. a total of 6000 seconds)\n"
      + "\nIf you have duplicate Mbean Names, then the adapter may not start properly\n"
      + "Check the Advanced Topics manual for more information" + "\n-->\n";

  public MessageMetricsWorkflowTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() {

  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    Channel c = new Channel();
    StandardWorkflow wf = new StandardWorkflow();
    wf.setUniqueId("MyWorkflowName");
    MessageMetricsInterceptor ti = new MessageMetricsInterceptor();
    ti.setUniqueId("Metrics_For_MyWorkflowName");
    ti.setTimesliceDuration(new TimeInterval(60L, TimeUnit.SECONDS));
    wf.addInterceptor(ti);
    c.getWorkflowList().add(wf);
    return c;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return "Workflow-with-" + MessageMetricsInterceptor.class.getSimpleName();
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