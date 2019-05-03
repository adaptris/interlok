/*
 * Copyright 2016 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adaptris.core.common;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
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

public class PayloadStreamInputParameterTest {

  private static final String TEXT = "Hello World";

  @Rule
  public TestName testName = new TestName();

  @Test
  public void testExtract() throws Exception {
    PayloadStreamInputParameter p = new PayloadStreamInputParameter();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT.getBytes());
    try (InputStream in = p.extract(msg)) {
      List<String> strings = IOUtils.readLines(in, Charset.defaultCharset());
      assertEquals(1, strings.size());
      assertEquals(TEXT, strings.get(0));
    }
  }

  @Test
  public void testWrap() throws Exception {
    PayloadStreamInputParameter p = new PayloadStreamInputParameter();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT.getBytes());
    try (InputStream in = p.wrap(msg)) {
      List<String> strings = IOUtils.readLines(in, Charset.defaultCharset());
      assertEquals(1, strings.size());
      assertEquals(TEXT, strings.get(0));
    }
  }


  @Test(expected = CoreException.class)
  public void testExtractWithException() throws Exception {
    PayloadStreamInputParameter p = new PayloadStreamInputParameter();
    AdaptrisMessage msg = new MyDefectiveMessage();
    try (InputStream in = p.extract(msg)) {
    }
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
