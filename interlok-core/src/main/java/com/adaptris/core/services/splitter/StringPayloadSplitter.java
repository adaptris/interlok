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

package com.adaptris.core.services.splitter;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;

/**
 * Partial implementation of MessageSplitter that splits Strings based payloads.
 *
 */
public abstract class StringPayloadSplitter extends MessageSplitterImp {

  /**
   * @see MessageSplitter#splitMessage(AdaptrisMessage)
   *
   */
  @Override
  public List<AdaptrisMessage> splitMessage(AdaptrisMessage msg) throws CoreException {
    List<AdaptrisMessage> result = new ArrayList<AdaptrisMessage>();
    try {
      AdaptrisMessageFactory factory = selectFactory(msg);
      List<String> payloads = split(msg.getContent());
      for (String payload : payloads) {
        AdaptrisMessage splitMsg = factory.newMessage(payload, msg.getContentEncoding());
        copyMetadata(msg, splitMsg);
        result.add(splitMsg);
      }
      logR.trace("Split gave " + result.size() + " messages");
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
    return result;
  }

  /**
   * default split operation.
   *
   * @param messagePayload the string payload derived from the {@link com.adaptris.core.AdaptrisMessage}
   * @return a list of strings that make up the split messages.
   * @throws Exception
   */
  protected abstract List<String> split(String messagePayload) throws Exception;
}
