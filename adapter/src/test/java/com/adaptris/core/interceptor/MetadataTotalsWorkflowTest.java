package com.adaptris.core.interceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.Channel;
import com.adaptris.core.ExampleWorkflowCase;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.util.TimeInterval;

public class MetadataTotalsWorkflowTest extends ExampleWorkflowCase {

  static final String DEFAULT_XML_COMMENT = "<!-- The interceptor here exposes statistics about the workflow via JMX\n"
      + "under the ObjectName 'com.adaptris:type=Metrics,adapter=XXX,channel=YYY,workflow=ZZZ,id=Metrics_For_MyWorkflowName'"
      + "\nwhere the adapter/channel/workflow denote the hierarchy for this interceptor."
      + "\nIt records the running total of metadata values for the metadata keys configured\n"
      + "keeping that data for 100 timeslices (i.e. a total of 6000 seconds)\n"
      + "\nEach message that has an integer value stored against 'metadatakey1' will have that value captured"
      + "\nand added to any existing count for 'metadatakey1'. The same applies for 'metadatakey2'"
      + "\n\nSo, if you have 5 messages in the same time period, 3 of which have 'metadatakey1=10' and 2 have 'metadatakey1=20'"
      + "\nthen the value for 'metadatakey1' is 70 when you query the statistics, and 0 for 'metadatakey2' "
 + "\n\n"
      + "If you have duplicate Mbean Names, then the adapter may not start properly\n"
      + "Check the Advanced Topics manual for more information" + "\n-->\n";

  public MetadataTotalsWorkflowTest(java.lang.String testName) {
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
    MetadataTotalsInterceptor ti = new MetadataTotalsInterceptor(new ArrayList(Arrays.asList(new String[]
    {
        "metadatakey1", "metadatakey2"
    })));
    ti.setUniqueId("Metrics_For_MyWorkflowName");
    ti.setTimesliceDuration(new TimeInterval(60L, TimeUnit.SECONDS));
    wf.addInterceptor(ti);
    c.getWorkflowList().add(wf);
    return c;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return "Workflow-with-" + MetadataTotalsInterceptor.class.getSimpleName();
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