package com.adaptris.core.stubs;

import com.adaptris.core.EventHandler;
import com.adaptris.core.EventHandlerAware;

public class EventHandlerAwareProducer extends MockMessageProducer implements EventHandlerAware {
  private EventHandler evtHandler;

  public EventHandlerAwareProducer() {
  }

  @Override
  public void registerEventHandler(EventHandler eh) {
    evtHandler = eh;
  }

  public EventHandler retrieveEventHandler() {
    return evtHandler;
  }
}
