package com.adaptris.core.stubs;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ProduceException;

/**
 * This is just a mock class that ensures that after the first invocation, the
 * same thread is calling it (c.f. Executors.newSingleThreadExecutor() )
 * 
 * @author lchan
 * 
 */
public class DetectThreadMockMessageProducer extends MockMessageProducer {

  private Thread firstCaller;

  public DetectThreadMockMessageProducer() {
    super();
  }

  public void produce(AdaptrisMessage msg) throws ProduceException {
    Thread t = Thread.currentThread();
    if (firstCaller == null) {
      firstCaller = t;
    }
    else {
      if (firstCaller != t) {
        throw new ProduceException("Should be the same thread = "
            + firstCaller.getName());
      }
    }
    super.produce(msg);
  }

}
