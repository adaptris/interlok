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

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.text.mime.BodyPartIterator;
import com.adaptris.util.text.mime.MimeConstants;
import com.adaptris.util.text.mime.MultiPartOutput;
import org.apache.commons.io.IOUtils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Multi-payload message MIME encoder. Encode a multi-payload message
 * with each payload as a separate MIME block.
 *
 * @author amanderson
 * @see MultiPayloadAdaptrisMessage
 * @since 3.9.3
 */
@ComponentProfile(summary = "A multi-payload message MIME encoder/decoder", tag = "multi-payload,MIME,encode,decode", since="3.9.3")
public class MultiPayloadMessageMimeEncoder extends MimeEncoderImpl {

  public MultiPayloadMessageMimeEncoder() {
    super();
    registerMessageFactory(new MultiPayloadMessageFactory());
  }

  @Override
  public void writeMessage(AdaptrisMessage msg, Object target) throws CoreException {
    try {
      if (!(target instanceof OutputStream)) {
        throw new IllegalArgumentException("MultiPayloadMessageMimeEncoder can only encode to an OutputStream");
      }
      MultiPartOutput output = new MultiPartOutput(msg.getUniqueId());
      if (msg instanceof MultiPayloadAdaptrisMessage) {
        MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)msg;
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
      output.writeTo((OutputStream)target);
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  protected MimeBodyPart payloadAsMimePart(MultiPayloadAdaptrisMessage m) throws Exception {
    MimeBodyPart p = new MimeBodyPart();
    p.setDataHandler(new DataHandler(new MessageDataSource(m)));
    if (!isEmpty(getPayloadEncoding())) {
      p.setHeader(MimeConstants.HEADER_CONTENT_ENCODING, getPayloadEncoding());
    }
    return p;
  }

  @Override
  public AdaptrisMessage readMessage(Object source) throws CoreException {
    try {
      if (!(source instanceof InputStream)) {
        throw new IllegalArgumentException("MultiPayloadMessageMimeEncoder can only decode from an OutputStream");
      }
      MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)currentMessageFactory().newMessage();
      BodyPartIterator input = new BodyPartIterator((InputStream)source);
      boolean deleteDefault = addPartsToMessage(input, message);
      if (deleteDefault) {
        message.deletePayload(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID); // delete the unused default
      }
      return message;
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  protected boolean addPartsToMessage(BodyPartIterator input, MultiPayloadAdaptrisMessage message) throws IOException, MessagingException {
    boolean deleteDefault = false;
    for (int i = 0; i < input.size(); i++) {
      MimeBodyPart payloadPart = Args.notNull(input.getBodyPart(i), "payload");
      String id = payloadPart.getContentID();
      if (!id.startsWith(PAYLOAD_CONTENT_ID)) {
        continue;
      }
      if (id.length() > PAYLOAD_CONTENT_ID.length() + 1 ) {
        id = id.substring(PAYLOAD_CONTENT_ID.length() + 1);
        message.switchPayload(id);
        deleteDefault = true;
      }
      try (InputStream payloadIn = payloadPart.getInputStream();
           OutputStream out = message.getOutputStream()) {
        IOUtils.copy(payloadIn, out);
      }
    }
    MimeBodyPart metadataPart = Args.notNull(input.getBodyPart(METADATA_CONTENT_ID), "metadata");
    try (InputStream metadata = metadataPart.getInputStream()) {
      message.setMetadata(getMetadataSet(metadata));
    }
    if (retainUniqueId()) {
      message.setUniqueId(input.getMessageID());
    }
    return deleteDefault;
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
