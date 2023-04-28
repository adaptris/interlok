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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;

public class FailoverPtpProducerTest extends FailoverJmsProducerCase {

  @Test
  public void testBug845() throws Exception {
    Object input = retrieveObjectForSampleConfig();
    String xml = defaultMarshaller.marshal(input);
    StandaloneProducer output = (StandaloneProducer) defaultMarshaller.unmarshal(xml);
    FailoverJmsConnection unmarshalled = (FailoverJmsConnection) output.getConnection();
    assertEquals(2, unmarshalled.getConnections().size());
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StandaloneProducer(createFailoverConfigExample(true),
        new PtpProducer().withQueue("targetQueue"));

  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-Failover";
  }

  static FailoverJmsConnection createFailoverConfigExample(boolean isPtp) {
    FailoverJmsConnection result = null;
    result = new FailoverJmsConnection(new ArrayList<>(Arrays.asList(new JmsConnection[]
    {
        new JmsConnection(new BasicActiveMqImplementation("tcp://SomeUnclusteredBroker:9999")),
        new JmsConnection(new BasicActiveMqImplementation("tcp://SomeUnclusteredBroker:9998"))
    })));
    result.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    return result;
  }

}
