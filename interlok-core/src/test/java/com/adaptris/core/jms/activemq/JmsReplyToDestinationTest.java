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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.jms.JmsConstants;
import com.adaptris.core.jms.JmsReplyToDestination;


public class JmsReplyToDestinationTest
    extends com.adaptris.interlok.junit.scaffolding.ExampleProduceDestinationCase {

  private static final String ANY_OLD_KEY = "ANY_OLD_KEY";
  protected static Log log = LogFactory.getLog(JmsReplyToDestinationTest.class);


  private AdaptrisMessage createMessage(Destination d) throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx");
    msg.addObjectHeader(JmsConstants.OBJ_JMS_REPLY_TO_KEY, d);
    msg.addObjectHeader(ANY_OLD_KEY, d);
    return msg;

  }

  private Queue createTempQueue(EmbeddedActiveMq broker) throws Exception {
    ActiveMQConnection conn = broker.createConnection();
    ActiveMQSession session = (ActiveMQSession) conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
   return session.createTemporaryQueue();
  }

  @Test
  public void testRetrieveDestination() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      Queue tempQueue = createTempQueue(activeMqBroker);
      AdaptrisMessage msg = createMessage(tempQueue);
      JmsReplyToDestination d = new JmsReplyToDestination();
      assertEquals("Queues", tempQueue, d.retrieveJmsDestination(msg));
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  @Test
  public void testRetrieveDestination_ByName() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      Queue tempQueue = createTempQueue(activeMqBroker);
      AdaptrisMessage msg = createMessage(tempQueue);
      JmsReplyToDestination d = new JmsReplyToDestination();
      d.setObjectMetadataKey(ANY_OLD_KEY);
      assertEquals("Queues", tempQueue, d.retrieveJmsDestination(msg));
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  @Test
  public void testGetDestination() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      Queue tempQueue = createTempQueue(activeMqBroker);
      AdaptrisMessage msg = createMessage(tempQueue);
      JmsReplyToDestination d = new JmsReplyToDestination();
      assertEquals(tempQueue.toString(), d.getDestination(msg));
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  @Test
  public void testGetDestination_MetadataDoesNotExist() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      Queue tempQueue = createTempQueue(activeMqBroker);
      AdaptrisMessage msg = createMessage(tempQueue);
      msg.getObjectHeaders().clear();
      JmsReplyToDestination d = new JmsReplyToDestination();
      d.setObjectMetadataKey(ANY_OLD_KEY);
      assertNull(d.getDestination(msg));
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  @Override
  protected ProduceDestination createDestinationForExamples() {
    JmsReplyToDestination dest = new JmsReplyToDestination();
    return dest;
  }

  @Override
  protected String getExampleCommentHeader(Object object) {
    return super.getExampleCommentHeader(object)
        + "<!--\n\nThis ProduceDestination implementation derives its destination from the JMSReplyTo Header"
        + "\nWhen receiving a message via JMS, any JMSReplyTo Header is stored in object metadata."
        + "\nAlthough the key within object metadata is configurable; it should be left blank unless you have a"
        + "\ncustom JMS Consumer implementation which overrides the default object metadata key." + "\n\n-->\n";
  }

}
