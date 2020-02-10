package com.adaptris.core.http.jetty;

import org.eclipse.jetty.security.LoginService;

/**
 * Interface to create Jetty LoginService instance
 * @author ellidges
 */
public interface JettyLoginServiceFactory {

  public LoginService retrieveLoginService() throws Exception;
  
}
