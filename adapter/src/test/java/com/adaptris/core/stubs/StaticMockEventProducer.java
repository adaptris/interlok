package com.adaptris.core.stubs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;

/**
 * Variation of {@link MockEventProducer} that keeps the messages produced in a static list.
 */
public class StaticMockEventProducer extends MockEventProducer {

  private static transient List<AdaptrisMessage> producedMessages = new ArrayList<AdaptrisMessage>();

  public StaticMockEventProducer() throws CoreException {
    super();
  }

  public StaticMockEventProducer(Collection<Class> eventsToKeep) throws CoreException {
    super(eventsToKeep);
  }

  /**
   * @see com.adaptris.core.AdaptrisMessageProducer#produce (com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void produce(AdaptrisMessage msg) throws ProduceException {
    if (msg == null) {
      throw new ProduceException("msg is null");
    }
    if (keepMessage(msg)) {
      producedMessages.add(msg);
    }
  }

  @Override
  public List<AdaptrisMessage> getMessages() {
    return producedMessages;
  }

  @Override
  public int messageCount() {
    return producedMessages.size();
  }

  @Override
  public void produce(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException {

    if (msg == null) {
      throw new ProduceException("msg is null");
    }
    if (destination == null) {
      throw new ProduceException("Destination is null");
    }
    produce(msg);
  }
}
