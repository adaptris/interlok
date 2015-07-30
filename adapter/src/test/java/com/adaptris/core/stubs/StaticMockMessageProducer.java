package com.adaptris.core.stubs;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ProduceException;

/**
 * Variation on MockMessageProducer that keeps messages in a static list for PoolingWorkflow tracking purposes.
 *
 * @author lchan
 *
 */
public class StaticMockMessageProducer extends MockMessageProducer {

  private static List<AdaptrisMessage> producedMessages = new ArrayList<AdaptrisMessage>();

  public StaticMockMessageProducer() {
    super();
  }

  @Override
  public void produce(AdaptrisMessage msg) throws ProduceException {
    if (msg == null) {
      throw new ProduceException("msg is null");
    }
    log.trace("Produced [" + msg.getUniqueId() + "]");
    producedMessages.add(msg);
  }

  /**
   * <p>
   * Returns the internal store of produced messages.
   * </p>
   *
   * @return the internal store of produced messages
   */
  @Override
  public List<AdaptrisMessage> getMessages() {
    return producedMessages;
  }

  @Override
  public int messageCount() {
    return producedMessages.size();
  }
}
