package com.adaptris.core.http.auth;

import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;


public abstract class UserPassAuthentication implements HttpAuthenticator {

  @Override
  public HttpAuthenticator setup(AdaptrisMessage msg) throws CoreException {
    ThreadLocalCredentials.getInstance().setThreadCredentials(getPasswordAuthentication(msg));
    return this;
  }
  
  protected abstract PasswordAuthentication getPasswordAuthentication(AdaptrisMessage msg)
    throws CoreException;

  @Override
  public void configureConnection(HttpURLConnection conn) {
    // Nothing to do here
  }
  
  @Override
  public void close() {
    ThreadLocalCredentials.getInstance().removeThreadCredentials();
  }

}
