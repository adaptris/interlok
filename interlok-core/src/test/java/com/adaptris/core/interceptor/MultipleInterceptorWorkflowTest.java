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
import com.adaptris.core.ExampleWorkflowCase;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.util.TimeInterval;

public class MultipleInterceptorWorkflowTest extends ExampleWorkflowCase {

  static final String DEFAULT_XML_COMMENT = "<!-- This workflow example simple shows multiple workflow interceptors configured\n"
      + "The MessageMetricsInterceptor exposes statistics about the workflow via JMX under the\n"
      + " name 'com.adaptris:type=Metrics, uid=Metrics_For_MyWorkflowName'\n"
      + "The ThrottlingInterceptor enforces no more than 60 messages per minute goes through the workflow.\n"
      + "Check the Advanced Topics manual for more information" + "\n-->\n";

  public MultipleInterceptorWorkflowTest(java.lang.String testName) {
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
    ThrottlingInterceptor ti2 = new ThrottlingInterceptor();
    ti2.setMaximumMessages(60);
    ti2.setCacheName("60msgsPerMinute");
    ti2.setTimeSliceInterval(new TimeInterval(1L, TimeUnit.MINUTES.name()));
    wf.addInterceptor(ti2);
    c.setUniqueId(UUID.randomUUID().toString());
    wf.setUniqueId(UUID.randomUUID().toString());
    c.getWorkflowList().add(wf);
    return c;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return "Workflow-with-MultipleInterceptors";
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
