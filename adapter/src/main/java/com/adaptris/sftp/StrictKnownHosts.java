package com.adaptris.sftp;

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
