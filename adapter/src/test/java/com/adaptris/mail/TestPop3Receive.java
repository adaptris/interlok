package com.adaptris.mail;

import javax.mail.URLName;

import org.apache.commons.net.pop3.POP3Client;

import com.icegreen.greenmail.pop3.Pop3Server;
import com.icegreen.greenmail.util.GreenMail;

public class TestPop3Receive extends MailReceiverCase {

  public TestPop3Receive(String name) {
    super(name);
  }

  protected MailReceiver createClient(GreenMail gm) throws Exception {
    Pop3Server server = gm.getPop3();
    String pop3UrlString = server.getProtocol() + "://localhost:" + server.getPort() + "/INBOX";
    URLName pop3Url = createURLName(pop3UrlString, DEFAULT_POP3_USER, DEFAULT_ENCODED_POP3_PASSWORD);
    ApachePOP3 client = new ApachePOP3(pop3Url, new ApacheClientConfig() {

      @Override
      POP3Client preConnectConfigure(POP3Client client) throws MailException {
        return client;
      }

      @Override
      POP3Client postConnectConfigure(POP3Client client) throws MailException {
        return client;
      }
    });
    return client;
  }

}