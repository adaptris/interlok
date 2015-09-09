package com.adaptris.core.jms;

import javax.jms.JMSException;
import javax.jms.Message;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Default implementation od <code>CorrelationIdSource</code> which does nothing.
 * </p>
 * 
 * @config null-correlation-id-source
 */
@XStreamAlias("null-correlation-id-source")
public class NullCorrelationIdSource implements CorrelationIdSource {

  /**
   * @see CorrelationIdSource#processCorrelationId (AdaptrisMessage, Message)
   */
  public void processCorrelationId(AdaptrisMessage src, Message dest)
      throws JMSException {

    // do nothing...
  }

  /**
   * 
   * @see CorrelationIdSource#processCorrelationId(Message, AdaptrisMessage)
   */
  public void processCorrelationId(Message src, AdaptrisMessage dest)
      throws JMSException {
  }
}
