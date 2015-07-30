package com.adaptris.core.services.system;

import com.adaptris.core.AdaptrisMessage;

/**
 * Interface for providing command line arguments to the {@link SystemCommandExecutorService}
 * 
 * @author sellidge
 */
public interface CommandArgument {
  /**
   * @param msg the {@link AdaptrisMessage}
   * @return the value for this argument
   */
  public String retrieveValue(AdaptrisMessage msg);
}
