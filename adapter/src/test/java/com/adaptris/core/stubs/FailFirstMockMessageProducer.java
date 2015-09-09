package com.adaptris.core.stubs;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ProduceException;

/**
 * This is just a dummy class to fail a message and then succeed (used to test
 * the RetryMessageErrorHandler).
 *
 * @author lchan
 *
 */
public class FailFirstMockMessageProducer extends MockMessageProducer {

  private int failUntilCount = 0;
  private transient int produceCount = 0;

  public FailFirstMockMessageProducer() {
    super();
    setFailUntilCount(1);
  }

  public FailFirstMockMessageProducer(int failUntilCount) {
    this();
    setFailUntilCount(failUntilCount);
  }

  @Override
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
    this.failUntilCount = count;
  }

}
