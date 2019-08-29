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
import javax.validation.constraints.NotBlank;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Authenticate using the standard {@link PasswordAuthentication} method with a username and password derived from metadata.
 * 
 * @author gdries
 *
 */
@XStreamAlias("http-metadata-username-password")
@DisplayOrder(order = {"usernameMetadataKey", "passwordMetadataKey"})
public class MetadataUsernamePassword extends UserPassAuthentication {

  @NotBlank
  private String usernameMetadataKey;
  
  @NotBlank
  private String passwordMetadataKey;
  
  @Override
  protected PasswordAuthentication getPasswordAuthentication(AdaptrisMessage msg) throws CoreException {
    try {
      String username = msg.getMetadataValue(getUsernameMetadataKey());
      char[] password = Password.decode(msg.getMetadataValue(getPasswordMetadataKey())).toCharArray();
      return new PasswordAuthentication(username, password);
    } catch (PasswordException e) {
      throw new CoreException("Unable to decode password", e);
    }
  }

  public String getUsernameMetadataKey() {
    return usernameMetadataKey;
  }

  /**
   * Metadata key for the username to use
   * @param usernameMetadataKey
   */
  public void setUsernameMetadataKey(String usernameMetadataKey) {
    this.usernameMetadataKey = usernameMetadataKey;
  }

  public String getPasswordMetadataKey() {
    return passwordMetadataKey;
  }

  /**
   * Metadata key for the password, this metadata value may be an encoded password.
   * @param passwordMetadataKey
   */
  public void setPasswordMetadataKey(String passwordMetadataKey) {
    this.passwordMetadataKey = passwordMetadataKey;
  }


}
