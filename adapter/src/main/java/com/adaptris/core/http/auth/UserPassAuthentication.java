package com.adaptris.core.http.auth;

import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;


public abstract class UserPassAuthentication implements HttpAuthenticator {

  private ThreadLocalCredentials threadLocalCreds;
  
  @Override
  public void setup(String target, AdaptrisMessage msg) throws CoreException {
    threadLocalCreds = ThreadLocalCredentials.getInstance(target);
    threadLocalCreds.setThreadCredentials(getPasswordAuthentication(msg));
    AdapterResourceAuthenticator.getInstance().addAuthenticator(threadLocalCreds);
    return;
  }
  
  protected abstract PasswordAuthentication getPasswordAuthentication(AdaptrisMessage msg)
    throws CoreException;

  @Override
  public void configureConnection(HttpURLConnection conn) {
    // Nothing to do here
  }
  
  @Override
  public void close() {
    threadLocalCreds.removeThreadCredentials();
    AdapterResourceAuthenticator.getInstance().removeAuthenticator(threadLocalCreds);
  }

}
