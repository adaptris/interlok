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

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;

public class PtpProducerTest extends BasicJmsProducerCase {

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  /**
   * @see com.adaptris.core.ExampleConfigCase#retrieveObjectForSampleConfig()
   */
  @Override
  protected Object retrieveObjectForSampleConfig() {
    return retrieveSampleConfig();
  }

  @Override
  protected String createBaseFileName(Object object) {
    PtpProducer p = (PtpProducer) ((StandaloneProducer) object).getProducer();
    return super.createBaseFileName(object);
  }

  private StandaloneProducer retrieveSampleConfig() {
    JmsConnection c = configureForExamples(new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616")));
    c.setClientId(null);
    StandaloneProducer result = new StandaloneProducer(c, configureForExamples(new PtpProducer()));
    return result;
  }

  @Override
  protected DefinedJmsProducer createProducer(ConfiguredProduceDestination dest) {
    return new PtpProducer(dest);
  }

  @Override
  protected JmsConsumerImpl createConsumer(ConfiguredConsumeDestination dest) {
    PtpConsumer ptp = new PtpConsumer();
    ptp.setDestination(dest);
    return ptp;
  }

  @Override
  protected QueueLoopback createLoopback(EmbeddedActiveMq mq, String dest) {
    return new QueueLoopback(mq, dest);
  }
}
