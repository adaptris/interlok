package com.adaptris.core;

import static org.junit.Assert.assertNull;
import org.junit.Test;
import com.adaptris.core.util.LifecycleHelper;

public class NullMessageProducerTest {

  @Test
  public void testMessageFactory() throws Exception {
    NullMessageProducer producer = new NullMessageProducer();
    assertNull(producer.getMessageFactory());
  }

  @Test
  public void testEndpoint() throws Exception {
    NullMessageProducer producer = new NullMessageProducer();
    assertNull(producer.endpoint(null));
  }

  @Test
  public void testRequest() throws Exception {
    NullMessageProducer producer = new NullMessageProducer();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try {
      LifecycleHelper.prepare(producer);
      LifecycleHelper.initAndStart(producer);
      assertNull(producer.request(msg));
      assertNull(producer.request(msg, 10L));
    } finally {
      LifecycleHelper.stopAndClose(producer);
    }
  }

  @Test
  public void testProduce() throws Exception {
    NullMessageProducer producer = new NullMessageProducer();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try {
      LifecycleHelper.prepare(producer);
      LifecycleHelper.initAndStart(producer);
      producer.produce(msg);
    } finally {
      LifecycleHelper.stopAndClose(producer);
    }
  }

}
