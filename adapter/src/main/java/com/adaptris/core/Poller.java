package com.adaptris.core;


/**
 * <p> 
 * Schedules polling for <code>AdaptrisPollingConsumer</code>s.
 * </p>
 */
public interface Poller extends AdaptrisComponent {
  
  /**
   * <p>
   * Register the <code>AdaptrisPollingConsumer</code> to use.  
   * </p>
   * @param consumer the <code>AdaptrisPollingConsumer</code> to use
   */
  void registerConsumer(AdaptrisPollingConsumer consumer);
  
  /**
   * <p>
   * Retrieve the <code>AdaptrisPollingConsumer</code> to use.
   * </p>
   * @return the <code>AdaptrisPollingConsumer</code> to use
   */
  AdaptrisPollingConsumer retrieveConsumer();
}
