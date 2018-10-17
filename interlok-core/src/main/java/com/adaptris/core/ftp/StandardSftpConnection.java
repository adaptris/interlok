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

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.filetransfer.FileTransferException;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.sftp.ConfigBuilder;
import com.adaptris.sftp.InlineConfigBuilder;
import com.adaptris.sftp.SftpClient;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * SFTP Connection class that connects via a configurable {@link SftpAuthenticationProvider}.
 * 
 * @config standard-sftp-connection
 * @since 3.6.0
 */
@XStreamAlias("standard-sftp-connection")
@AdapterComponent
@ComponentProfile(summary = "Connect to a server using the SSH File Transfer Protocol; authentication via a configured authentication provider",
    tag = "connections,sftp")
@DisplayOrder(order = {"defaultUserName", "defaultControlPort"})
public class StandardSftpConnection extends FileTransferConnection {

  private static final String SCHEME_SFTP = "sftp";

  private static final int DEFAULT_CONTROL_PORT = 22;
  private static final int DEFAULT_TIMEOUT = 60000;

  @NotNull
  private SftpAuthenticationProvider authentication;

  @AdvancedConfig
  @Valid
  private TimeInterval socketTimeout;
  @AdvancedConfig
  private String knownHostsFile;
  @Valid
  @NotNull
  @AutoPopulated
  @AdvancedConfig
  private ConfigBuilder configuration;
  // For sending keep alives every 60 seconds on the control port when downloading stuff.
  // Could make it configurable
  private transient long keepAlive = 60;

  public StandardSftpConnection() {
    super();
    setConfiguration(new InlineConfigBuilder());
  }

  public StandardSftpConnection(ConfigBuilder config, SftpAuthenticationProvider prov) {
    this();
    setConfiguration(config);
    setAuthentication(prov);
  }

  @Override
  protected boolean acceptProtocol(String s) {
    return SCHEME_SFTP.equalsIgnoreCase(s);
  }

  @Override
  protected FileTransferClient create(String remoteHost, int port, UserInfo ui)
      throws IOException, FileTransferException, PasswordException {
    log.debug("Connecting to {}:{} as user {}", remoteHost, port, ui.getUser());
    SftpClient sftp = getAuthentication().connect(new SftpClient(remoteHost, port, socketTimeout(), knownHosts(), getConfiguration())
        .withAdditionalDebug(additionalDebug()).withKeepAliveTimeout(keepAlive), ui);
    return sftp;
  }

  /**
   *
   * @see com.adaptris.core.NullConnection#initConnection()
   */
  @Override
  protected void initConnection() throws CoreException {
    try {
      Args.notNull(getAuthentication(), "authentication");
      super.initConnection();
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  public TimeInterval getSocketTimeout() {
    return socketTimeout;
  }

  /**
   * The socket timeout in milliseconds for connect / read /write operations.
   * 
   * @param t The socketTimeout to set, default is 60000
   */
  public void setSocketTimeout(TimeInterval t) {
    socketTimeout = t;
  }

  int socketTimeout() {
    return getSocketTimeout() != null ? Long.valueOf(getSocketTimeout().toMilliseconds()).intValue() : DEFAULT_TIMEOUT;
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
  public ConfigBuilder getConfiguration() {
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
  public void setConfiguration(ConfigBuilder repo) {
    this.configuration = Args.notNull(repo, "configuration");
  }

  /**
   * @return the authProvider
   */
  public SftpAuthenticationProvider getAuthentication() {
    return authentication;
  }

  /**
   * @param p the authProvider to set
   */
  public void setAuthentication(SftpAuthenticationProvider p) {
    this.authentication = p;
  }

}

