package com.adaptris.mail;

import com.icegreen.greenmail.pop3.Pop3Server;
import com.icegreen.greenmail.util.GreenMail;

public class TestJavamailPop3Receive extends MailReceiverCase {

  public TestJavamailPop3Receive(String name) {
    super(name);
  }

  protected MailReceiver createClient(GreenMail gm) throws Exception {
    Pop3Server server = gm.getPop3();
    String pop3Url = server.getProtocol() + "://localhost:" + server.getPort() + "/INBOX";
    MailboxClient client = new MailboxClient(createURLName(pop3Url, DEFAULT_POP3_USER, DEFAULT_ENCODED_POP3_PASSWORD));
    return client;
  }


}