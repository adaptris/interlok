package com.adaptris.sftp;

import java.util.Arrays;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.util.Args;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.jcraft.jsch.ConfigRepository;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * @config sftp-host-config
 */
@XStreamAlias("sftp-host-config")
public class HostConfig implements ConfigRepository.Config {

  @NotBlank
  private String hostname;
  private String user;
  private int port = -1;
  private KeyValuePairSet config;

  public HostConfig() {
    setConfig(new KeyValuePairSet());
  }

  public HostConfig(String hostname, String user, int port, KeyValuePairSet config) {
    setHostname(hostname);
    setUser(user);
    setPort(port);
    setConfig(config);
  }

  public HostConfig(String hostname, String user, int port, KeyValuePair... kvps) {
    this(hostname, user, port, new KeyValuePairSet(Arrays.asList(kvps)));
  }

  @Override
  public String getValue(String key) {
    return getConfig().getValue(key);
  }

  /**
   * Not Supported and always returns null
   */
  @Override
  public String[] getValues(String key) {
    return null;
  }

  public KeyValuePairSet getConfig() {
    return config;
  }

  public void setConfig(KeyValuePairSet config) {
    this.config = config;
  }

  public void setHostname(String hostname) {
    this.hostname = Args.notBlank(hostname, "hostname");
  }

  @Override
  public String getHostname() {
    return hostname;
  }


  public void setUser(String user) {
    this.user = user;
  }

  @Override
  public String getUser() {
    return user;
  }

  public void setPort(int port) {
    this.port = port;
  }

  @Override
  public int getPort() {
    return port;
  }

}
