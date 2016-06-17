package com.adaptris.core.http.jetty;

import org.eclipse.jetty.security.Authenticator;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Factory which will return an instance of org.eclipse.jetty.security.authentication.BasicAuthenticator
 * @author ellidges
 */
@XStreamAlias("basic-authenticator")
public class BasicAuthenticatorFactory implements JettyAuthenticatorFactory {

  @Override
  public Authenticator retrieveAuthenticator() {
    return new org.eclipse.jetty.security.authentication.BasicAuthenticator();
  }

  
  
}
