/*
 * Copyright Adaptris Ltd.
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

package com.adaptris.core.services.splitter;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.IOException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.util.stream.StreamUtil;

public abstract class MessageCopier extends MessageSplitterImp {

  protected AdaptrisMessage duplicateWithPayload(AdaptrisMessageFactory factory, AdaptrisMessage msg) throws IOException {
    AdaptrisMessage result = factory.newMessage();
    result.setContentEncoding(msg.getContentEncoding());
    StreamUtil.copyAndClose(msg.getInputStream(), result.getOutputStream());
    copyMetadata(msg, result);
    return result;
  }

  protected static int toInteger(String s) {
    if (isEmpty(s)) {
      return 0;
    }
    return Double.valueOf(s).intValue();
  }

  protected class MessageCopierIterator extends SplitMessageIterator {

    // Should this be a long, Integer.MAX_VALUE is quite large.
    private transient int maxCount;
    private transient int currentCount;
    private transient MessageCallback callback;

    protected MessageCopierIterator(AdaptrisMessage msg, int max, MessageCallback callback) {
      super(msg, selectFactory(msg));
      maxCount = max;
      currentCount = 0;
      this.callback = callback;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    protected AdaptrisMessage constructAdaptrisMessage() throws Exception {
      if (currentCount >= maxCount) return null;
      try {
        AdaptrisMessage splitMsg = duplicateWithPayload(factory, msg);
        splitMsg = callback.handle(splitMsg, currentCount);
        currentCount++;
        return splitMsg;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @FunctionalInterface
  protected interface MessageCallback {
    AdaptrisMessage handle(AdaptrisMessage input, int counter) throws Exception;
  }
}
