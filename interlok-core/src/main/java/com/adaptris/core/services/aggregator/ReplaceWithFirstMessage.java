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

package com.adaptris.core.services.aggregator;

import static org.apache.commons.io.IOUtils.copy;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Special implementation of {@link MessageAggregator} that replaces the original payload with the first aggregated message.
 *
 * <p>
 * This is primarily designed to be used where there is a one to one relationship between the original and aggregated message. No
 * parsing is done of the first message, it is simply used as is; all other messages that are passed in as part of the collection to
 * be aggregated are ignored.
 * </p>
 *
 * @config replace-with-first-message-aggregator
 * @author lchan
 *
 */
@JacksonXmlRootElement(localName = "replace-with-first-message-aggregator")
@XStreamAlias("replace-with-first-message-aggregator")
public class ReplaceWithFirstMessage extends MessageAggregatorImpl {

  @Override
  public void joinMessage(AdaptrisMessage msg, Collection<AdaptrisMessage> msgs) throws CoreException {
    aggregate(msg, msgs);
  }

  @Override
  public void aggregate(AdaptrisMessage original, Iterable<AdaptrisMessage> msgs)
      throws CoreException {
    try {
      for (AdaptrisMessage m : msgs) {
        if (filter(m)) {
          try (InputStream in = m.getInputStream(); OutputStream out = original.getOutputStream()) {
            copy(in, out);
          }
          overwriteMetadata(m, original);
          break;
        }
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }
}
