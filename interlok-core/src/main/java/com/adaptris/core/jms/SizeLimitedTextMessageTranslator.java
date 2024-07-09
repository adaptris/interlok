package com.adaptris.core.jms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Translates between <code>AdaptrisMessage</code> and
 * <code>javax.jms.TextMessages</code>. Assumes default platform encoding.
 * </p>
 * <p>
 * In the adapter configuration file this class is aliased as
 * <b>text-message-translator</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 * <p>
 * You must specify a maximum size in bytes that the serialized message may not exceed.
 * If it does, then a JMSException is thrown.
 * </p>
 * <p>
 * WARNING:  This translator may not work with all JMS vendors due to their messages not being serializable.
 * One such case is ActiveMQ.
 * </p>
 * 
 * @config text-message-translator
 * 
 */
@XStreamAlias("size-limited-text-message-translator")
@DisplayOrder(order = { "maxSizeBytes", "metadataFilter", "moveMetadata", "moveJmsHeaders", "reportAllErrors", "limitExceededExceptionMessage" })
public class SizeLimitedTextMessageTranslator extends MessageTypeTranslatorImp{
  
  /**
   * This is the maximum size in bytes that the JMS message when serialized may not exceed.
   */
  @Min(1)
  @NotNull
  @Getter
  @Setter
  private long maxSizeBytes;
  /**
   * A custom exception message that will be printed to the log file should a message size exceed the maximum allowed.
   */
  @AdvancedConfig()
  @Getter
  @Setter
  private String limitExceededExceptionMessage;
  
  private static String DEFAULT_LIMIT_EXCEEDED_EXCEPTION_MESSAGE = "Max message size exceeded.";
  
  public SizeLimitedTextMessageTranslator() {
    super();
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
  @Override
  public Message translate(AdaptrisMessage msg) throws JMSException {
    Message message = helper.moveMetadata(msg, session.createTextMessage(msg.getContent()));
    
    byte[] serializeMessage = serializeMessage(message);
    log.trace("JMS message is of size: " + serializeMessage.length);
    
    if(serializeMessage.length > getMaxSizeBytes()) {
      throw new JMSException(limitExceededExceptionMessage() + ". " + serializeMessage.length + " bytes");
    }
    
    return message;
  }

  private static byte[] serializeMessage(Message message) throws JMSException {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(message);
      oos.flush();
      return bos.toByteArray();
    } catch (IOException e) {
      throw new JMSException("Error serializing message: " + e.getMessage());
    }
  }

  private String limitExceededExceptionMessage() {
    return StringUtils.defaultIfBlank(getLimitExceededExceptionMessage(), DEFAULT_LIMIT_EXCEEDED_EXCEPTION_MESSAGE);
  }
  
  /**
   * <p>
   * Translates a <code>TextMessage</code> into an <code>AdaptrisMessage</code>
   * using the default platform character encoding.
   * </p>
   *
   * @param msg
   *          the <code>TextMessage</code> to translate
   * @return an <code>AdaptrisMessage</code>
   * @throws JMSException
   */
  @Override
  public AdaptrisMessage translate(Message msg) throws JMSException {
    AdaptrisMessage result = currentMessageFactory().newMessage(((TextMessage) msg).getText());
    return helper.moveMetadata(msg, result);
  }

}
