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

import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;

public class PtpConsumerTest extends com.adaptris.interlok.junit.scaffolding.jms.JmsConsumerCase {


  /**
   * @see com.adaptris.core.ExampleConfigCase#retrieveObjectForSampleConfig()
   */
  @Override
  protected Object retrieveObjectForSampleConfig() {
    return retrieveSampleConfig();
  }

  @Override
  protected String createBaseFileName(Object object) {
    ((StandaloneConsumer) object).getConsumer();
    return super.createBaseFileName(object);
  }

  protected StandaloneConsumer retrieveSampleConfig() {
    JmsConnection c = new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616"));
    PtpConsumer pc = new PtpConsumer();

    pc.setQueue("destination");
    MetadataCorrelationIdSource mcs = new MetadataCorrelationIdSource();
    mcs.setMetadataKey("MetadataKey");
    pc.setCorrelationIdSource(mcs);
    c.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    StandaloneConsumer result = new StandaloneConsumer();
    result.setConnection(c);
    result.setConsumer(pc);

    return result;
  }
}
