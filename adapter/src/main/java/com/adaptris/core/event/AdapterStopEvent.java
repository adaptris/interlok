package com.adaptris.core.event;

import com.adaptris.core.AdapterLifecycleEvent;
import com.adaptris.core.EventNameSpaceConstants;
import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * <p>
 * <code>AdapterLifecycleEvent</code> indicating that <code>stop</code> has been invoked.
 * </p>
 * 
 * @config adapter-stop-event
 */
@XStreamAlias("adapter-stop-event")
public class AdapterStopEvent extends AdapterLifecycleEvent {
  private static final long serialVersionUID = 2014012301L;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public AdapterStopEvent() {
    super(EventNameSpaceConstants.ADAPTER_STOP);
  }
}
