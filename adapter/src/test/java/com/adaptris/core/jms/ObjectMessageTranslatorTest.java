/*
 * $RCSfile: ObjectMessageTranslatorTest.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/04/17 07:56:38 $
 * $Author: lchan $
 */
package com.adaptris.core.jms;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;

public class ObjectMessageTranslatorTest extends MessageTypeTranslatorCase {

  public ObjectMessageTranslatorTest(String name) {
    super(name);
  }

  public void testObjectMessageToAdaptrisMessage() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    MessageTypeTranslatorImp trans = new ObjectMessageTranslator();
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);

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
      broker.destroy();
    }
  }

  public void testAdaptrisMessageToObjectMessage() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    MessageTypeTranslatorImp trans = new ObjectMessageTranslator();
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
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
      broker.destroy();

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
