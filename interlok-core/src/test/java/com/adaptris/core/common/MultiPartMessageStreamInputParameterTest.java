package com.adaptris.core.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.stubs.DefectiveAdaptrisMessage;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.util.GuidGenerator;

public class MultiPartMessageStreamInputParameterTest {

  private static final String MULTI_PART_NO_HEADER = "\n"
      + "------=_Part_1_1038479175.1649809692675\n"
      + "Content-Type: text/plain\n"
      + "Content-Disposition: form-data; name=\"file\"\n"
      + "Content-ID: file\n"
      + "\n"
      + "Some text\n"
      + "------=_Part_1_1038479175.1649809692675--\n"
      + "";

  private static final String MULTI_PART = "Message-ID: check-service-test-message\n"
      + "Mime-Version: 1.0\n"
      + "Content-Type: multipart/form-data; \n"
      + "    boundary=\"----=_Part_1_1038479175.1649809692675\"\n"
      + "Content-Length: 191\n"
      + MULTI_PART_NO_HEADER;


  @Rule
  public TestName testName = new TestName();

  @Test
  public void testExtract() throws Exception {
    MultiPartMessageStreamInputParameter streamInputParameter = new MultiPartMessageStreamInputParameter();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(MULTI_PART.getBytes());
    try (InputStream in = streamInputParameter.extract(msg)) {
      String result = IOUtils.toString(in, Charset.defaultCharset());
      assertEquals(MULTI_PART_NO_HEADER.replaceAll("\\r", ""), result.replaceAll("\\r", ""));
    }
  }

  @Test
  public void testExtractWithException() throws Exception {
    MultiPartMessageStreamInputParameter p = new MultiPartMessageStreamInputParameter();
    AdaptrisMessage msg = new MyDefectiveMessage();

    assertThrows(CoreException.class, () -> { try (InputStream in = p.extract(msg)) {}});
  }

  private class MyDefectiveMessage extends DefectiveAdaptrisMessage {
    public MyDefectiveMessage() {
      super(new GuidGenerator(), new DefectiveMessageFactory());
    }

    @Override
    public InputStream getInputStream() throws IOException {
      throw new IOException("broken");
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
      throw new IOException("broken");
    }
  }

}
