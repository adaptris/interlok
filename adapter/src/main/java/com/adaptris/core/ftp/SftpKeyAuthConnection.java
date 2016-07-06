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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.FileUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.util.Args;
import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.filetransfer.FileTransferException;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.adaptris.sftp.ConfigRepositoryBuilder;
import com.adaptris.sftp.InlineConfigRepository;
import com.adaptris.sftp.SftpClient;
import com.adaptris.sftp.SftpException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * SFTP Connection class using public/private key authentication
 * <p>
 * This connection implementation allows you to use a public / private key pair to authenticate against the sftp server. It deviates
 * from a standard {@link SftpConnection}. Rather than specifying a default password for accessing the server, you specify a
 * {@link #setPrivateKeyFilename(String)} and {@link #setPrivateKeyPassword(String)} which contains your private key credentials
 * which are then supplied to the server.
 * </p>
 * <p>
 * It has the following behavioural changes from a standard SftpConnection :
 * <ul>
 * <li>If the private key is not accepted by the target server, then an exception will be thrown.</li>
 * <li>If no private key password is specified then it is assumed to be a 0 length string.</li>
 * <li>Only a single privatekey file will be supported per SftpKeyAuthConnection instance.</li>
 * <li>Specifying the username+password in the destination (e.g. <code>sftp://lchan:myPassword@1.2.3.4:22//opt/sftp</code>), will
 * override the username used to login but no other credentials. The only valid authentication is via the specified private
 * key.</li>
 * <li>You can specify additional behaviour using one of {@link DefaultKnownHosts}, {@link com.adaptris.sftp.LenientKnownHosts}
 * or {@link com.adaptris.sftp.StrictKnownHosts}. {@link com.adaptris.sftp.StrictKnownHosts} will cause an exception to be thrown
 * if the servers key is not present in any configured known_hosts file.</li>
 * <li>The private key and known_hosts file are expected to be in OpenSSH format</li>
 * </ul>
 * </p>
 * <p>
 * The password associated with {@link #setPrivateKeyPassword(String)} may be encoded using any of the standard {@link
 * com.adaptris.security.password.Password}
 * mechanisms and it will be decoded when the private key is first accessed.
 * </p>
 * 
 * @config sftp-key-auth-connection
 * 
 * @author dsefton
 */
@XStreamAlias("sftp-key-auth-connection")
@AdapterComponent
@ComponentProfile(summary = "Connect to a server using the SSH File Transfer Protocol; authentication via keys",
    tag = "connections,sftp")
@DisplayOrder(order = {"defaultUserName", "privateKeyFilename", "privateKeyPassword", "defaultControlPort"})
public class SftpKeyAuthConnection extends FileTransferConnection {

  private static final String SCHEME_SFTP = "sftp";

  static final int DEFAULT_CONTROL_PORT = 22;
  private static final int DEFAULT_TIMEOUT = 60000;

  private String privateKeyFilename;
  private String privateKeyPassword;
  @AdvancedConfig
  private Integer socketTimeout;
  @AdvancedConfig
  private String knownHostsFile;
  @Valid
  @NotNull
  @AutoPopulated
  @AdvancedConfig
  private ConfigRepositoryBuilder configuration;
  // For sending keep alives every 60 seconds on the control port when downloading stuff.
  // Could make it configurable
  private transient long keepAlive = 60;

  public SftpKeyAuthConnection() {
    super();
    setConfiguration(new InlineConfigRepository());
  }


  @Override
  protected boolean acceptProtocol(String s) {
    return SCHEME_SFTP.equalsIgnoreCase(s);
  }

  @Override
  protected FileTransferClient create(String remoteHost, int port, UserInfo ui) throws IOException, FileTransferException {
    log.debug("Connecting to " + remoteHost + ":" + port + " as user " + ui.getUser());
    SftpClient sftp = new SftpClient(remoteHost, port, socketTimeout(), knownHosts(), getConfiguration().build());
    sftp.setAdditionalDebug(additionalDebug());
    sftp.setKeepAliveTimeout(keepAlive);
    try {
      byte[] privateKey = FileUtils.readFileToByteArray(new File(getPrivateKeyFilename()));
      sftp.connect(ui.getUser(), privateKey, Password.decode(getPrivateKeyPassword()).getBytes());
    }
    catch (PasswordException e) {
      throw new SftpException(e);
    }
    return sftp;
  }

  public Integer getSocketTimeout() {
    return socketTimeout;
  }

  /**
   * The socket timeout in milliseconds for connect / read /write operations.
   * 
   * @param t The socketTimeout to set, default is 60000
   */
  public void setSocketTimeout(Integer t) {
    socketTimeout = t;
  }

  int socketTimeout() {
    return getSocketTimeout() != null ? getSocketTimeout().intValue() : DEFAULT_TIMEOUT;
  }

  public String getPrivateKeyFilename() {
    return privateKeyFilename;
  }

  /**
   * The name of the file where the private key is held
   *
   * @param privateKeyFilename name of file holding the private key
   */
  public void setPrivateKeyFilename(String privateKeyFilename) {
    this.privateKeyFilename = privateKeyFilename;
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
   * @param privateKeyPassword
   */
  public void setPrivateKeyPassword(String privateKeyPassword) {
    this.privateKeyPassword = privateKeyPassword;
  }

  @Override
  protected UserInfo createUserInfo() throws FileTransferException {
    return new UserInfo(getDefaultUserName());
  }

  public String getKnownHostsFile() {
    return knownHostsFile;
  }

  public void setKnownHostsFile(String k) {
    knownHostsFile = k;
  }

  private File knownHosts() {
    return knownHostsFile != null ? new File(knownHostsFile) : null;
  }

  @Override
  public int defaultControlPort() {
    return getDefaultControlPort() != null ? getDefaultControlPort().intValue() : DEFAULT_CONTROL_PORT;
  }


  /**
   * @return the configRepository
   */
  public ConfigRepositoryBuilder getConfiguration() {
    return configuration;
  }



  /**
   * Set the config repository.
   * <p>
   * Use a config repository to set various SSH based settings (such as {@code PreferredAuthentications} or
   * {@code ServerAliveInterval}.
   * </p>
   * 
   * @param repo the configRepository to set
   */
  public void setConfiguration(ConfigRepositoryBuilder repo) {
    this.configuration = Args.notNull(repo, "configuration");
  }
}

