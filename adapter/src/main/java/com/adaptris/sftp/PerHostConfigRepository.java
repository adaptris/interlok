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
import com.adaptris.core.util.Args;
import com.adaptris.util.KeyValuePairSet;
import com.jcraft.jsch.ConfigRepository;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * A statically configured SSH {@link ConfigRepository} that supports different configuration on a per host basis.
 * 
 * @config sftp-per-host-config-repository
 */
@XStreamAlias("sftp-per-host-config-repository")
public class PerHostConfigRepository implements ConfigRepository, ConfigRepositoryBuilder {

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

  public PerHostConfigRepository() {
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
  public ConfigRepository build() throws IOException {
    return this;
  }

  /**
   * @return the defaultConfiguration
   */
  public KeyValuePairSet getDefaultConfiguration() {
    return defaultConfiguration;
  }

  /**
   * @param kvps the defaultConfiguration to set
   */
  public void setDefaultConfiguration(KeyValuePairSet kvps) {
    this.defaultConfiguration = Args.notNull(kvps, "defaultConfiguration");
  }

}
