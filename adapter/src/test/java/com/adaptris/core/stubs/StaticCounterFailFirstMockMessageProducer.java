package com.adaptris.core.stubs;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ProduceException;

public class StaticCounterFailFirstMockMessageProducer extends FailFirstMockMessageProducer {
  
  private static int failUntilCount = 0;
  private static int produceCount = 0;

  public StaticCounterFailFirstMockMessageProducer() {
    super();
    setFailUntilCount(1);
  }

  public void produce(AdaptrisMessage msg) throws ProduceException {
    if (msg == null) {
      throw new ProduceException("msg is null");
    }
    if (produceCount < failUntilCount) {
      produceCount++;
      throw new ProduceException("Produce for " + msg.getUniqueId()
          + " deemed to have failed");
    }
    else {
      super.produce(msg);
    }
  }

  public void resetCount() {
    produceCount = 0;
  }

  /**
   * @return the count
   */
  public int getFailUntilCount() {
    return failUntilCount;
  }

  /**
   * @param count the count to set
   */
  public void setFailUntilCount(int count) {
    failUntilCount = count;
  }
}
