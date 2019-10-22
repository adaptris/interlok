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
import javax.validation.constraints.NotBlank;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.util.Args;
import com.jcraft.jsch.ConfigRepository;
import com.jcraft.jsch.OpenSSHConfig;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Builds a {@link ConfigRepository} based on an OpenSSH configuration file.
 * 
 * @config openssh-config-repository
 */
@XStreamAlias("openssh-config-builder")
@DisplayOrder(order = {"opensshConfigFile", "proxy"})
public class OpenSSHConfigBuilder extends ConfigBuilderImpl {

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
  public ConfigRepository buildConfigRepository() throws IOException {
    return OpenSSHConfig.parseFile(getOpensshConfigFile());
  }

}
