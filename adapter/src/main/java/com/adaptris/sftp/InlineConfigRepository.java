package com.adaptris.sftp;

import java.io.IOException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.util.KeyValuePairSet;
import com.jcraft.jsch.ConfigRepository;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A statically configured SSH {@link ConfigRepository} with global defaults for all hosts.
 * 
 * @config sftp-inline-config-repository
 */
@XStreamAlias("sftp-inline-config-repository")
public class InlineConfigRepository implements ConfigRepository, ConfigRepositoryBuilder {

  @AutoPopulated
  @NotNull
  @Valid
  private KeyValuePairSet config;


  public InlineConfigRepository() {
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
   * @param config the config to set
   */
  public void setConfig(KeyValuePairSet config) {
    this.config = config;
  }


  @Override
  public ConfigRepository build() throws IOException {
    return this;
  }

}
