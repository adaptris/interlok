package com.adaptris.core.http.jetty;

import com.adaptris.core.CoreException;

/**
 * Interface for registering servlets by the various jetty connection implementations.
 *
 * @author lchan
 *
 */
public interface JettyServletRegistrar {

  /**
   * Add a servlet to the jetty engine.
   *
   * @param wrapper
   * @throws CoreException
   */
  void addServlet(ServletWrapper wrapper) throws CoreException;

  /**
   * Remove a servlet from the jetty engine.
   * 
   * @param wrapper the servlet wrapper
   * @throws CoreException
   */
  void removeServlet(ServletWrapper wrapper) throws CoreException;
}
