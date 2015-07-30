package com.adaptris.core.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageTranslator;

// abstract factory pattern

/**
 * <p>
 * Interface that translate <code>AdaptrisMessage</code>s to the various type of
 * <code>javax.jms.Message</code>s, and vice versa.
 * </p>
 */
public interface MessageTypeTranslator extends AdaptrisComponent,
    AdaptrisMessageTranslator {

  /**
   * <p>
   * Translates the passed <code>AdaptrisMessage</code> into an instance of a
   * subclass of <code>javax.jms.Message</code>.
   * </p>
   * 
   * @param msg the <code>AdaptrisMessage</code> to translate
   * @return a <code>javax.jms.Message</code>
   * @throws JMSException
   */
  Message translate(AdaptrisMessage msg) throws JMSException;

  /**
   * <p>
   * Translates the passed <code>javax.jms.Message</code> into an instance of
   * <code>AdaptrisMessage</code>.
   * </p>
   * 
   * @param msg the <code>javax.jms.Message</code> to translate
   * @return a <code>AdaptrisMessage</code>
   * @throws JMSException
   */
  AdaptrisMessage translate(Message msg) throws JMSException;

  /**
   * Register the JMS session with this message translator.
   * 
   * @param s the session.
   */
  void registerSession(Session s);

  /**
   * Obtain the JMS session currently registered.
   * 
   * @return the session
   */
  Session currentSession();
}
