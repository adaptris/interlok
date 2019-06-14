package com.adaptris.core.services.cache.translators;

import javax.jms.JMSException;
import javax.jms.Queue;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.jms.JmsConstants;

@SuppressWarnings("deprecation")
public class ObjectMetadataCacheValueTranslatorTest extends CacheValueTranslatorBaseCase {


  public ObjectMetadataCacheValueTranslatorTest(String s) {
    super(s);
  }

  public void testGetValueFromMessage() throws Exception {
    AdaptrisMessage msg = createMessage();
    // We can just reuse the OBJ_JMS_REPLY_TO_KEY metadata
    ObjectMetadataCacheValueTranslator translator = new ObjectMetadataCacheValueTranslator();
    translator.setMetadataKey(JmsConstants.OBJ_JMS_REPLY_TO_KEY);
    assertEquals("myQueue", ((Queue) translator.getValueFromMessage(msg)).getQueueName());
  }

  public void testAddValueToMessage() throws Exception {
    AdaptrisMessage message = createMessage();
    ObjectMetadataCacheValueTranslator translator = new ObjectMetadataCacheValueTranslator();
    translator.setMetadataKey(JmsConstants.OBJ_JMS_REPLY_TO_KEY);
    translator.addValueToMessage(message, new Queue() {
      @Override
      public String getQueueName() throws JMSException {
        return "HelloWorld";
      }
    });
    assertEquals("HelloWorld", ((Queue)message.getObjectMetadata().get(JmsConstants.OBJ_JMS_REPLY_TO_KEY)).getQueueName());
  }
}
