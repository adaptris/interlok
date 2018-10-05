/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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
