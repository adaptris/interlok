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

public class ThrottlingInterceptorWorkflowTest extends ExampleWorkflowCase {

  static final String DEFAULT_XML_COMMENT = "<!-- The interceptor here throttles the workflow so that\n"
      + "no more than 60 messages per minute goes through the workflow.\n"
      + "If you have configured multiple interceptors with the same cache-name then\n"
      + "they must share the same timeslice configuration.\n" + "Check the Advanced Topics manual for more information" + "\n-->\n";


  @Override
  protected Object retrieveObjectForSampleConfig() {
    Channel c = new Channel();
    StandardWorkflow wf = new StandardWorkflow();
    ThrottlingInterceptor ti = new ThrottlingInterceptor();
    ti.setMaximumMessages(60);
    ti.setCacheName("60msgsPerMinute");
    ti.setTimeSliceInterval(new TimeInterval(1L, TimeUnit.MINUTES.name()));
    wf.addInterceptor(ti);
    c.getWorkflowList().add(wf);
    c.setUniqueId(UUID.randomUUID().toString());
    wf.setUniqueId(UUID.randomUUID().toString());
    return c;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return "Workflow-with-" + ThrottlingInterceptor.class.getSimpleName();
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
