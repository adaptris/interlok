/*
 * Copyright 2018 Adaptris Ltd.
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
package com.adaptris.util.text.mime;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;

import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.stubs.TempFileUtils;
import com.adaptris.util.GuidGenerator;

public abstract class PartIteratorCase implements MimeConstants {
  protected static final String PAYLOAD_1 = "The quick brown fox jumps over the lazy dog";
  protected static final String PAYLOAD_2 = "Sixty zippers were quickly picked from the woven jute bag";
  protected static final String PAYLOAD_3 = "Quick zephyrs blow, vexing daft Jim";

  private static GuidGenerator guid = new GuidGenerator();

  protected File generateFileInput() throws Exception {
    return generateFileInput(false, this);
  }

  protected static File generateFileInput(boolean noContentId, Object marker) throws Exception {
    File file = TempFileUtils.createTrackedFile(marker);
    File tempFile = TempFileUtils.createTrackedFile(marker);
    MultiPartOutput output = new MultiPartOutput(guid.getUUID());
    output.addPart(PAYLOAD_1, "payload1");
    output.addPart(PAYLOAD_2, "payload2");
    output.addPart(PAYLOAD_3, "payload3");
    try (FileOutputStream out = new FileOutputStream(file)) {
      output.writeTo(out, tempFile);
    }
    return file;
  }

  protected static String toString(MimeBodyPart p) throws Exception {
    StringWriter out = new StringWriter();
    try (InputStream in = p.getInputStream()) {
      IOUtils.copy(in, out);
    }
    return out.toString();
  }

  protected static byte[] generateByteArrayInput(boolean noContentId) throws Exception {
    MultiPartOutput output = new MultiPartOutput(guid.getUUID());
    output.addPart(PAYLOAD_1, ENCODING_BASE64, "payload1");
    output.addPart(PAYLOAD_2, "payload2");
    output.addPart(PAYLOAD_3, ENCODING_BASE64, "payload3");
    if (noContentId) {
      output.addPart(PAYLOAD_1, ENCODING_BASE64, "");
    }
    return output.getBytes();
  }

  protected static String toString(byte[] b) throws Exception {
    StringWriter out = new StringWriter();
    try (InputStream in = new ByteArrayInputStream(b)) {
      IOUtils.copy(in, out);
    }
    return out.toString();
  }

  protected static MimeMultipart createMultipart() throws Exception {
    MimeMultipart mime = new MimeMultipart();
    try (BodyPartIterator bodies = new BodyPartIterator(generateByteArrayInput(false))) {
      while(bodies.hasNext()) {
        mime.addBodyPart(bodies.next());
      }
    };
    return mime;
  }
}
