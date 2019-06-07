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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.ftp.FileTransferConnection.UserInfo;
import com.adaptris.filetransfer.FileTransferException;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.sftp.SftpClient;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * {@link SftpAuthenticationProvider} that wraps other {@link SftpAuthenticationProvider} instances.
 * 
 * @author lchan
 * @config sftp-authentication-wrapper
 */
@XStreamAlias("sftp-authentication-wrapper")
@ComponentProfile(summary = "SFTP Authentication Provider that wraps other providers.")
@DisplayOrder(order = {"providers"})
public class SftpAuthenticationWrapper implements SftpAuthenticationProvider {

  @NotNull
  @AutoPopulated
  @Valid
  @XStreamImplicit
  private List<SftpAuthenticationProvider> providers;

  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean logExceptions;

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  public SftpAuthenticationWrapper() {
    setProviders(new ArrayList<SftpAuthenticationProvider>());
  }

  public SftpAuthenticationWrapper(SftpAuthenticationProvider... auths) {
    this();
    setProviders(new ArrayList<SftpAuthenticationProvider>(Arrays.asList(auths)));
  }

  @Override
  public SftpClient connect(SftpClient sftp, UserInfo ui) throws FileTransferException, IOException, PasswordException {
    boolean connected = false;
    for (SftpAuthenticationProvider p : getProviders()) {
      if (connect(sftp, ui, p)) {
        connected = true;
        break;
      }
    }
    if (!connected) {
      throw new FileTransferException("Failed to connect via any configured authentication providers");
    }
    return sftp;
  }

  private boolean connect(SftpClient sftp, UserInfo ui, SftpAuthenticationProvider prov) {
    boolean connected = false;
    try {
      prov.connect(sftp, ui);
      connected = true;
    }
    catch (Exception e) {
      log.warn("Failed to connect using {}", prov.getClass().getCanonicalName());
      if (logExceptions()) {
        log.trace("Exception message from {}", prov.getClass().getCanonicalName(), e);
      }
    }
    return connected;
  }


  /**
   * @return the providers
   */
  public List<SftpAuthenticationProvider> getProviders() {
    return providers;
  }

  /**
   * @param list the providers
   */
  public void setProviders(List<SftpAuthenticationProvider> list) {
    this.providers = list;
  }

  public Boolean getLogExceptions() {
    return logExceptions;
  }

  /**
   * Whether or not to log exceptions from each provider.
   * 
   * @param b true to enable logging, default is false if not otherwise specified.
   */
  public void setLogExceptions(Boolean b) {
    this.logExceptions = b;
  }

  private boolean logExceptions() {
    return BooleanUtils.toBooleanDefaultIfNull(getLogExceptions(), false);
  }
}
