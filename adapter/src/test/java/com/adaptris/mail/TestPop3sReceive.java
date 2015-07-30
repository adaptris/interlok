package com.adaptris.mail;

import javax.mail.URLName;
import javax.net.ssl.SSLContext;

import org.apache.commons.net.pop3.POP3Client;
import org.apache.commons.net.util.SSLContextUtils;

import com.adaptris.http.util.AlwaysTrustManager;
import com.icegreen.greenmail.pop3.Pop3Server;
import com.icegreen.greenmail.util.GreenMail;

public class TestPop3sReceive extends MailReceiverCase {

  public TestPop3sReceive(String name) {
    super(name);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  protected MailReceiver createClient(GreenMail gm) throws Exception {
    Pop3Server server = gm.getPop3s();
    String pop3UrlString = server.getProtocol() + "://localhost:" + server.getPort() + "/INBOX";
    URLName pop3Url = createURLName(pop3UrlString, DEFAULT_POP3_USER, DEFAULT_POP3_PASSWORD);
    // Create an SSL context so that we get POP3S w/o certificate authentication!!!.
    SSLContext ctx = SSLContextUtils.createSSLContext("TLS", null, new AlwaysTrustManager());
    ApachePOP3S client = new ApachePOP3S(pop3Url, new ApacheClientConfig() {

      @Override
      POP3Client preConnectConfigure(POP3Client client) throws MailException {
        return client;
      }

      @Override
      POP3Client postConnectConfigure(POP3Client client) throws MailException {
        return client;
      }
    }, true, ctx);
    return client;
  }

}