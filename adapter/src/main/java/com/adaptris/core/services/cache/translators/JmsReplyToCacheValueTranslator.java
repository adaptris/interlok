package com.adaptris.core.services.cache.translators;

import java.io.Serializable;

import javax.jms.Destination;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.jms.JmsConstants;
import com.adaptris.core.services.cache.CacheValueTranslator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link CacheValueTranslator} that retrieves and sets the JMSReplyTo destination of a message.
 * 
 * <p>
 * Note that this can only be used with caches that allow insertion of non-{@link Serializable} objects as the JMS destination
 * interface is not serializable.
 * </p>
 * 
 * @config jms-replyto-cache-value-translator
 * 
 * 
 * @author stuellidge
 */
@XStreamAlias("jms-replyto-cache-value-translator")
public class JmsReplyToCacheValueTranslator implements CacheValueTranslator<Destination> {

  /**
   * gets the JMSReplyTo destination from the message
   */
  @Override
  public Destination getValueFromMessage(AdaptrisMessage msg) throws CoreException {
    if (msg.getObjectHeaders().containsKey(JmsConstants.OBJ_JMS_REPLY_TO_KEY)) {
      return (Destination) msg.getObjectHeaders().get(JmsConstants.OBJ_JMS_REPLY_TO_KEY);
    }
    return null;
  }

  /**
   * sets the JMSReplyTo destination on the message
   */
  @Override
  public void addValueToMessage(AdaptrisMessage msg, Destination value) throws CoreException {
    msg.addObjectHeader(JmsConstants.OBJ_JMS_REPLY_TO_KEY, value);
  }

}
