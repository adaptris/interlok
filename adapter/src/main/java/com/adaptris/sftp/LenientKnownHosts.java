package com.adaptris.sftp;

import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Implementation of {@link SftpConnectionBehaviour} where you can specify a known hosts file but will automatically add new server
 * keys into the specified file when connecting.
 * 
 * @config lenient-known-hosts
 * 
 * @author dsefton
 * 
 */
@XStreamAlias("lenient-known-hosts")
public class LenientKnownHosts extends SftpBehaviourImpl {

  public LenientKnownHosts() {
    super();
  }

  public LenientKnownHosts(String knownHostsFile, boolean compression) {
    this();
    setKnownHostsFile(knownHostsFile);
    setUseCompression(compression);
  }

  @Override
  protected void doConfigure(SftpClient c) throws SftpException {
    c.setHostKeyChecking(false);
    c.setKnownHosts(getKnownHostsFile());
  }

}
