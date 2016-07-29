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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.util.KeyValuePairSet;
import com.jcraft.jsch.ConfigRepository;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A statically configured SSH {@link ConfigRepository} with global defaults for all hosts.
 * 
 * @config sftp-inline-config-repository
 */
@XStreamAlias("sftp-inline-config")
@DisplayOrder(order = {"config", "proxy"})
public class InlineConfigBuilder extends ConfigBuilderImpl implements ConfigRepository {

  @AutoPopulated
  @NotNull
  @Valid
  private KeyValuePairSet config;


  public InlineConfigBuilder() {
    setConfig(new KeyValuePairSet());
  }

  @Override
  public Config getConfig(final String host) {
    return new Config() {
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
        return config.getValue(key);
      }

      @Override
      public String[] getValues(String key) {
        return null;
      }
      
    };
  }


  /**
   * @return the config
   */
  public KeyValuePairSet getConfig() {
    return config;
  }

  /**
   * Set your configuration options.
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
   * @param config the config to set
   */
  public void setConfig(KeyValuePairSet config) {
    this.config = config;
  }


  @Override
  public ConfigRepository buildConfigRepository() throws IOException {
    return this;
  }

}
