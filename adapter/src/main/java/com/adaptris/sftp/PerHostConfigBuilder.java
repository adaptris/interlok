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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.util.Args;
import com.adaptris.util.KeyValuePairSet;
import com.jcraft.jsch.ConfigRepository;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * A statically configured SSH {@link ConfigRepository} that supports different configuration on a per host basis.
 * <p>
 * If a host is not configured then we build default configuration from {@link #getDefaultConfiguration()}
 * </p>
 * 
 * @config sftp-per-host-config-repository
 */
@XStreamAlias("sftp-per-host-config-builder")
@DisplayOrder(order = {"hosts", "defaultConfiguration", "proxy"})
public class PerHostConfigBuilder extends ConfigBuilderImpl implements ConfigRepository {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  @AutoPopulated
  @NotNull
  @Valid
  @XStreamImplicit(itemFieldName = "host")
  private List<HostConfig> hosts;

  @AutoPopulated
  @NotNull
  @Valid
  @AdvancedConfig
  private KeyValuePairSet defaultConfiguration;

  public PerHostConfigBuilder() {
    setHosts(new ArrayList<HostConfig>());
    setDefaultConfiguration(new KeyValuePairSet());
  }

  /**
   * Return the configuration for the host.
   * <p>
   * If a host is not configured then we build default configuration from {@link #getDefaultConfiguration()}
   * </p>
   * 
   */
  @Override
  public Config getConfig(final String host) {
    Config result = new Config() {
      @Override
      public String getHostname() {
        return host;
      }

      @Override
      public String getUser() {
        return null;
      }

      @Override
      public int getPort() {
        return -1;
      }

      @Override
      public String getValue(String key) {
        return defaultConfiguration.getValue(key);
      }

      @Override
      public String[] getValues(String key) {
        return null;
      }

    };
    for (HostConfig cfg : getHosts()) {
      if (cfg.getHostname().equals(host)) {
        result = cfg;
        log.trace("Found Host in config [{}], configuration=[{}]", cfg.getHostname(), cfg.getConfig());
        break;
      }
    }
    return result;
  }

  /**
   * @return the hosts
   */
  public List<HostConfig> getHosts() {
    return hosts;
  }

  /**
   * @param hostConfig the hosts to set
   */
  public void setHosts(List<HostConfig> hostConfig) {
    this.hosts = hostConfig;
  }

  @Override
  public ConfigRepository buildConfigRepository() throws IOException {
    return this;
  }

  /**
   * @return the defaultConfiguration
   */
  public KeyValuePairSet getDefaultConfiguration() {
    return defaultConfiguration;
  }

  /**
   * Set your default configuration options.
   * <p>
   * Some of the more common options you might configure are:
   * <ul>
   * <li>{@code compression.s2c} and {@code compression.c2s} for the compression algorithm to use : {@code none} or {@code zlib}
   * </li>
   * <li>{@code compression_level} for the level of compression</li>
   * <li>{@code server_host_key} for enabling or disabling certain types of keys such as
   * {@code ssh-rsa,ssh-dss,ecdsa-sha2-nistp256,ecdsa-sha2-nistp384,ecdsa-sha2-nistp521}</li>
   * <li>{@code StrictHostKeyChecking} to force the host to present in any configured {@code known_hosts} file</li>
   * <li>{@code PreferredAuthentications} for preferred authentication mechanisms such as
   * {@code gssapi-with-mic,publickey,keyboard-interactive,password}</li>
   * <li>{@code kex} for the key exchange algorithms</li>
   * <li>{@code ServerAliveInterval}, {@code ConnectTimeout}, {@code MaxAuthTries}, {@code ClearAllForwardings},
   * {@code HashKnownHosts}</li>
   * </ul>
   * </p>
   * <p>
   * Generally the defaults are quite sensible, so you don't tend to need to configure anything unless the host
   * you are connecting to has some very specific requires
   * </p>
   * 
   * @param kvps the defaultConfiguration to set
   */
  public void setDefaultConfiguration(KeyValuePairSet kvps) {
    this.defaultConfiguration = Args.notNull(kvps, "defaultConfiguration");
  }

}
