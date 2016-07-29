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
package com.adaptris.sftp;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.lang.reflect.Method;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.security.password.Password;
import com.jcraft.jsch.Proxy;

/**
 * Adds proxy support for connecting to an SFTP server
 * 
 * @author lchan
 *
 */
public abstract class ViaProxy implements ProxyBuilder {

  private String proxy;
  @AdvancedConfig
  private String username;
  @AdvancedConfig
  @InputFieldHint(style = "PASSWORD")
  private String password;

  public final Proxy buildProxy() throws SftpException {
    // if someone configures ${host}:${port} in config using var-sub
    // as config (because it's defined to be "" where we don't need it
    // Then let's short cut that and not have a proxy.
    if (isEmpty(getProxy()) || ":".equals(getProxy())) {
      return null;
    }
    Proxy proxy = null;
    proxy = createProxy(getProxy());
    if (!isEmpty(getUsername())) {
      addCredentials(proxy);
    }
    return proxy;
  }

  protected abstract Proxy createProxy(String proxy);

  // lack of inheritance in the JSCH proxy classes; so do it old-skool
  private void addCredentials(Proxy proxy) throws SftpException {
    try {
      Method method = proxy.getClass().getMethod("setUserPasswd", String.class, String.class);
      method.invoke(proxy, getUsername(), Password.decode(getPassword()));
    } catch (Exception e) {
      throw new SftpException(e);
    }
  }

  public String getProxy() {
    return proxy;
  }

  /**
   * The proxy details in {@code host:port} format.
   * 
   * @param s the proxy in {@code host:port} format, the default port depends on the proxy in question.
   */
  public void setProxy(String s) {
    this.proxy = s;
  }

  public String getUsername() {
    return username;
  }


  public void setUsername(String s) {
    this.username = s;
  }


  public String getPassword() {
    return password;
  }


  public void setPassword(String s) {
    this.password = s;
  }
}
