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
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;

public class ObjectMessageTranslatorTest extends GenericMessageTypeTranslatorCase {

  @BeforeClass
  public static void setUpAll() throws Exception {
    activeMqBroker = new EmbeddedActiveMq();
    activeMqBroker.start();
  }
  
  @AfterClass
  public static void tearDownAll() throws Exception {
    if(activeMqBroker != null)
      activeMqBroker.destroy();
  }
  
  @Test
  public void testObjectMessageToAdaptrisMessage() throws Exception {
    MessageTypeTranslatorImp trans = new ObjectMessageTranslator();
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);

      ObjectMessage jmsMsg = session.createObjectMessage();
      Exception e = new Exception("This is an Exception that was serialized");
      e.fillInStackTrace();
      jmsMsg.setObject(e);
      addProperties(jmsMsg);
      start(trans, session);
      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertMetadata(msg);
      Object o = readException(msg);
      assertException(e, (Exception) o);
    }
    finally {
      stop(trans);
    }
  }

  @Test
  public void testAdaptrisMessageToObjectMessage() throws Exception {
    MessageTypeTranslatorImp trans = new ObjectMessageTranslator();
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      Exception e = new Exception("This is an Exception that was serialized");
      write(e, msg);
      addMetadata(msg);
      Message jmsMsg = trans.translate(msg);
      assertTrue("jmsMsg instanceof ObjectMessage", jmsMsg instanceof ObjectMessage);
      assertJmsProperties(jmsMsg);
      assertException(e, (Exception) ((ObjectMessage) jmsMsg).getObject());
    }
    finally {
      stop(trans);
    }
  }

  /**
   * @see com.adaptris.core.jms.MessageTypeTranslatorCase#createMessage(javax.jms.Session)
   */
  @Override
  protected Message createMessage(Session session) throws Exception {
    return session.createObjectMessage();
  }

  /**
   * @see com.adaptris.core.jms.MessageTypeTranslatorCase#createTranslator()
   */
  @Override
  protected MessageTypeTranslatorImp createTranslator() throws Exception {
    return new ObjectMessageTranslator();
  }

  protected static Exception readException(AdaptrisMessage msg) throws IOException, ClassNotFoundException {
    InputStream in = msg.getInputStream();
    ObjectInputStream objIn = new ObjectInputStream(in);
    Object o = objIn.readObject();
    objIn.close();
    in.close();
    return (Exception) o;
  }

  protected static void write(Exception e, AdaptrisMessage msg) throws IOException {
    e.fillInStackTrace();
    OutputStream out = msg.getOutputStream();
    ObjectOutputStream objOut = new ObjectOutputStream(out);
    objOut.writeObject(e);
    objOut.close();
    out.close();
  }

  protected static void assertException(Exception e1, Exception e2) {
    assertEquals("Test Exception", e1.getMessage(), e2.getMessage());
    StackTraceElement[] s1 = e1.getStackTrace();
    StackTraceElement[] s2 = e2.getStackTrace();
    for (int i = 0; i < s1.length; i++) {
      assertEquals(s1[i].getFileName(), s2[i].getFileName());
      assertEquals(s1[i].getClassName(), s2[i].getClassName());
      assertEquals(s1[i].getLineNumber(), s2[i].getLineNumber());
      assertEquals(s1[i].getMethodName(), s2[i].getMethodName());
    }
  }
}
