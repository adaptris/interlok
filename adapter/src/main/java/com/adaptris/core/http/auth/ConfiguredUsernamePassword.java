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

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Authenticate using the standard {@link PasswordAuthentication} method with a static username and password
 * 
 * @author gdries
 *
 */
@XStreamAlias("http-configured-username-password")
@DisplayOrder(order = {"username", "password"})
public class ConfiguredUsernamePassword extends UserPassAuthentication {

  private String username = null;
  
  @InputFieldHint(style = "PASSWORD")
  private String password = null;
  
  @Override
  protected PasswordAuthentication getPasswordAuthentication(AdaptrisMessage msg) throws CoreException {
    try {
      return new PasswordAuthentication(username, Password.decode(password).toCharArray());
    } catch (PasswordException e) {
      throw new CoreException("Unable to decode password", e);
    }
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

}
