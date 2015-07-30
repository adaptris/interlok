package com.adaptris.mail;

import org.apache.commons.net.pop3.POP3Client;


/**
 * Allows configuration of additional settings on the POP3 Client.
 * 
 * @author lchan
 * 
 */
abstract class ApacheClientConfig {

  /**
   * Configure things like Connection Timeout, Cipher Suites etc.
   * 
   */
  abstract POP3Client preConnectConfigure(POP3Client client) throws MailException;

  /**
   * Configure things like TCPNODELAY and SO_KEEPALIVE that only work when you have a socket open.
   * 
   */
  abstract POP3Client postConnectConfigure(POP3Client client) throws MailException;

}
