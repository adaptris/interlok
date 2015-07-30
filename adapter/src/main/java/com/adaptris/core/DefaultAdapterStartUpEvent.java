package com.adaptris.core;

import com.adaptris.core.event.StandardAdapterStartUpEvent;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Event containing <code>Adapter</code> start-up information..
 * </p>
 * 
 * @config default-adapter-start-up-event
 * 
 * @see AdapterStartUpEvent
 */
@XStreamAlias("default-adapter-start-up-event")
@Deprecated
public class DefaultAdapterStartUpEvent extends StandardAdapterStartUpEvent {
  private static final long serialVersionUID = 2014012301L;

  // DO NOT REMOVE THIS CLASS UNTIL WE GET RID OF ALL V2 CLIENTS TO A V3 HUB
  public DefaultAdapterStartUpEvent() throws Exception {
    super();
  }
}
