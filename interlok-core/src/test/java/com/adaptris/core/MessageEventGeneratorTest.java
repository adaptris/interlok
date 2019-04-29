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

package com.adaptris.core;

public class MessageEventGeneratorTest extends BaseCase {

  public MessageEventGeneratorTest(String name) {
    super(name);
  }

  public void testNullConfirmationId() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage();

    Service s2 = new NullService();

    ServiceList services = new ServiceList();
    services.setOutOfStateHandler(new NullOutOfStateHandler());
    
    services.addService(s2);
    services.doService(msg);

    MessageLifecycleEvent evt = msg.getMessageLifecycleEvent();
    MleMarker mle = evt.getMleMarkers().get(0);

    assertNull(mle.getConfirmationId());
  }

  public void testNullConfirmationIdNoKey() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage();

    Service s2 = new NullService();

    ServiceList services = new ServiceList();
    services.setOutOfStateHandler(new NullOutOfStateHandler());
    
    services.addService(s2);
    services.doService(msg);

    MessageLifecycleEvent evt = msg.getMessageLifecycleEvent();
    MleMarker mle = evt.getMleMarkers().get(0);

    assertNull(mle.getConfirmationId());
  }

  public void testXmlRoundTrip() throws Exception {

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

    Service s2 = new NullService();
    ServiceList services = new ServiceList();
    services.setOutOfStateHandler(new NullOutOfStateHandler());
    
    services.addService(s2);
    services.doService(msg);
    MessageLifecycleEvent input = msg.getMessageLifecycleEvent();
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String xml = m.marshal(input);
    MessageLifecycleEvent output = (MessageLifecycleEvent) m.unmarshal(xml);
    assertRoundtripEquality(input, output);
  }
}
