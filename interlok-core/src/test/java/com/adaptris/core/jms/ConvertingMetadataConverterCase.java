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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import org.junit.Test;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;

public abstract class ConvertingMetadataConverterCase extends MetadataConverterCase {

  @Test
  public void testConvertFailure() throws Exception {

    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    MetadataConverter mc = createConverter();
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      MetadataCollection metadataCollection = new MetadataCollection();
      metadataCollection.add(new MetadataElement(HEADER, testName.getMethodName()));
      Message jmsMsg = session.createMessage();
      mc.moveMetadata(metadataCollection, jmsMsg);
      assertEquals(testName.getMethodName(), jmsMsg.getStringProperty(HEADER));
    }
    finally {
      broker.destroy();
    }
  }

  @Test
  public void testConvertFailure_Strict() throws Exception {

    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    MetadataConverter mc = createConverter();
    mc.setStrictConversion(true);
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      MetadataCollection metadataCollection = new MetadataCollection();
      metadataCollection.add(new MetadataElement(HEADER, testName.getMethodName()));
      Message jmsMsg = session.createMessage();
      mc.moveMetadata(metadataCollection, jmsMsg);
      fail();
    }
    catch (JMSException expected) {

    }
    finally {
      broker.destroy();
    }
  }

}
