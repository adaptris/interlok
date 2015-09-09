package com.adaptris.core.stubs;

import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.ProduceException;

/**
 * Simple stub that wraps a MockMessageProducer. Probably of use when you want
 * to test using a StandaloneConsumer.
 *
 * @author lchan
 * @author $Author: $
 */
public class MockMessageListener implements AdaptrisMessageListener, MessageCounter {
  private MockMessageProducer producer;

  private long waitTime = -1;

  public MockMessageListener() {
    producer = new MockMessageProducer();
  }

  public MockMessageListener(long waitTime) {
    this();
    this.waitTime = waitTime;
  }

  public void onAdaptrisMessage(AdaptrisMessage msg) {
    try {
      producer.produce(msg);
    }
    catch (ProduceException e) {
      ;
    }
    if (waitTime != -1) {
      try {
        Thread.sleep(waitTime);
      }
      catch (InterruptedException e) {
        ;
      }
    }

  }

  public List<AdaptrisMessage> getMessages() {
    return producer.getMessages();
  }

  public int messageCount() {
    return producer.messageCount();
  }
}
