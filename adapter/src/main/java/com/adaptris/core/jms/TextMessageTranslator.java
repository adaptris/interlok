/*
 * $RCSfile: TextMessageTranslator.java,v $
 * $Revision: 1.11 $
 * $Date: 2009/03/25 11:43:37 $
 * $Author: lchan $
 */
package com.adaptris.core.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Translates between <code>AdaptrisMessage</code> and <code>javax.jms.TextMessages</code>. Assumes default platform encoding.
 * </p>
 * <p>
 * In the adapter configuration file this class is aliased as <b>text-message-translator</b> which is the preferred alternative to
 * the fully qualified classname when building your configuration.
 * </p>
 * 
 * @config text-message-translator
 * @license BASIC
 */
@XStreamAlias("text-message-translator")
public final class TextMessageTranslator extends MessageTypeTranslatorImp {

  public TextMessageTranslator() {
    super();
  }

  public TextMessageTranslator(boolean moveMetadata, boolean moveJmsHeaders) {
    super(moveMetadata, moveJmsHeaders);
  }

  /**
   * <p>
   * Translates an <code>AdaptrisMessage</code> into a <code>TextMessage</code>
   * using the default platform character encoding.
   * </p>
   *
   * @param msg the <code>AdaptrisMessage</code> to translate
   * @return a new <code>TextMessage</code>
   * @throws JMSException
   */
  public Message translate(AdaptrisMessage msg) throws JMSException {
    return helper.moveMetadata(msg, session.createTextMessage(msg.getStringPayload()));
  }

  /**
   * <p>
   * Translates a <code>TextMessage</code> into an <code>AdaptrisMessage</code>
   * using the default platform character encoding.
   * </p>
   *
   * @param msg the <code>TextMessage</code> to translate
   * @return an <code>AdaptrisMessage</code>
   * @throws JMSException
   */
  public AdaptrisMessage translate(Message msg) throws JMSException {
    AdaptrisMessage result = currentMessageFactory().newMessage(((TextMessage) msg).getText());
    return helper.moveMetadata(msg, result);
  }
}
