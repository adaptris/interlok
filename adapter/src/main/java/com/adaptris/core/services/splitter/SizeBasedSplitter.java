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
import java.util.Iterator;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
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
@DisplayOrder(order = {"split-size-bytes"})
public class SizeBasedSplitter extends MessageSplitterImp {

  // 256k seems like a reasonable amount; it's the max size of SQS messages.
  static final int DEFAULT_SPLIT_SIZE = 256 * 1024;
  // Is 64k a good max buffer size, or should we make it configurable?
  private static final int MAX_BUF_SIZ = 64 * 1024;

  @InputFieldDefault(value = "" + DEFAULT_SPLIT_SIZE)
  private Integer splitSizeBytes;

  public SizeBasedSplitter() {}

  public CloseableIterable<AdaptrisMessage> splitMessage(final AdaptrisMessage msg) throws CoreException {
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


  private class SplitGenerator implements CloseableIterable<AdaptrisMessage>, Iterator<AdaptrisMessage> {
    private final AdaptrisMessage msg;
    private final InputStream input;
    private final AdaptrisMessageFactory factory;

    private AdaptrisMessage nextMessage;
    private int numberOfMessages;

    public SplitGenerator(InputStream buf, AdaptrisMessage msg, AdaptrisMessageFactory factory) {
      this.input = buf;
      this.msg = msg;
      this.factory = factory;
      logR.trace("Using message factory: {}", factory.getClass());
    }

    @Override
    public Iterator<AdaptrisMessage> iterator() {
      // This Iterable can only be Iterated once so multiple iterators are unsupported. That's why
      // it's safe to just return this in this method without constructing a new Iterator.
      return this;
    }

    @Override
    public boolean hasNext() {
      if (nextMessage == null) {
        try {
          nextMessage = constructAdaptrisMessage();
        } catch (IOException e) {
          logR.warn("Could not construct next AdaptrisMessage", e);
          throw new RuntimeException("Could not construct next AdaptrisMessage", e);
        }
      }

      return nextMessage != null;
    }

    @Override
    public AdaptrisMessage next() {
      AdaptrisMessage ret = nextMessage;
      nextMessage = null;
      return ret;
    }

    private AdaptrisMessage constructAdaptrisMessage() throws IOException {
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

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
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
    return getSplitSizeBytes() != null ? getSplitSizeBytes().intValue() : DEFAULT_SPLIT_SIZE;
  }

}
