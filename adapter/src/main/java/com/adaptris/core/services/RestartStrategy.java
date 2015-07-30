package com.adaptris.core.services;

import com.adaptris.core.AdaptrisMessage;

/**
 * Restart strategy for {@link StatelessServiceWrapper}.
 * 
 * @author amcgrath
 * 
 */
public interface RestartStrategy {

  /**
   * Mark the current message as processed.
   * 
   * @param msg the message currently being processed.
   */
  public void messageProcessed(AdaptrisMessage msg);
  
  /**
   * Whether or not a restart of the underlying service is required.
   * 
   * @return true if a restart is required.
   */
  public boolean requiresRestart();
  
}
