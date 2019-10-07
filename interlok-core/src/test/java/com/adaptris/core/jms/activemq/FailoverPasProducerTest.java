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

package com.adaptris.core.jms.activemq;

import static com.adaptris.core.BaseCase.execute;
import static com.adaptris.core.jms.JmsProducerCase.assertMessages;
import static com.adaptris.core.jms.activemq.EmbeddedActiveMq.createMessage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.PasConsumer;
import com.adaptris.core.jms.PasProducer;
import com.adaptris.core.stubs.MockMessageListener;

public class FailoverPasProducerTest {

  @Rule
  public TestName testName = new TestName();

  @Test
  public void testProduceAndConsume() throws Exception {

    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(broker.getFailoverJmsConnection(false), new PasConsumer(
            new ConfiguredConsumeDestination(testName.getMethodName())));
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    StandaloneProducer standaloneProducer = new StandaloneProducer(broker.getFailoverJmsConnection(false), new PasProducer(
            new ConfiguredProduceDestination(testName.getMethodName())));
    try {
      broker.start();
      execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
      assertMessages(jms, 1);
    }
    finally {
      broker.destroy();
    }
  }


}
