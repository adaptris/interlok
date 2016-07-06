package com.adaptris.sftp;

import java.io.IOException;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.util.Args;
import com.jcraft.jsch.ConfigRepository;
import com.jcraft.jsch.OpenSSHConfig;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Builds a {@link ConfigRepository} based on an OpenSSH configuration file.
 * 
 * @config openssh-config-repository
 */
@XStreamAlias("openssh-config-repository")
public class OpenSSHConfigBuilder implements ConfigRepositoryBuilder {

  @NotBlank
  private String opensshConfigFile;

  public OpenSSHConfigBuilder() {

  }

  public OpenSSHConfigBuilder(String cfg) {
    this();
    setOpensshConfigFile(cfg);
  }

  /**
   * @return the opensshConfigFile
   */
  public String getOpensshConfigFile() {
    return opensshConfigFile;
  }

  /**
   * Set the OpenSSH configuration file to parse.
   * 
   * @param s the opensshConfigFile to set
   */
  public void setOpensshConfigFile(String s) {
    this.opensshConfigFile = Args.notBlank(s, "opensshConfigFile");
  }

  @Override
  public ConfigRepository build() throws IOException {
    return OpenSSHConfig.parseFile(getOpensshConfigFile());
  }

}
