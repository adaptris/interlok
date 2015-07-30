/*
 * $RCSfile: MockMessageProducer.java,v $
 * $Revision: 1.12 $
 * $Date: 2008/10/28 13:34:30 $
 * $Author: lchan $
 */
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
