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

package com.adaptris.sftp;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass for {@link StrictKnownHosts} and {@link LenientKnownHosts} allowing compression and a specified known_hosts
 * file.
 *
 * @author lchan
 *
 */
public abstract class SftpBehaviourImpl implements SftpConnectionBehaviour {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  @NotBlank
  private String knownHostsFile;
  private boolean useCompression;

  public SftpBehaviourImpl() {
    useCompression = false;
  }

  public String getKnownHostsFile() {
    return knownHostsFile;
  }

  /**
   * Specify the known hosts file to be used.
   *
   * @param s
   */
  public void setKnownHostsFile(String s) {
    knownHostsFile = s;
  }

  public boolean getUseCompression() {
    return useCompression;
  }

  public void setUseCompression(boolean useCompression) {
    this.useCompression = useCompression;
  }

  @Override
  public final void configure(SftpClient c) throws SftpException {
    c.setUseCompression(useCompression);
    doConfigure(c);
  }

  protected abstract void doConfigure(SftpClient c) throws SftpException;
}
