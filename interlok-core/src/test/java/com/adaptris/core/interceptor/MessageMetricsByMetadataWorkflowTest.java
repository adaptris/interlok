/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.interceptor;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.adaptris.core.Channel;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.TimeInterval;

public class MessageMetricsByMetadataWorkflowTest
    extends com.adaptris.interlok.junit.scaffolding.ExampleWorkflowCase {

  static final String DEFAULT_XML_COMMENT = "<!-- The interceptor here exposes statistics about the workflow via JMX\n"
      + "under the ObjectName 'com.adaptris:type=Metrics,adapter=XXX,channel=YYY,workflow=ZZZ,id=Metrics_For_MyWorkflowName'"
      + "\nwhere the adapter/channel/workflow denote the hierarchy for this interceptor."
      + "\nIt records message throughput and total size per timeslice (60 seconds).\n"
      + "keeping that data for 100 timeslices (i.e. a total of 6000 seconds)\n"
      + "\nOnly messages where the specified metadata element ('messageType=ORDER') exists at the end of the"
      + "\nworkflow are recorded."
 + "\n\n"
      + "If you have duplicate Mbean Names, then the adapter may not start properly\n"
      + "Check the Advanced Topics manual for more information" + "\n-->\n";


  @Override
  protected Object retrieveObjectForSampleConfig() {
    Channel c = new Channel();
    StandardWorkflow wf = new StandardWorkflow();
    wf.setUniqueId("MyWorkflowName");
    MessageMetricsInterceptorByMetadata ti = new MessageMetricsInterceptorByMetadata();
    ti.setUniqueId("Metrics_For_MyWorkflowName");
    ti.setMetadataElement(new KeyValuePair("messageType", "ORDER"));
    ti.setTimesliceDuration(new TimeInterval(60L, TimeUnit.SECONDS));
    wf.addInterceptor(ti);
    c.getWorkflowList().add(wf);
    c.setUniqueId(UUID.randomUUID().toString());
    wf.setUniqueId(UUID.randomUUID().toString());
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
