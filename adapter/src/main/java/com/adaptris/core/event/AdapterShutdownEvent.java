package com.adaptris.core.event;

import com.adaptris.core.AdapterLifecycleEvent;
import com.adaptris.core.EventNameSpaceConstants;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * <code>AdapterLifecycleEvent</code> indicating that <code>ShutdownHandler</code> has been invoked.
 * </p>
 * 
 * @config adapter-shutdown-event
 */
@XStreamAlias("adapter-shutdown-event")
public class AdapterShutdownEvent extends AdapterLifecycleEvent {
  private static final long serialVersionUID = 2014012301L;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public AdapterShutdownEvent() {
    super(EventNameSpaceConstants.ADAPTER_SHUTDOWN);
  }
}
