package com.adaptris.core.util;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.junit.Test;

public class EncodingHelperTest extends EncodingHelper {

  @Test
  public void testEncoding() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
    for (Encoding e : Encoding.values()) {
      assertNotNull(e.wrap(in));
      assertNotNull(e.wrap(out));
      if (e != Encoding.None) {
        assertNotEquals(ByteArrayInputStream.class, e.wrap(in).getClass());
        assertNotEquals(ByteArrayOutputStream.class, e.wrap(out).getClass());
      }
    }
  }


  @Test
  public void testBase64Encoding() throws Exception {
    for (Base64Encoding e : Base64Encoding.values()) {
      assertNotNull(e.encoder());
      assertNotNull(e.decoder());
    }
  }


}
