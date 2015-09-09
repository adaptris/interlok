package com.adaptris.core.stubs;

import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.EventHandler;
import com.adaptris.core.EventHandlerAware;

/**
 * <p>
 * Mock implementation of <code>AdaptrisMessageConsumer</code> which allows
 * e.g. test cases to create and submit messages to the registered
 * <code>AdaptrisMessageListener</code>.
 * </p>
 */
public class EventHandlerAwareConsumer extends MockMessageConsumer implements EventHandlerAware {

  private EventHandler evtHandler;

  public EventHandlerAwareConsumer() {
    super();
  }

  public EventHandlerAwareConsumer(ConsumeDestination d, AdaptrisMessageListener m) {
    super(d, m);
  }

  public EventHandlerAwareConsumer(ConsumeDestination d) {
    super(d);
  }

  public EventHandlerAwareConsumer(AdaptrisMessageListener aml) {
    super(aml);
  }

  @Override
  public void registerEventHandler(EventHandler eh) {
    evtHandler = eh;
  }

  public EventHandler retrieveEventHandler() {
    return evtHandler;
  }

}
