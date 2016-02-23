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

import com.adaptris.annotation.DisplayOrder;
import com.thoughtworks.xstream.annotations.XStreamAlias;



/**
 * Implementation of {@link SftpConnectionBehaviour} where you can specify a known hosts file and connections will fail if the
 * servers key is missing from the file.
 * 
 * @config strict-known-hosts
 * 
 * @author dsefton
 * 
 */
@XStreamAlias("strict-known-hosts")
@DisplayOrder(order = {"knownHostsFile", "useCompression"})
public class StrictKnownHosts extends SftpBehaviourImpl {

  public StrictKnownHosts() {
    super();
  }

  public StrictKnownHosts(String knownHostsFile, boolean compression) {
    this();
    setKnownHostsFile(knownHostsFile);
    setUseCompression(compression);
  }

  @Override
  protected void doConfigure(SftpClient c) throws SftpException {
    c.setHostKeyChecking(true);
    c.setKnownHosts(getKnownHostsFile());
  }

}
