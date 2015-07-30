package com.adaptris.core.services.findreplace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;

public class HexSequenceConfiguredReplacementSourceTest {

  private static final String CRLF_HEX = "0d0a";
  private static final String CRLF = "\r\n";

  @Test
  public void testSetCharset() throws Exception {
    HexSequenceConfiguredReplacementSource service = new HexSequenceConfiguredReplacementSource();
    assertNull(service.getCharset());
    service.setCharset("UTF-8");
    assertEquals("UTF-8", service.getCharset());
  }

  @Test
  public void testObtainReplaceWith_NoCharset() throws Exception {
    HexSequenceConfiguredReplacementSource service = new HexSequenceConfiguredReplacementSource();
    service.setValue(CRLF_HEX);
    String s = service.obtainValue(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    assertEquals(CRLF, s);
  }

  @Test
  public void testObtainReplaceWith_Charset() throws Exception {
    HexSequenceConfiguredReplacementSource service = new HexSequenceConfiguredReplacementSource("UTF-8");
    service.setValue(CRLF_HEX);
    String s = service.obtainValue(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    assertEquals(CRLF, s);
  }

  @Test
  public void testObtainReplaceWith_InvalidCharset() throws Exception {
    try {
      HexSequenceConfiguredReplacementSource service = new HexSequenceConfiguredReplacementSource("BlahDeBlah");
      service.setValue(CRLF_HEX);
      String s = service.obtainValue(AdaptrisMessageFactory.getDefaultInstance().newMessage());
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  @Test
  public void testObtainReplaceWith_InvalidHex() throws Exception {
    try {
      HexSequenceConfiguredReplacementSource service = new HexSequenceConfiguredReplacementSource();
      service.setValue("0a0dgg");
      String s = service.obtainValue(AdaptrisMessageFactory.getDefaultInstance().newMessage());
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  @Test
  public void testObtainReplaceWith_InsufficientHex() throws Exception {
    try {
      HexSequenceConfiguredReplacementSource service = new HexSequenceConfiguredReplacementSource();
      service.setValue("0d0ac");
      // This is an odd number of valid hex chars...
      service.obtainValue(AdaptrisMessageFactory.getDefaultInstance().newMessage());
      fail();
    }
    catch (ServiceException expected) {
    }
  }

  @Test
  public void testObtainReplaceWith_NullMessage() throws Exception {
    HexSequenceConfiguredReplacementSource service = new HexSequenceConfiguredReplacementSource();
    service.setValue(CRLF_HEX);
    String s = service.obtainValue(null);
    assertEquals(CRLF, s);

  }

}
