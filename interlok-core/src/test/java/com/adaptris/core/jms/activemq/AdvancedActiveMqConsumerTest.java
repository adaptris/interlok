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

import static com.adaptris.core.jms.activemq.AdvancedActiveMqImplementationTest.createImpl;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.PtpConsumer;
import com.adaptris.core.jms.UrlVendorImplementation;
import com.adaptris.util.KeyValuePair;

public class AdvancedActiveMqConsumerTest extends BasicActiveMqConsumerTest {

  @Override
  protected String createBaseFileName(Object object) {
    return ((StandaloneConsumer) object).getConsumer().getClass().getName() + "-AdvancedActiveMQ";
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {

    JmsConnection connection = new JmsConnection();
    PtpConsumer producer = new PtpConsumer();
    ConfiguredConsumeDestination dest = new ConfiguredConsumeDestination(
        "destination");

    producer.setDestination(dest);
    UrlVendorImplementation vendorImpl = createImpl();
    vendorImpl.setBrokerUrl(BasicActiveMqImplementationTest.PRIMARY);
    connection.setUserName("BrokerUsername");
    connection.setPassword("BrokerPassword");
    connection.setVendorImplementation(vendorImpl);
    StandaloneConsumer result = new StandaloneConsumer();
    result.setConnection(connection);
    result.setConsumer(producer);

    return result;
  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj) + "<!-- Not all elements within the VendorImplementation are required. \n"
        + "\nThis example explicitly configures elements to show you possible configuration \n"
        + "Use of these values may cause failure within your ActiveMQ environment\n"
        + "Check the ActiveMQ documentation for the exact meanings of each field.\n-->\n";
  }

  @Override
  protected BasicActiveMqImplementation createVendorImpl() {
    AdvancedActiveMqImplementation result = new AdvancedActiveMqImplementation();
    result.getConnectionFactoryProperties().addKeyValuePair(new KeyValuePair("DisableTimeStampsByDefault", "true"));
    result.getConnectionFactoryProperties().addKeyValuePair(new KeyValuePair("NestedMapAndListEnabled", "true"));
    return result;
  }
}
