package com.adaptris.core.jms;

import javax.jms.JMSException;
import javax.jms.Message;

import com.adaptris.core.AdaptrisMessage;

/**
 * <p>
 * Provides a <code>JMSCorrelationId</code> for the
 * <code>javax.jms.Message</code>.
 * </p>
 */
public interface CorrelationIdSource {

  /**
   * <p>
   * Provides a <code>JMSCorrelationId</code> for the
   * <code>javax.jms.Message</code>.
   * </p>
   * 
   * @param src the <code>AdaptrisMessage</code> being processed
   * @param dest the <code>javax.jms.Message</code> to send
   * @throws JMSException if encoutered setting <code>JMSCorrelationId</code>
   */
  void processCorrelationId(AdaptrisMessage src, Message dest)
      throws JMSException;

  /**
   * <p>
   * Provides a <code>JMSCorrelationId</code> for the
   * <code>javax.jms.Message</code>.
   * </p>
   * 
   * @param dest the <code>AdaptrisMessage</code> to be processed
   * @param src the <code>javax.jms.Message</code> that has been received
   * @throws JMSException if encoutered setting <code>JMSCorrelationId</code>
   */
  void processCorrelationId(Message src, AdaptrisMessage dest)
      throws JMSException;
}
