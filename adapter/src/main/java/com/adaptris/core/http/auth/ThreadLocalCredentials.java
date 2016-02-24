package com.adaptris.core.http.auth;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadLocalCredentials extends Authenticator {
  
  private static ThreadLocalCredentials INSTANCE = new ThreadLocalCredentials();

  public static ThreadLocalCredentials getInstance() {
    return INSTANCE;
  }
  
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  
  /**
   *  Keeps the current credentials. No initial value provider is set on purpose
   *  since this ThreadLocal must always have its value set explicitly.
   */
  private transient final ThreadLocal<PasswordAuthentication> threadAuthentication = new ThreadLocal<PasswordAuthentication>();
  
  @Override
  protected PasswordAuthentication getPasswordAuthentication() {
    PasswordAuthentication auth = threadAuthentication.get();
    if(auth != null) {
      log.trace("Using user={} to login to [{}]", auth.getUserName(), getRequestingURL());
    }
    return auth;
  }

  /**
   * Set the credentials for the current thread
   */
  public void setThreadCredentials(PasswordAuthentication pwauth) {
    this.threadAuthentication.set(pwauth);
  }
  
  /**
   * Remove the credentials for the current thread
   */
  public void removeThreadCredentials() {
    this.threadAuthentication.set(null);
  }
  
}
