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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.splitter.PooledSplitJoinService;
import com.adaptris.interlok.util.CloseableIterable;

/**
 * Interface for creating a single {@link com.adaptris.core.AdaptrisMessage} instance from multiple
 * Messages.
 *
 * @see PooledSplitJoinService
 */
public interface MessageAggregator {

  /**
   * <p>
   * Joins multiple {@link com.adaptris.core.AdaptrisMessage}s into a single AdaptrisMessage
   * objects. Preservation of metadata is down to the implementation.
   * </p>
   *
   * @param msg the msg to insert all the messages into
   * @param msgs the list of messages to join.
   * @throws CoreException wrapping any other exception
   * @implNote the default operation throws a {@code UnsupportedOperationException}.
   */
  default void joinMessage(AdaptrisMessage msg, Collection<AdaptrisMessage> msgs)
      throws CoreException {
    throw new UnsupportedOperationException(
        "Use aggregate(AdaptrisMessage, Iterable<AdaptrisMessage>) instead");
  }

  /**
   * <p>
   * Joins multiple {@link com.adaptris.core.AdaptrisMessage}s into a single AdaptrisMessage
   * objects. Preservation of metadata is down to the implementation.
   * </p>
   *
   * @param original the original message
   * @param msgs the list of messages to join.
   * @implNote The default implementation turns the Iterable into a collection and invokes
   *           {@link #joinMessage(AdaptrisMessage, Collection)} for backwards compatibility
   *           reasons.
   */
  default void aggregate(AdaptrisMessage original, Iterable<AdaptrisMessage> msgs)
      throws CoreException, IOException {
    joinMessage(original, collect(msgs));
  }


  static Collection<AdaptrisMessage> collect(Iterable<AdaptrisMessage> iter)
      throws IOException, CoreException {
    if (iter instanceof Collection) {
      return (Collection<AdaptrisMessage>) iter;
    }
    List<AdaptrisMessage> result = new ArrayList<AdaptrisMessage>();
    try (CloseableIterable<AdaptrisMessage> messages = CloseableIterable.ensureCloseable(iter)) {
      for (AdaptrisMessage msg : messages) {
        result.add(msg);
      }
    }
    return result;
  }
}
