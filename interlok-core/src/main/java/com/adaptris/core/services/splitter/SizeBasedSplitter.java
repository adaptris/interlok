/*
 * Copyright 2015 Adaptris Ltd.
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

package com.adaptris.core.services.splitter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.util.NumberUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link MessageSplitter} implementation that splits by size.
 * <p>
 * Attempts to split an AdaptrisMessage object by size. Reads the specified number of bytes from the input stream when required
 * during the split. Note that while an attempt is made to make sure that each split message is of the same size, this is not
 * guaranteed.
 * </p>
 *
 * @config size-based-splitter
 */
@XStreamAlias("size-based-splitter")
@DisplayOrder(order = {"splitSizeBytes"})
public class SizeBasedSplitter extends MessageSplitterImp {

  // 256k seems like a reasonable amount; it's the max size of SQS messages.
  public static final int DEFAULT_SPLIT_SIZE = 256 * 1024;
  // Is 64k a good max buffer size, or should we make it configurable?
  private static final int MAX_BUF_SIZ = 64 * 1024;

  @InputFieldDefault(value = "" + DEFAULT_SPLIT_SIZE)
  private Integer splitSizeBytes;

  public SizeBasedSplitter() {}

  public com.adaptris.core.util.CloseableIterable<AdaptrisMessage> splitMessage(final AdaptrisMessage msg) throws CoreException {
    logR.debug("SizeBasedSplitter splits every {} bytes", splitSizeBytes());
    logExpected(msg);
    try {
      return new SplitGenerator(msg.getInputStream(), msg, selectFactory(msg));
    } catch (IOException e) {
      throw new CoreException(e);
    }
  }

  private void logExpected(AdaptrisMessage msg) {
    int whole = (int) msg.getSize() / splitSizeBytes();
    int remainder = (int) msg.getSize() % splitSizeBytes();
    int expected = whole + ((remainder > 0) ? 1 : 0);
    logR.trace("Expecting {} split messages", expected);
  }


  private class SplitGenerator extends SplitMessageIterator {
    private final InputStream input;
    private int numberOfMessages;

    public SplitGenerator(InputStream buf, AdaptrisMessage msg, AdaptrisMessageFactory factory) {
      super(msg, factory);
      this.input = buf;
      logR.trace("Using message factory: {}", factory.getClass());
    }

    protected AdaptrisMessage constructAdaptrisMessage() throws IOException {
      AdaptrisMessage newMsg = factory.newMessage();
      final int bufSiz = Math.min(splitSizeBytes(), MAX_BUF_SIZ);
      byte[] buffer = new byte[bufSiz];
      final int splitSize = splitSizeBytes();
      try (OutputStream out = newMsg.getOutputStream()) {
        int bytesLeft = splitSize;
        while (bytesLeft > 0) {
          int bytesToRead = Math.min(bufSiz, bytesLeft);
          int bytesRead = input.read(buffer, 0, bytesToRead);
          if (bytesRead == -1) {
            break;
          }
          bytesLeft -= bytesRead;
          out.write(buffer, 0, bytesRead);
        }
      }
      if (newMsg.getSize() > 0) {
        numberOfMessages++;
        copyMetadata(msg, newMsg);
        return newMsg;
      }
      return null;
    }



    @Override
    public void close() throws IOException {
      logR.trace("Split gave {} messages", numberOfMessages);
      input.close();
    }

  }


  /**
   * @return the splitSizeBytes
   */
  public Integer getSplitSizeBytes() {
    return splitSizeBytes;
  }

  /**
   * @param l the splitSizeBytes to set, default is {@value #DEFAULT_SPLIT_SIZE} if not specified.
   */
  public void setSplitSizeBytes(Integer l) {
    this.splitSizeBytes = l;
  }

  int splitSizeBytes() {
    return NumberUtils.toIntDefaultIfNull(getSplitSizeBytes(), DEFAULT_SPLIT_SIZE);
  }

}
