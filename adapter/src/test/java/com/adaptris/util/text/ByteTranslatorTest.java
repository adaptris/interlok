package com.adaptris.util.text;

import junit.framework.TestCase;

import com.adaptris.util.GuidGenerator;

public class ByteTranslatorTest extends TestCase {

  public ByteTranslatorTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testBase64Translator() throws Exception {
    ByteTranslator b = new Base64ByteTranslator();
    String uniq = new GuidGenerator().getUUID();
    String base64String = new String(Conversion.byteArrayToBase64String(uniq
        .getBytes()));
    byte[] bytes = b.translate(base64String);
    String result = b.translate(bytes);
    assertEquals(base64String, result);
  }

  public void testCharsetByteTranslatorUsingIso8859() throws Exception {
    CharsetByteTranslator b = new CharsetByteTranslator("ISO-8859-1");
    String uniq = new GuidGenerator().getUUID();
    byte[] bytes = b.translate(uniq);
    String result = b.translate(bytes);
    assertEquals(uniq, result);
    assertEquals("ISO-8859-1", b.getCharsetEncoding());
  }

  public void testCharsetByteTranslator() throws Exception {
    CharsetByteTranslator b = new CharsetByteTranslator();
    String uniq = new GuidGenerator().getUUID();
    byte[] bytes = b.translate(uniq);
    String result = b.translate(bytes);
    assertEquals("UTF-8", b.getCharsetEncoding());
    assertEquals(uniq, result);
  }

  public void testSimpleByteTranslator() throws Exception {
    ByteTranslator b = new SimpleByteTranslator();
    String uniq = new GuidGenerator().getUUID();
    byte[] bytes = b.translate(uniq);
    String result = b.translate(bytes);
    assertEquals(uniq, result);
  }

}
