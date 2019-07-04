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
package com.adaptris.core.management.jmx;

import static com.adaptris.security.password.Password.decode;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.AuthorizeCallback;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.security.exc.PasswordException;
import com.sun.jdmk.security.sasl.AuthenticateCallback;

/**
 * Simple {@link CallbackHandler} implementation.
 * @author lchan
 *
 */
class SimpleCallbackHandler implements CallbackHandler {

  private String password;
  private String username;
  private transient Logger logger = LoggerFactory.getLogger(JmxRemoteComponent.class);

  SimpleCallbackHandler(String user, String password) {
    this.username = user;
    this.password = password;
  }

  @Override
  public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    for (int i = 0; i < callbacks.length; ++i) {
      if (callbacks[i] instanceof AuthenticateCallback) {
        AuthenticateCallback cb = (AuthenticateCallback) callbacks[i];
        cb.setAuthenticated(authenticate(cb.getAuthenticationID(), cb.getPassword()));
      } else if (callbacks[i] instanceof AuthorizeCallback) {
        AuthorizeCallback cb = (AuthorizeCallback) callbacks[i];
        // If you can login; then you're authorized.
        cb.setAuthorized(authorize(cb.getAuthenticationID()));
      } else {
        throw new UnsupportedCallbackException(callbacks[i]);
      }
    }
  }

  private boolean authenticate(String user, char[] pw) {
    boolean rc = false;
    try {
      rc = new EqualsBuilder().append(username, user).append(defaultIfEmpty(decode(password), "").toCharArray(), pw).isEquals();
    } catch (PasswordException e) {
      rc = false;
    }
    return rc;
  }

  private boolean authorize(String user) {
    return new EqualsBuilder().append(username, user).isEquals();
  }
}
