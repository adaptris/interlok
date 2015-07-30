/*
 * $RCSfile: HeartbeatEvent.java,v $
 * $Revision: 1.13 $
 * $Date: 2006/05/23 06:34:59 $
 * $Author: hfraser $
 */
package com.adaptris.core;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Standard <code>Adapter</code> heartbeat event. This class may be sub-classed
 * to meet solution-specific heartbeat information requirements.
 * </p>
 * <p>
 * In the adapter configuration file this class is aliased as <b>heartbeat-event</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 */
@XStreamAlias("heartbeat-event")
public class HeartbeatEvent extends AdapterLifecycleEvent {
  private static final long serialVersionUID = 2014012301L;

  private long heartbeatTime;
  private AdapterStateSummary adapterStateSummary;

  /**
   * <p>
   * Creates a new instance.
   * </p> 
   */
  public HeartbeatEvent() {
    super(EventNameSpaceConstants.HEARTBEAT);
  }

  /**
   * <p>
   * Returns the time this event was sent.
   * </p>
   * @return a <code>long</code> representing the time this event was sent
   */
  public long getHeartbeatTime() {
    return heartbeatTime;
  }

  /**
   * <p>
   * Sets the time this event was sent.
   * </p>
   * @param l a <code>long</code> representing the time this event
   * was sent
   */
  public void setHeartbeatTime(long l) {
    this.heartbeatTime = l;
  }
  
  /**
   * <p>
   * Extracts a report on the <code>Adapter</code>'s current state.
   * </p>
   * @param adapter the <code>Adapter</code> to extract state from
   */
  public void extractState(Adapter adapter) {
    adapterStateSummary = new AdapterStateSummary(adapter);
  }
  
  /**
   * <p>
   * Return the <code>AdapterStateSummary</code>.
   * </p>
   * @return the <code>AdapterStateSummary</code>
   */
  public AdapterStateSummary getAdapterStateSummary() {
    return this.adapterStateSummary;
  }
  
  /**
   * <p>
   * Sets the <code>AdapterStateSummary</code>.
   * </p>
   * @param a the <code>AdapterStateSummary</code>
   */
  public void setAdapterStateSummary(AdapterStateSummary a) {
    this.adapterStateSummary = a;
  }

}
