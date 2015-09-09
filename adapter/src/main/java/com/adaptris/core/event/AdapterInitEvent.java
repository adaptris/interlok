package com.adaptris.core.event;

import com.adaptris.core.AdapterLifecycleEvent;
import com.adaptris.core.EventNameSpaceConstants;
import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * <p>
 * <code>AdapterLifecycleEvent</code> indicating that <code>init</code> has been invoked.
 * </p>
 * 
 * @config adapter-init-event
 */
@XStreamAlias("adapter-init-event")
public class AdapterInitEvent extends AdapterLifecycleEvent {
  private static final long serialVersionUID = 2014012301L;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public AdapterInitEvent() {
    super(EventNameSpaceConstants.ADAPTER_INIT);  
  }
}
