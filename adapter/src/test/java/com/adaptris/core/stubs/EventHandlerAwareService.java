package com.adaptris.core.stubs;

import com.adaptris.core.EventHandler;
import com.adaptris.core.EventHandlerAware;
import com.adaptris.core.NullService;

public class EventHandlerAwareService extends NullService implements EventHandlerAware {

  private EventHandler evtHandler;

  public EventHandlerAwareService() {
    super();
  }

  public EventHandlerAwareService(String id) {
    super(id);
  }

  @Override
  public void registerEventHandler(EventHandler eh) {
    evtHandler = eh;
  }

  public EventHandler retrieveEventHandler() {
    return evtHandler;
  }
}
