/*
 * $RCSfile: AdapterStartEvent.java,v $
 * $Revision: 1.6 $
 * $Date: 2005/09/23 00:56:54 $
 * $Author: hfraser $
 */
package com.adaptris.core.event;

import com.adaptris.core.AdapterLifecycleEvent;
import com.adaptris.core.EventNameSpaceConstants;
import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * <p>
 * <code>AdapterLifecycleEvent</code> indicating that <code>start</code> has been invoked.
 * </p>
 * 
 * @config adapter-start-event
 */
@XStreamAlias("adapter-start-event")
public class AdapterStartEvent extends AdapterLifecycleEvent {
  private static final long serialVersionUID = 2014012301L;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public AdapterStartEvent() {
    super(EventNameSpaceConstants.ADAPTER_START);
  }
}
