package com.adaptris.core.http.jetty;

import org.eclipse.jetty.security.Authenticator;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* Factory which will return an instance of org.eclipse.jetty.security.authentication.BasicAuthenticator
* @author ellidges
*/
@JacksonXmlRootElement(localName = "jetty-basic-authenticator")
@XStreamAlias("jetty-basic-authenticator")
public class BasicAuthenticatorFactory implements JettyAuthenticatorFactory {

@Override
public Authenticator retrieveAuthenticator() {
return new org.eclipse.jetty.security.authentication.BasicAuthenticator();
}



}
