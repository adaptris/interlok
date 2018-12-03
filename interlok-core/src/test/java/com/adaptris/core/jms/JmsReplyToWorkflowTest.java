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

package com.adaptris.core.jms;

import java.util.UUID;

import com.adaptris.core.Channel;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ExampleWorkflowCase;

/**
 * <p>
 * Tests for JmsReplyToWorkflow.
 * </p>
 */
@SuppressWarnings("deprecation")
public class JmsReplyToWorkflowTest extends ExampleWorkflowCase {

  /**
   * Constructor for JmsReplyToWorkflowTest.
   *
   * @param arg0
   */
  public JmsReplyToWorkflowTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    Channel c = new Channel();
    c.setUniqueId(UUID.randomUUID().toString());
    c.setConsumeConnection(JmsTransactedWorkflowTest.configure(new JmsConnection()));
    c.setProduceConnection(JmsTransactedWorkflowTest.configure(new JmsConnection()));
    JmsReplyToWorkflow workflow = new JmsReplyToWorkflow();
    workflow.setUniqueId(UUID.randomUUID().toString());
    workflow.setProducer(new PtpProducer());
    workflow.setConsumer(new PtpConsumer(new ConfiguredConsumeDestination("Sample_Queue1")));
    c.getWorkflowList().add(workflow);
    return c;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return JmsReplyToWorkflow.class.getName();
  }

  @Override
  protected JmsReplyToWorkflow createWorkflowForGenericTests() {
    return new JmsReplyToWorkflow();
  }



}
