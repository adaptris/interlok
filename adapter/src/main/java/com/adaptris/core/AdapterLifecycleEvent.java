package com.adaptris.core;

import java.io.Serializable;

/**
 * <p>
 * Contains behaviour common to all <code>Events</code> in the framework 
 * which relate to an <code>Adapter</code>'s lifecycle. 
 * </p>
 * <p>
 * In the adapter configuration file this class is aliased as <b>adapter-lifecycle-event</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 */
public abstract class AdapterLifecycleEvent extends Event implements Serializable {

  private static final long serialVersionUID = 2014012301L;

  private String adapterUniqueId;
  private boolean wasSuccessful;
  
  /**
   * <p>
   * Creates a new instance with passed name space.  Used by concrete sub 
   * classes.  Success defaults to true.
   * </p>
   * @param s the name space of this <code>Event</code>, may not be null or 
   * empty
   */
  protected AdapterLifecycleEvent(String s) {
    super(s);
    setWasSuccessful(true);
  }

  /**
   * <p>
   * Returns the name space of this <code>Event</code>.  Over-rides
   * implementation in <code>Event</code> and adds <code>.fail</code> or 
   * <code>.success</code> to the end of the original name space, based on
   * calling <code>getWasSuccessful</code>.
   * </p>
   * @return the namespace of this <code>Event</code>
   */
  @Override
  public String getNameSpace() {
    StringBuffer result = new StringBuffer(super.getNameSpace());
    
    if (getWasSuccessful()) {
      result.append(".success");
    }
    else {
      result.append(".fail");
    }
    
    return result.toString();
  }

  /**
   * <p>
   * Returns the unique ID of the adapter emitting this event.
   * @return the unique ID of the adapter emitting this event
   */
  public String getAdapterUniqueId() {
    return adapterUniqueId;
  }

  /**
   * <p>
   * Sets the unique ID of the adapter emitting this event.
   * </p>
   * @param string the unique ID of the adapter emitting this event
   */
  public void setAdapterUniqueId(String string) {
    adapterUniqueId = string;
  }
  
  /**
   * <p>
   * Returns true if the adapter life cycle event occurred successfully, 
   * otherwise false.
   * </p>
   * @return true if the adapter life cycle event occurred successfully.
   */
  public boolean getWasSuccessful() {
    return wasSuccessful;
  }

  /**
   * <p>
   * Sets the success or otherwise of the adapter life cycle event.
   * </p>
   * @param b the success or otherwise of the adapter life cycle event
   */
  public void setWasSuccessful(boolean b) {
    wasSuccessful = b;
  }
}
