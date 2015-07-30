/*
 * $RCSfile: ByteTranslatorTest.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/07/24 11:05:03 $
 * $Author: lchan $
 */
package com.adaptris.util.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.security.MessageDigest;

import org.junit.Test;

public class ConversionTest {

  private static final String TEXT = "The Quick Brown Fox Jumps Over The Lazy Dog";

  private static final String HEX_STRING = "c77321cd7ec8ee99c7abd38c1ac8ee99c773218c";

  private static final byte[] BYTES =
  {
      (byte) 0xc7, (byte) 0x73, (byte) 0x21, (byte) 0xcd, (byte) 0x7e, (byte) 0xc8, (byte) 0xee, (byte) 0x99, (byte) 0xc7,
      (byte) 0xab, (byte) 0xd3, (byte) 0x8c, (byte) 0x1a, (byte) 0xc8, (byte) 0xee, (byte) 0x99, (byte) 0xc7, (byte) 0x73,
      (byte) 0x21, (byte) 0x8c
  };

  @Test
  public void testBase64() {
    String base64 = Conversion.byteArrayToBase64String(TEXT.getBytes());
    assertEquals(TEXT, new String(Conversion.base64StringToByteArray(base64)));
    assertNotNull(Conversion.byteArrayToBase64String(BYTES));
  }

  @Test
  public void testHex() throws Exception {
    assertNotNull(Conversion.byteArrayToHexString(TEXT.getBytes()));
    assertTrue(MessageDigest.isEqual(BYTES, Conversion.hexStringToByteArray(HEX_STRING)));
    // assertEquals(BYTES, new
    // String(Conversion.hexStringToByteArray(HEX_STRING)));
  }

  @Test
  public void testHex_InvalidChars() throws Exception {
    try {
      Conversion.hexStringToByteArray("0d0aGG");
      fail();
    }
    catch (IOException expected) {
      assertEquals("Illegal hexadecimal character G", expected.getMessage());

    }
  }

  @Test
  public void testHex_InsufficientChars() throws Exception {
    try {
      // Odd numbers of chars should fail.
      Conversion.hexStringToByteArray("0d0ac");
      fail();
    }
    catch (IOException expected) {
      assertEquals("Odd number of characters.", expected.getMessage());
    }
  }

}
