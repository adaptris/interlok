/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.services;

import static org.junit.Assert.assertEquals;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.GeneralServiceExample;

public class Utf8BomRemoverTest extends GeneralServiceExample {

  private static final byte[] UTF_8_BOM =
  {
      (byte) 0xEF, (byte) 0xBB, (byte) 0xBF,
  };

  private static final String PAYLOAD = "Pack my box with five dozen liquor jugs";


  @Test
  public void testServiceWithBom() throws Exception {
    AdaptrisMessage msg = create(true);
      execute(new Utf8BomRemover(), msg);
    assertEquals(PAYLOAD, msg.getContent());
  }

  @Test
  public void testServiceWithoutBom() throws Exception {
    AdaptrisMessage msg = create(false);
    execute(new Utf8BomRemover(), msg);
    assertEquals(PAYLOAD, msg.getContent());
  }

  private AdaptrisMessage create(boolean includeBom) throws Exception {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    try (OutputStream out = msg.getOutputStream()) {
      if (includeBom) {
        out.write(UTF_8_BOM);
        out.flush();
      }
      try (OutputStreamWriter writer = new OutputStreamWriter(out)) {
        writer.write(PAYLOAD);
      }
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
