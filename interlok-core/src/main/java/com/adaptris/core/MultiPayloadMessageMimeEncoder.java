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

import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.text.mime.BodyPartIterator;
import com.adaptris.util.text.mime.MultiPartFileInput;
import com.adaptris.util.text.mime.MultiPartOutput;
import org.apache.commons.io.IOUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MultiPayloadMessageMimeEncoder extends MimeEncoderImpl {

  public MultiPayloadMessageMimeEncoder() {
    super();
    registerMessageFactory(new MultiPayloadMessageFactory());
  }

  @Override
  public void writeMessage(AdaptrisMessage msg, Object target) throws CoreException {
    try {
      File baseFile = asFile(target);
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
      try (OutputStream out = new FileOutputStream(baseFile)) {
        output.writeTo(out);
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  public AdaptrisMessage readMessage(Object source) throws CoreException {
    try {
      MultiPayloadAdaptrisMessage msg = (MultiPayloadAdaptrisMessage)currentMessageFactory().newMessage();
      File baseFile = asFile(source);
      MultiPartFileInput input = new MultiPartFileInput(baseFile);
      addPartsToMessage(input, msg);
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
      id = id.substring(PAYLOAD_CONTENT_ID.length() + 1);
      message.switchPayload(id);
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
  }

  private File asFile(Object o) {
    if (!(o instanceof File)) {
      throw new IllegalArgumentException("MultiPayloadMessageMimeEncoderImpl can only encode/decode to/from a File");
    }
    return (File)o;
  }
}
