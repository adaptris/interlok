/*
 * Copyright 2017 Adaptris Ltd.
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
import static org.junit.jupiter.api.Assertions.fail;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;

public abstract class ConvertingMetadataConverterCase extends MetadataConverterCase {

  @BeforeAll
  public static void setUpAll() throws Exception {
    activeMqBroker = new EmbeddedActiveMq();
    activeMqBroker.start();
  }
  
  @AfterAll
  public static void tearDownAll() throws Exception {
    if(activeMqBroker != null)
      activeMqBroker.destroy();
  }
  
  @Test
  public void testConvertFailure(TestInfo info) throws Exception {
    MetadataConverter mc = createConverter();
    Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
    MetadataCollection metadataCollection = new MetadataCollection();
    metadataCollection.add(new MetadataElement(HEADER, info.getDisplayName()));
    Message jmsMsg = session.createMessage();
    mc.moveMetadata(metadataCollection, jmsMsg);
    assertEquals(info.getDisplayName(), jmsMsg.getStringProperty(HEADER));
  }

  @Test
  public void testConvertFailure_Strict(TestInfo info) throws Exception {
    MetadataConverter mc = createConverter();
    mc.setStrictConversion(true);
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      MetadataCollection metadataCollection = new MetadataCollection();
      metadataCollection.add(new MetadataElement(HEADER, info.getDisplayName()));
      Message jmsMsg = session.createMessage();
      mc.moveMetadata(metadataCollection, jmsMsg);
      fail();
    }
    catch (JMSException expected) {

    }
  }

}
