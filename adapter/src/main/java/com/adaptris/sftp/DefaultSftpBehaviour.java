package com.adaptris.sftp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;



/**
 * Default implementation of {@link SftpConnectionBehaviour} that assumes default behaviour. *
 * 
 * <p>
 * The default behaviour is for server public keys to be added to <code>~/.ssh/known_hosts</code> automatically. It is functionally
 * equivalent to {@link LenientKnownHosts} but without the ability to specify a specific known hosts file or compression.
 * </p>
 * 
 * @config sftp-default-behaviour
 * 
 * @author dsefton
 * 
 */
@XStreamAlias("sftp-default-behaviour")
public class DefaultSftpBehaviour implements SftpConnectionBehaviour {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public void configure(SftpClient c) throws SftpException {
  }

}
