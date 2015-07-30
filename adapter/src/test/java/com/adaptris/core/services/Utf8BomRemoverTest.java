package com.adaptris.core.services;

import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.GeneralServiceExample;

public class Utf8BomRemoverTest extends GeneralServiceExample {

  private static final byte[] UTF_8_BOM =
  {
      (byte) 0xEF, (byte) 0xBB, (byte) 0xBF,
  };

  private static final String PAYLOAD = "Pack my box with five dozen liquor jugs";

  public Utf8BomRemoverTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testServiceWithBom() throws Exception {
    AdaptrisMessage msg = create(true);
      execute(new Utf8BomRemover(), msg);
    assertEquals(PAYLOAD, msg.getStringPayload());
  }

  public void testServiceWithoutBom() throws Exception {
    AdaptrisMessage msg = create(false);
    execute(new Utf8BomRemover(), msg);
    assertEquals(PAYLOAD, msg.getStringPayload());
  }

  private AdaptrisMessage create(boolean includeBom) throws Exception {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    OutputStream out = msg.getOutputStream();
    OutputStreamWriter writer = null;
    try {
      if (includeBom) {
        out.write(UTF_8_BOM);
        out.flush();
      }
      writer = new OutputStreamWriter(out);
      writer.write(PAYLOAD);
      writer.flush();
    }
    finally {
      IOUtils.closeQuietly(writer);
      IOUtils.closeQuietly(out);
    }

    return msg;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new Utf8BomRemover();
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "<!--" + "\nThis is only really useful when Windows (.NET application or otherwise)"
        + "\ngenerated files are being processed by the adapter. In almost all situations,"
        + "\nwindows will output a redundant UTF-8 BOM which may cause issues with certain types"
        + "\nof XML processing. In the event that no BOM is detected, then nothing is " + "\ndone to the message.\n" + "\n-->\n";
  }
}
