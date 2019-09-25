package com.adaptris.core.services.cache.translators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import javax.jms.JMSException;
import javax.jms.Queue;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.jms.JmsConstants;

public class ObjectMetadataCacheValueTranslatorTest extends CacheValueTranslatorBaseCase {

  @Test
  public void testGetValueFromMessage() throws Exception {
    AdaptrisMessage msg = createMessage();
    // We can just reuse the OBJ_JMS_REPLY_TO_KEY metadata
    ObjectMetadataCacheValueTranslator translator = new ObjectMetadataCacheValueTranslator();
    translator.setMetadataKey(JmsConstants.OBJ_JMS_REPLY_TO_KEY);
    assertEquals("myQueue", ((Queue) translator.getValueFromMessage(msg)).getQueueName());
  }


  @Test
  public void testGetValueFromMessage_NonExistent() throws Exception {
    AdaptrisMessage msg = createMessage();
    // We can just reuse the OBJ_JMS_REPLY_TO_KEY metadata
    ObjectMetadataCacheValueTranslator translator = new ObjectMetadataCacheValueTranslator();
    translator.setMetadataKey("MyNonExistentKey");
    assertNull(translator.getValueFromMessage(msg));
  }

  @Test
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
    assertEquals("HelloWorld", ((Queue) message.getObjectHeaders().get(JmsConstants.OBJ_JMS_REPLY_TO_KEY)).getQueueName());
  }
}
