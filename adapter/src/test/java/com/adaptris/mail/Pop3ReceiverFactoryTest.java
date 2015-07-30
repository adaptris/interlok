package com.adaptris.mail;

import com.icegreen.greenmail.pop3.Pop3Server;
import com.icegreen.greenmail.util.GreenMail;

public class Pop3ReceiverFactoryTest extends Pop3FactoryCase {

  public Pop3ReceiverFactoryTest(String name) {
    super(name);
  }

  @Override
  Pop3ReceiverFactory create() {
    return new Pop3ReceiverFactory();
  }

  @Override
  Pop3Server getServer(GreenMail gm) {
    return gm.getPop3();
  }

  @Override
  Pop3ReceiverFactory configure(Pop3ReceiverFactory f) {
    f.setConnectTimeout(60000);
    f.setKeepAlive(true);
    f.setReceiveBufferSize(8192);
    f.setSendBufferSize(8192);
    f.setTcpNoDelay(true);
    f.setTimeout(60000);
    return f;
  }

}
