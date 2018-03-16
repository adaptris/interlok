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
package com.adaptris.core.ftp;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.ftp.FileTransferConnection.UserInfo;
import com.adaptris.filetransfer.FileTransferException;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.adaptris.sftp.SftpClient;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link SftpAuthenticationProvider} using a password.
 * 
 * 
 * @author lchan
 * @config sftp-password-authentication
 * 
 */
@XStreamAlias("sftp-password-authentication")
public class SftpPasswordAuthentication implements SftpAuthenticationProvider {
  @InputFieldHint(style = "PASSWORD", external = true)
  private String defaultPassword;

  public SftpPasswordAuthentication() {

  }

  public SftpPasswordAuthentication(String pw) {
    this();
    setDefaultPassword(pw);
  }

  @Override
  public SftpClient connect(SftpClient sftp, UserInfo ui) throws FileTransferException, IOException, PasswordException {
    boolean noPasswd = StringUtils.isEmpty(ui.getPassword());
    String defaultPw = ExternalResolver.resolve(getDefaultPassword());
    sftp.connect(ui.getUser(), noPasswd ? Password.decode(defaultPw) : Password.decode(ui.getPassword()));
    return sftp;
  }


  /**
   * Get the password.
   *
   * @return the password.
   */

  public String getDefaultPassword() {
    return defaultPassword;
  }

  /**
   * Set the default password (supports encoded passwords).
   * 
   * @param s the password to be used if not overridden as part of the destination.
   * @see Password#MSCAPI_STYLE
   * @see Password#NON_PORTABLE_PASSWORD
   * @see Password#PORTABLE_PASSWORD
   */
  public void setDefaultPassword(String s) {
    this.defaultPassword = s;
  }
}
