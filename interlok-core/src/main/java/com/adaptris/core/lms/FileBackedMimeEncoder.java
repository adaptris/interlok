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

package com.adaptris.core.lms;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.*;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.text.mime.MultiPartFileInput;
import com.adaptris.util.text.mime.MultiPartOutput;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Implementation of {@code AdaptrisMessageEncoder} that stores payload and metadata as a mime-encoded multipart message.
 * <p>
 * The expected use case for this is within a {@link LargeFsConsumer} or {@link LargeFsProducer} so that you can capture the
 * metadata along with any exceptions in addition to the payload. Since the data may be arbitrarily large, this implementation works
 * <strong>only with files</strong>.
 * </p>
 * 
 * @config file-backed-mime-encoder
 */
@XStreamAlias("file-backed-mime-encoder")
@DisplayOrder(order = {"payloadEncoding", "metadataEncoding", "retainUniqueId"})
public class FileBackedMimeEncoder extends MimeEncoderImpl<File, File> {

  public FileBackedMimeEncoder() {
    super();
    registerMessageFactory(new FileBackedMessageFactory());
  }

  @Override
  public void writeMessage(AdaptrisMessage msg, File target) throws CoreException {
    try {
      // Use the message unique id as the message id.
      MultiPartOutput output = new MultiPartOutput(msg.getUniqueId());
      AdaptrisMessageFactory factory = currentMessageFactory();
      output.addPart(payloadAsMimePart(msg), PAYLOAD_CONTENT_ID);
      output.addPart(getMetadata(msg), getMetadataEncoding(), METADATA_CONTENT_ID);
      if (msg.getObjectHeaders().containsKey(CoreConstants.OBJ_METADATA_EXCEPTION)) {
        output.addPart(asMimePart((Exception) msg.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION)),
            EXCEPTION_CONTENT_ID);
      }
      try (OutputStream out = new FileOutputStream(target)) {
        // If we are file backed, then lets assume we're large, and we stream to disk first...
        if (factory instanceof FileBackedMessageFactory) {
          output.writeTo(out, ((FileBackedMessageFactory) factory).createTempFile(msg));
        } else {
          output.writeTo(out);
        }
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  public AdaptrisMessage readMessage(File source) throws CoreException {
    try {
      AdaptrisMessage msg = currentMessageFactory().newMessage();
      MultiPartFileInput input = new MultiPartFileInput(source);
      addPartsToMessage(input, msg);
      return msg;
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

}
