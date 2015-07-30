package com.adaptris.core.services;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.GeneralServiceExample;

public class ChangeCharEncodingServiceTest extends GeneralServiceExample {

  public ChangeCharEncodingServiceTest(String name) {
    super(name);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new ChangeCharEncodingService("iso-8859-1");
  }

  public void testSetCharEncoding() {
    ChangeCharEncodingService srv = new ChangeCharEncodingService();
    assertNull(srv.getCharEncoding());
    srv.setCharEncoding("UTF-8");
    assertEquals("UTF-8", srv.getCharEncoding());
    srv.setCharEncoding(null);
    assertEquals(null, srv.getCharEncoding());
  }


  public void testChangeCharset() throws Exception {
    ChangeCharEncodingService srv = new ChangeCharEncodingService();
    srv.setCharEncoding("iso-8859-1");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello");
    assertNull(msg.getCharEncoding());
    execute(srv, msg);
    assertEquals("iso-8859-1", msg.getCharEncoding());
  }

}
