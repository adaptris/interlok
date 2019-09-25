package com.adaptris.core.services.cache.translators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import javax.jms.JMSException;
import javax.jms.Queue;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.jms.JmsConstants;
import com.adaptris.core.services.cache.CacheValueTranslator;

public class JmsReplyToCacheValueTranslatorTest extends CacheValueTranslatorBaseCase {

  @Test
  public void testGetValueFromMessage() throws Exception {
    AdaptrisMessage msg = createMessage();
    JmsReplyToCacheValueTranslator translator = new JmsReplyToCacheValueTranslator();
    assertEquals("myQueue", ((Queue) translator.getValueFromMessage(msg)).getQueueName());
  }

  @Test
  public void testGetNoReplyTo() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    JmsReplyToCacheValueTranslator translator = new JmsReplyToCacheValueTranslator();
    assertNull(translator.getValueFromMessage(msg));
  }

  @Test
  public void testAddValueToMessage() throws Exception {
    AdaptrisMessage message = createMessage();
    JmsReplyToCacheValueTranslator translator = new JmsReplyToCacheValueTranslator();
    translator.addValueToMessage(message, new Queue() {
      @Override
      public String getQueueName() throws JMSException {
        return "HelloWorld";
      }
    });
    assertEquals("HelloWorld", ((Queue) message.getObjectHeaders().get(JmsConstants.OBJ_JMS_REPLY_TO_KEY)).getQueueName());
  }

  @Test
  public void testAddInvalidValueToMessage() throws Exception {
    AdaptrisMessage message = createMessage();
    CacheValueTranslator translator = new JmsReplyToCacheValueTranslator();
    try {
      translator.addValueToMessage(message, new Object());
      fail("Successfully added an object that isn't a javax.jms.Destination");
    }
    catch (ClassCastException expected) {
      ;
    }

  }
}
