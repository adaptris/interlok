/*******************************************************************************
 * Copyright 2019 Adaptris Ltd.
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
 *******************************************************************************/
package com.adaptris.core.services.mime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import javax.mail.internet.MimeBodyPart;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.metadata.RegexMetadataFilter;

public class MimePartBuilderTest {

  @Test
  public void testBuild() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    InlineMimePartBuilder builder = new InlineMimePartBuilder();
    MimeBodyPart part = builder.build(msg);
    assertNotNull(part);
    assertNotNull(part.getContentID());
  }

  @Test
  public void testBuild_WithContentEncoding() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    InlineMimePartBuilder builder = new InlineMimePartBuilder().withContentEncoding("base64");
    MimeBodyPart part = builder.build(msg);
    assertNotNull(part);
    assertNotNull(part.getContentID());
    assertNotNull(part.getEncoding());
  }

  @Test
  public void testBuild_WithContentId() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    InlineMimePartBuilder builder =
        new InlineMimePartBuilder().withContentId("%message{%uniqueId}");
    MimeBodyPart part = builder.build(msg);
    assertNotNull(part);
    assertEquals(msg.getUniqueId(), part.getContentID());
  }

  @Test
  public void testBuild_WithPartHeader() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    msg.addMetadata("X-Interlok-Mime", "yes");
    InlineMimePartBuilder builder =
        new InlineMimePartBuilder().withContentId("%message{%uniqueId}")
            .withPartHeaderFilter(new RegexMetadataFilter().withIncludePatterns("X-Interlok.*"));
    MimeBodyPart part = builder.build(msg);
    assertNotNull(part);
    assertEquals(msg.getUniqueId(), part.getContentID());
    assertEquals(1, part.getHeader("X-Interlok-Mime").length);
  }
}
