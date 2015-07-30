package com.adaptris.core;


/**
 * Marker interface indicating that this component needs to be made aware of the
 * EventHandler.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public interface EventHandlerAware {

  /**
   * Register the current event handler against this component.
   * 
   * @param eh the event handler currently in use.
   */
  void registerEventHandler(EventHandler eh);
}
