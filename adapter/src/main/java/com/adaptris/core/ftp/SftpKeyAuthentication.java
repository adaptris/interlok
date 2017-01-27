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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.ftp.FileTransferConnection.UserInfo;
import com.adaptris.core.util.Args;
import com.adaptris.filetransfer.FileTransferException;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.adaptris.sftp.SftpClient;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link SftpAuthenticationProvider} using keys.
 * 
 * @author lchan
 * @config sftp-key-authentication
 * 
 */
@XStreamAlias("sftp-key-authentication")
@DisplayOrder(order ={ "privateKeyFilename", "privateKeyPassword" })
public class SftpKeyAuthentication implements SftpAuthenticationProvider {
  @NotBlank
  private String privateKeyFilename;
  @InputFieldHint(style = "PASSWORD")
  private String privateKeyPassword;

  public SftpKeyAuthentication() {
  }

  public SftpKeyAuthentication(String filename, String pkeyPassword) {
    this();
    setPrivateKeyFilename(filename);
    setPrivateKeyPassword(pkeyPassword);
  }

  @Override
  public SftpClient connect(SftpClient sftp, UserInfo ui) throws FileTransferException, IOException, PasswordException {
    byte[] privateKey = FileUtils.readFileToByteArray(new File(getPrivateKeyFilename()));
    sftp.connect(ui.getUser(), privateKey,
        getPrivateKeyPassword() != null ? Password.decode(getPrivateKeyPassword()).getBytes() : null);
    return sftp;
  }

  public String getPrivateKeyFilename() {
    return privateKeyFilename;
  }

  /**
   * The name of the file where the private key is held
   *
   * @param filename name of file holding the private key
   */
  public void setPrivateKeyFilename(String filename) {
    this.privateKeyFilename = Args.notBlank(filename, "privateKeyFilename");
  }

  /**
   * The password for the private key (if it has one)
   *
   * @return private key password
   */
  public String getPrivateKeyPassword() {
    return privateKeyPassword;
  }

  /**
   * The password for the private key (if it has one)
   *
   * @param pw
   */
  public void setPrivateKeyPassword(String pw) {
    this.privateKeyPassword = pw;
  }

}
