package com.adaptris.mail;

import java.io.IOException;

import javax.mail.URLName;

import org.apache.commons.net.pop3.POP3Client;

/**
 * Mailbox client implementation using commons net pop3.
 * 
 * @author lchan
 * 
 */
class ApachePOP3 extends ApacheMailClient<POP3Client> {

  ApachePOP3(URLName url, ApacheClientConfig clientConfig) {
    super(url, clientConfig);
  }

  @Override
  POP3Client createClient() throws MailException {
    return new POP3Client();
  }

  @Override
  void postConnectAction(POP3Client client) throws MailException, IOException {
  }
}
