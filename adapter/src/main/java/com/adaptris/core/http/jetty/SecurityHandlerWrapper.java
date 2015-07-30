package com.adaptris.core.http.jetty;

import org.eclipse.jetty.security.SecurityHandler;

/**
 * This is a proxy interface for the Jetty Security Handler implementation.
 * <p>
 * There is only one supported implementation at this time, which is {@link HashUserRealmProxy}
 * </p>
 * 
 * @author lchan
 * 
 */
public interface SecurityHandlerWrapper {

  /**
   * Create a security handler implementation.
   * 
   * @return a Jetty SecurityHandler implementation
   * @throws Exception.
   */
  SecurityHandler createSecurityHandler() throws Exception;
}
