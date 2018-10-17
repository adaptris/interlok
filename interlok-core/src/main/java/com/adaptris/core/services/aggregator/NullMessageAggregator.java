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

import java.util.Collection;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.splitter.SplitJoinService;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Special implementation of {@link MessageAggregator} that does not aggregate messages.
 * 
 * <p>
 * Although an edge case; it might be that you are using this as part of a {@link SplitJoinService} and you simply wish to keep the
 * original message and ignore everything else. This aggregator has an empty {@linkplain #joinMessage(AdaptrisMessage, Collection)}
 * method.
 * </p>
 * 
 * @config null-message-aggregator
 * @author lchan
 * @since 3.0.3
 * 
 */
@XStreamAlias("null-message-aggregator")
public class NullMessageAggregator implements MessageAggregator {

  @Override
  public void joinMessage(AdaptrisMessage msg, Collection<AdaptrisMessage> msgs) throws CoreException {
    // Nothing to do.
  }
}
