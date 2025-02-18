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

package com.adaptris.core;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.text.mime.BodyPartIterator;
import com.adaptris.util.text.mime.MimeConstants;
import com.adaptris.util.text.mime.MultiPartOutput;

/**
 * Multi-payload message MIME encoder. Encode a multi-payload message with each payload as a separate MIME block.
 *
 * <pre>{@code
 * <encoder class="com.adaptris.core.MultiPayloadMessageMimeEncoder">
 *   <metadata-encoding>base64</metadata-encoding>
 *   <payload-encoding>base64</payload-encoding>
 *   <retain-unique-id>true</retain-unique-id>
 * </encoder>
 * }</pre>
 *
 * @author amanderson
 * @see MultiPayloadAdaptrisMessage
 * @since 3.9.3
 */

@ComponentProfile(summary = "A multi-payload message MIME encoder/decoder", tag = "multi-payload,MIME,encode,decode", since = "3.9.3")
public class MultiPayloadMessageMimeEncoder extends MimeEncoderImpl<OutputStream, InputStream> {

  protected static final String CURRENT_PAYLOAD_ID = "AdaptrisMessage/current-payload-id";

  public MultiPayloadMessageMimeEncoder() {
    super();
    registerMessageFactory(new MultiPayloadMessageFactory());
  }

  @Override
  public void writeMessage(AdaptrisMessage msg, OutputStream target) throws CoreException {
    try {
      MultiPartOutput output = new MultiPartOutput(msg.getUniqueId());
      if (msg instanceof MultiPayloadAdaptrisMessage message) {
        if (StringUtils.isNotEmpty(message.getCurrentPayloadId())) {
          output.setHeader(CURRENT_PAYLOAD_ID, message.getCurrentPayloadId());
        }
        for (String id : message.getPayloadIDs()) {
          message.switchPayload(id);
          output.addPart(payloadAsMimePart(message), PAYLOAD_CONTENT_ID + "/" + id);
        }
      } else {
        output.addPart(payloadAsMimePart(msg), PAYLOAD_CONTENT_ID);
      }
      output.addPart(getMetadata(msg), getMetadataEncoding(), METADATA_CONTENT_ID);
      if (msg.getObjectHeaders().containsKey(CoreConstants.OBJ_METADATA_EXCEPTION)) {
        output.addPart(asMimePart((Exception) msg.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION)), EXCEPTION_CONTENT_ID);
      }
      writeNextServiceId(output, msg);
      output.writeTo(target);
      target.flush();
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  private MimeBodyPart payloadAsMimePart(MultiPayloadAdaptrisMessage m) throws Exception {
    MimeBodyPart p = new MimeBodyPart();
    p.setDataHandler(new DataHandler(new MessageDataSource(m)));
    if (!isEmpty(getPayloadEncoding())) {
      p.setHeader(MimeConstants.HEADER_CONTENT_ENCODING, getPayloadEncoding());
    }
    return p;
  }

  @Override
  public AdaptrisMessage readMessage(InputStream source) throws CoreException {
    try {
      BodyPartIterator input = new BodyPartIterator(source);
      AdaptrisMessage msg = currentMessageFactory().newMessage();
      if (msg instanceof MultiPayloadAdaptrisMessage message) {
        message.deletePayload(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID); // Delete the default payload
        addPartsToMessage(input, message);
      } else {
        addPartsToMessage(input, msg);
      }
      return msg;
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  protected void addPartsToMessage(BodyPartIterator input, MultiPayloadAdaptrisMessage message) throws IOException, MessagingException {
    for (int i = 0; i < input.size(); i++) {
      MimeBodyPart payloadPart = Args.notNull(input.getBodyPart(i), "payload");
      String id = payloadPart.getContentID();
      if (!id.startsWith(PAYLOAD_CONTENT_ID)) {
        continue;
      }
      if (id.length() > PAYLOAD_CONTENT_ID.length() + 1) {
        id = id.substring(PAYLOAD_CONTENT_ID.length() + 1);
        if (message.hasPayloadId(id)) {          
          message.switchPayload(id);
        } else {
          message.addPayload(id, new byte[0]);
        }
      }
      try (InputStream payloadIn = payloadPart.getInputStream(); OutputStream out = message.getOutputStream()) {
        IOUtils.copy(payloadIn, out);
      }
    }
    String currentPayloadId = input.getHeaders().getHeader(CURRENT_PAYLOAD_ID, null);
    if (StringUtils.isNotEmpty(currentPayloadId)) {
      message.switchPayload(currentPayloadId);
    }
    MimeBodyPart metadataPart = Args.notNull(input.getBodyPart(METADATA_CONTENT_ID), "metadata");
    try (InputStream metadata = metadataPart.getInputStream()) {
      message.addMetadata(getMetadataSet(metadata));
    }
    if (retainUniqueId()) {
      message.setUniqueId(input.getMessageID());
    }
    if (retainNextServiceId()) {
      message.setNextServiceId(StringUtils.trimToEmpty(input.getHeaders().getHeader(NEXT_SERVICE_ID, null)));
    }
  }

  private static class MessageDataSource implements DataSource {
    private MultiPayloadAdaptrisMessage message;
    private String id;

    private MessageDataSource(MultiPayloadAdaptrisMessage msg) {
      id = msg.getCurrentPayloadId();
      message = msg;
    }

    @Override
    public String getContentType() {
      return "application/octet-stream";
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return message.getInputStream(id);
    }

    @Override
    public String getName() {
      return message.getUniqueId();
    }

    @Override
    public OutputStream getOutputStream() {
      throw new UnsupportedOperationException();
    }
  }
}
