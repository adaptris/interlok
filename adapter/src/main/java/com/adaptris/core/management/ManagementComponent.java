package com.adaptris.core.management;

import java.util.Properties;

/**
 * Interface for management components that exist outside of the standard adapter lifecycle.
 *
 *
 */
public interface ManagementComponent {

  /**
   * Initialise the management component.
   *
   * @param config configuration properties that have been built during bootstrap.
   * @throws Exception if initialisation fails.
   */
  public void init(Properties config) throws Exception;

  /**
   * Start the management component.
   *
   */
  public void start() throws Exception;


  /**
   * Stop the management component.
   *
   */
  public void stop() throws Exception;

  /**
   * Destroy the management component making it require re-initialisation.
   *
   */
  void destroy() throws Exception;

}
