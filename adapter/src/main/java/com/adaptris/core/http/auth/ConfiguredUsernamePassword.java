package com.adaptris.core.http.auth;

import java.net.PasswordAuthentication;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.thoughtworks.xstream.annotations.XStreamAlias;

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
