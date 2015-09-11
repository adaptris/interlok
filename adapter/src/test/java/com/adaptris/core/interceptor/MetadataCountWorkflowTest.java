package com.adaptris.core.interceptor;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.Channel;
import com.adaptris.core.ExampleWorkflowCase;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.util.TimeInterval;

public class MetadataCountWorkflowTest extends ExampleWorkflowCase {

  static final String DEFAULT_XML_COMMENT = "<!-- The interceptor here exposes statistics about the workflow via JMX\n"
      + "under the ObjectName 'com.adaptris:type=Metrics,adapter=XXX,channel=YYY,workflow=ZZZ,id=Metrics_For_MyWorkflowName'"
      + "\nwhere the adapter/channel/workflow denote the hierarchy for this interceptor."
      + "\nIt records the running total of instances of particular values for the metadata key configured\n"
      + "keeping that data for 100 timeslices (i.e. a total of 6000 seconds)\n"
      + "\nEach message is checked for 'metadatakey1', the value of which is used to key the statistics, which is incremented."
      + "\n\nSo, if in a given time period you have 5 messages; 3 of which contain 'metadatakey1=Alice' 2 have 'metadatakey1=Bob'"
      + "\nthen the count for 'Alice' and 'Bob' will be '3' and '2' respectively when you query the statistics."
 + "\n\n"
      + "If you have duplicate Mbean Names, then the adapter may not start properly\n"
      + "Check the Advanced Topics manual for more information" + "\n-->\n";

  public MetadataCountWorkflowTest(java.lang.String testName) {
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
    MetadataCountInterceptor ti = new MetadataCountInterceptor("metadatakey1");
    ti.setUniqueId("Metrics_For_MyWorkflowName");
    ti.setTimesliceDuration(new TimeInterval(60L, TimeUnit.SECONDS));
    wf.addInterceptor(ti);
    c.setUniqueId(UUID.randomUUID().toString());
    wf.setUniqueId(UUID.randomUUID().toString());
    c.getWorkflowList().add(wf);
    return c;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return "Workflow-with-" + MetadataCountInterceptor.class.getSimpleName();
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