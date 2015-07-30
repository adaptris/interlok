package com.adaptris.core.mail;

import static org.apache.commons.lang.StringUtils.isEmpty;

import javax.mail.URLName;

import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;

final class MailHelper {

  static final URLName createURLName(String urlString, String username, String password) throws PasswordException {
    URLName url = new URLName(urlString);
    String realPassword = url.getPassword();
    String realUser = url.getUsername();
    if (realUser == null && !isEmpty(username)) {
      realUser = username;
    }
    if (url.getPassword() == null && password != null) {
      realPassword = Password.decode(password);
    }
    return new URLName(url.getProtocol(), url.getHost(), url.getPort(), url.getFile(), realUser, realPassword);
  }
}
