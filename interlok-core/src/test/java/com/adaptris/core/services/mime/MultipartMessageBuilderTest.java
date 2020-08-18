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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.apache.commons.lang3.BooleanUtils;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.ByteArrayFromMetadata;
import com.adaptris.core.common.ByteArrayFromObjectMetadata;
import com.adaptris.core.common.ByteArrayFromPayload;
import com.adaptris.core.metadata.RegexMetadataFilter;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.adaptris.util.GuidGenerator;

public class MultipartMessageBuilderTest extends MimeServiceExample {

  @Override
  protected MultipartMessageBuilder retrieveObjectForSampleConfig() {
    return new MultipartMessageBuilder().withMimeParts(
        new InlineMimePartBuilder()
            .withBody(new ByteArrayFromObjectMetadata().withKey("ObjectMetadataKey"))
            .withContentType("application/octet-stream").withContentId("from-object-metadata"),
        new InlineMimePartBuilder()
            .withBody(new ByteArrayFromMetadata().withKey("StringMetadataKey"))
            .withContentType("application/octet-stream").withContentId("from-string-metadata"),
        new InlineMimePartBuilder().withBody(new ByteArrayFromPayload())
            .withContentId("%message{%uniqueId}").withPartHeaderFilter(
                new RegexMetadataFilter().withIncludePatterns("Content-Disposition")));
  }

  @Test
  public void testService() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    assertFalse(msg.headersContainsKey(CoreConstants.MSG_MIME_ENCODED));
    MultipartMessageBuilder service =
        new MultipartMessageBuilder().withMimeParts(new InlineMimePartBuilder());
    // default just uses the payload.
    ExampleServiceCase.execute(service, msg);
    String payload = msg.getContent();
    // The unique-id forms the content-id.
    assertTrue(payload.contains(msg.getUniqueId()));
    assertTrue(msg.headersContainsKey(CoreConstants.MSG_MIME_ENCODED));
    assertTrue(
        BooleanUtils.toBoolean(msg.getMetadataValue(CoreConstants.MSG_MIME_ENCODED)));
  }

  @Test
  public void testService_WithContentId() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    msg.addMetadata("customContentId", new GuidGenerator().safeUUID());

    MultipartMessageBuilder service =
        new MultipartMessageBuilder().withMimeParts(new InlineMimePartBuilder())
            .withContentId("%message{customContentId}");
    ExampleServiceCase.execute(service, msg);
    String payload = msg.getContent();
    assertTrue(payload.contains(msg.getMetadataValue("customContentId")));
  }

  @Test
  public void testService_WithSubType() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    MultipartMessageBuilder service = new MultipartMessageBuilder()
        .withMimeParts(new InlineMimePartBuilder()).withMimeContentSubType("form-data");
    ExampleServiceCase.execute(service, msg);
    String payload = msg.getContent();
    System.err.println(payload);
    assertTrue(payload.contains("multipart/form-data"));
  }

  @Test
  public void testService_WithHeader() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    msg.addMetadata("X-Interlok-Mime", "yes");
    MultipartMessageBuilder service =
        new MultipartMessageBuilder().withMimeParts(new InlineMimePartBuilder())
            .withMimeHeaderFilter(new RegexMetadataFilter().withIncludePatterns("X-Interlok.*"));
    // default just uses the payload.
    ExampleServiceCase.execute(service, msg);
    String payload = msg.getContent();
    // The unique-id forms the content-id.
    assertTrue(payload.contains(msg.getUniqueId()));
    assertTrue(payload.contains("X-Interlok-Mime"));
  }

  @Test
  public void testService_Exception() throws Exception {
    AdaptrisMessage msg = new DefectiveMessageFactory(WhenToBreak.OUTPUT).newMessage("Hello World");
    MultipartMessageBuilder service =
        new MultipartMessageBuilder().withMimeParts(new InlineMimePartBuilder());
    try {
      ExampleServiceCase.execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }
}
