package com.adaptris.core.runtime;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;

/**
 * A Digester for handling and recording any errors during the standard workflow processing.
 *
 * @author lchan
 *
 */
public interface MessageErrorDigester extends AdaptrisComponent {

  /**
   * Digest the error that failed.
   *
   * @param message
   */
	void digest(AdaptrisMessage message);

  /**
   * Get the total number of errors that were recorded by this Digester implementatino.
   * 
   * @return the total number of errors.
   */
  int getTotalErrorCount();

}
