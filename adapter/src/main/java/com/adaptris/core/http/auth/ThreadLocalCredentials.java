/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adaptris.core.http.auth;

import java.net.PasswordAuthentication;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.http.ResourceAuthenticator;

public class ThreadLocalCredentials implements ResourceAuthenticator {

  /**
   * Keep one ThreadLocalCredentials per target, multiple can be added to the AdapterResourceAuthenticator.
   */
  private static Map<String, ThreadLocalCredentials> instances = Collections.synchronizedMap(new HashMap<String, ThreadLocalCredentials>());

  public static ThreadLocalCredentials getInstance(String target) {
    ThreadLocalCredentials instance = instances.get(target);
    if(instance == null) {
      instance = new ThreadLocalCredentials(target);
      instances.put(target, instance);
    }
    return instance;
  }
  
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  
  /**
   *  Keeps the current credentials. No initial value provider is set on purpose
   *  since this ThreadLocal must always have its value set explicitly.
   */
  private transient final ThreadLocal<PasswordAuthentication> threadAuthentication = new ThreadLocal<PasswordAuthentication>();

  private final String target;
  
  private ThreadLocalCredentials(String target) {
    this.target = target;
  }
  
  @Override
  public PasswordAuthentication authenticate(ResourceTarget target) {
    if(this.target.equals(target.getRequestingURL().toString())) {
      PasswordAuthentication auth = getThreadCredentials();
      if(auth != null) {
        log.trace("Using user={} to login to [{}]", auth.getUserName(), target.getRequestingURL());
      }
      return auth;
    } else {
      log.trace("Unmatched authentication request for {}. My target is {}", target.getRequestingURL(), this.target);
    }
    return null;
  }

  public PasswordAuthentication getThreadCredentials() {
    return threadAuthentication.get();
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
