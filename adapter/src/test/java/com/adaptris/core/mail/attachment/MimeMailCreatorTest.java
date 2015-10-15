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

package com.adaptris.core.mail.attachment;

import static com.adaptris.core.services.mime.MimeJunitHelper.PAYLOAD_1;
import static com.adaptris.core.services.mime.MimeJunitHelper.PAYLOAD_2;
import static com.adaptris.core.services.mime.MimeJunitHelper.PAYLOAD_3;

import java.util.List;

import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BaseCase;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.text.mime.MultiPartOutput;
import com.adaptris.util.text.mime.SelectByPosition;

/**
 * @author lchan
 * @author $Author: lchan $
 */
public class MimeMailCreatorTest extends BaseCase {

  private static final String[] PAYLOADS =
  {
      PAYLOAD_1, PAYLOAD_2, PAYLOAD_3
  };

  private static final String[] CONTENT_TYPES =
  {
      "application/octet-stream; name=\"attachment1.txt\"", "text/plain", "application/octet-stream; name=\"attachment3.txt\""

  };

  private static final String[] CONTENT_DISPOSITON =
  {
      "attachment; name=\"attachment1.txt\"", "attachment;",
      "attachment;"

  };

  /**
   * @param name
   */
  public MimeMailCreatorTest(String name) {
    super(name);
  }

  public void testBodyCreation() throws Exception {
    MimeMailCreator mmc = new MimeMailCreator();
    mmc.setBodySelector(new SelectByPosition(1));
    MailContent mc =  mmc.createBody(create());
    log.trace(mc);
    assertEquals(PAYLOAD_2, new String(mc.getBytes()));
    assertEquals("text/plain", mc.getContentType());
  }

  public void testAttachmentCreation() throws Exception {
    MimeMailCreator mmc = new MimeMailCreator();
    mmc.setBodySelector(new SelectByPosition(1));
    List<MailAttachment> list =  mmc.createAttachments(create());
    assertEquals(2, list.size());
    MailAttachment a = list.get(0);
    log.trace(a);
    assertEquals(PAYLOAD_1, new String(a.getBytes()));
    assertEquals("attachment1.txt", a.getFilename());
    a = list.get(1);
    log.trace(a);
    assertEquals(PAYLOAD_3, new String(a.getBytes()));
    assertEquals("attachment3.txt", a.getFilename());
  }

  protected static AdaptrisMessage create() throws Exception {
    MultiPartOutput output = new MultiPartOutput(new GuidGenerator().getUUID());
    for (int i = 0; i < PAYLOADS.length; i++) {
      InternetHeaders hdr = new InternetHeaders();
      hdr.addHeader("Content-Type", CONTENT_TYPES[i]);
      hdr.addHeader("Content-Disposition", CONTENT_DISPOSITON[i]);
      MimeBodyPart part = new MimeBodyPart(hdr, PAYLOADS[i].getBytes());
      output.addPart(part, new GuidGenerator().getUUID());
    }
    return AdaptrisMessageFactory.getDefaultInstance().newMessage(
        output.getBytes());

  }

}
