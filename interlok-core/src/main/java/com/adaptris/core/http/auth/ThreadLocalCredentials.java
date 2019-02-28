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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.core.http.ResourceAuthenticator;

public class ThreadLocalCredentials implements ResourceAuthenticator {

  /**
   * Keep one ThreadLocalCredentials per target, multiple can be added to the AdapterResourceAuthenticator.
   */
  private static Map<String, ThreadLocalCredentials> instances = Collections.synchronizedMap(new HashMap<String, ThreadLocalCredentials>());

  /**
   * Keeps the current credentials. No initial value provider is set on purpose since this ThreadLocal must always have its value
   * set explicitly.
   */
  private transient final ThreadLocal<PasswordAuthentication> threadAuthentication = new ThreadLocal<PasswordAuthentication>();

  protected static final transient Logger log =
      LoggerFactory.getLogger(ThreadLocalCredentials.class);

  private final String target;
  private final ResourceTargetMatcher matcher;

  private ThreadLocalCredentials(String target, ResourceTargetMatcher matcher) {
    this.target = target;
    this.matcher = matcher;
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

  private String target() {
    return target;
  }

  private ResourceTargetMatcher matcher() {
    return matcher;
  }

  @Override
  public PasswordAuthentication authenticate(ResourceTarget target) {
    if (matcher().matches(target)) {
      PasswordAuthentication auth = getThreadCredentials();
      if (auth != null) {
        log.trace("Using user={} to login", auth.getUserName());
      }
      return auth;
    }
    return null;
  }

  /**
   * Get an instance for the specified target.
   */
  public static ThreadLocalCredentials getInstance(String target) {
    return getInstance(target, new DefaultResourceTargetMatcher(target));
  }

  /**
   * Get an instance for the specified target using the specified {@link ResourceTargetMatcher}.
   * 
   */
  public static ThreadLocalCredentials getInstance(String target, ResourceTargetMatcher matcher) {
    ThreadLocalCredentials instance = instances.get(target);
    if (instance == null) {
      if (matcher != null) {
        instance = new ThreadLocalCredentials(target, matcher);
        instances.put(target, instance);
      } else {
        instance = getInstance(target);
      }
    }
    return instance;
  }

  /*
   * The default {@link ResourceTargetMatcher} that matches against {@link ResourceTarget#getRequestingURL()}.
   *
   */
  private static class DefaultResourceTargetMatcher implements ResourceTargetMatcher {
    private final String urlTarget;

    private DefaultResourceTargetMatcher(String target) {
      this.urlTarget = target;
    }

    @Override
    public boolean matches(ResourceTarget target) {
      boolean rc = StringUtils.equals(urlTarget, target.getRequestingURL().toString());
      if (rc) {
        log.trace("Matched authentication request for {}.", target.getRequestingURL());
      }
      else {
        log.trace("Unmatched authentication request for {}. My target is {}", target.getRequestingURL(), urlTarget);
      }
      return rc;
    }

  }

}
