package com.adaptris.core.http.jetty;

import org.eclipse.jetty.security.Authenticator;

/**
 * Interface for factory to create Jetty Authenticators
 * @author ellidges
 */
public interface JettyAuthenticatorFactory {

  public Authenticator retrieveAuthenticator();
  
}
