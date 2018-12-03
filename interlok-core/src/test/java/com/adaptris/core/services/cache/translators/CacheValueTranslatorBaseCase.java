package com.adaptris.core.services.cache.translators;

import javax.jms.JMSException;
import javax.jms.Queue;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BaseCase;
import com.adaptris.core.jms.JmsConstants;

public class CacheValueTranslatorBaseCase extends BaseCase {

  static final String VALUE_TWO = "two";
  static final String VALUE_ONE = "one";
  static final String MY_QUEUE = "myQueue";
  static final String KEY_TWO = "keyTwo";
  static final String KEY_ONE = "keyOne";
  static final String PAYLOAD = "<root xmlns=\"uri:test\"> <element id=\"one\">abc</element> <element id=\"two\">def</element>   <element id=\"three\" marker=\"true\">ghi</element> </root>";

  public CacheValueTranslatorBaseCase(String s) {
    super(s);
  }

  protected AdaptrisMessage createMessage() {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    message.addMetadata(KEY_ONE, VALUE_ONE);
    message.addMetadata(KEY_TWO, VALUE_TWO);
    message.addObjectMetadata(JmsConstants.OBJ_JMS_REPLY_TO_KEY, new Queue() {
      @Override
      public String getQueueName() throws JMSException {
        return MY_QUEUE;
      }
    });
    return message;
  }
}