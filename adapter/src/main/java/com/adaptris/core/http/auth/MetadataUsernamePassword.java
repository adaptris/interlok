package com.adaptris.core.http.auth;

import java.net.PasswordAuthentication;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.thoughtworks.xstream.annotations.XStreamAlias;

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
