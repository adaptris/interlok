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

import static org.junit.Assert.assertEquals;
import java.util.UUID;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.junit.Test;
import com.adaptris.core.Channel;
import com.adaptris.core.ProduceException;

/**
 * <p>
 * Tests for JmsReplyToWorkflow.
 * </p>
 */
@SuppressWarnings("deprecation")
public class JmsReplyToWorkflowTest
    extends com.adaptris.interlok.junit.scaffolding.ExampleWorkflowCase {



  @Test(expected = ProduceException.class)
  public void testProducerTypeQueue() throws Exception {
    ActiveMQQueue queue = new ActiveMQQueue("dest");
    assertEquals(queue, JmsReplyToWorkflow.ProducerType.QueueProducer.validate(queue));
    JmsReplyToWorkflow.ProducerType.TopicProducer.validate(queue);
  }


  @Test(expected = ProduceException.class)
  public void testProducerTypeTopic() throws Exception {
    ActiveMQTopic topic = new ActiveMQTopic("dest");
    assertEquals(topic, JmsReplyToWorkflow.ProducerType.TopicProducer.validate(topic));
    JmsReplyToWorkflow.ProducerType.QueueProducer.validate(topic);
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
    workflow.setConsumer(new PtpConsumer().withQueue("Sample_Queue1"));
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
