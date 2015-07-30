package com.adaptris.core.services.dynamic;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.EventHandler;
import com.adaptris.core.EventHandlerAware;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("dynamic-event-handler-aware-service")
public class DynamicEventHandlerAwareService extends DynamicService implements EventHandlerAware {

  private static transient EventHandler eventHandler;

  public DynamicEventHandlerAwareService() {
    super();
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
  }

  @Override
  public void registerEventHandler(EventHandler eh) {
    eventHandler = eh;
  }

  // It's a bit lame to have this static,
  // But the class goes out of scope during the tests.
  public static EventHandler registeredEventHandler() {
    return eventHandler;
  }
}
