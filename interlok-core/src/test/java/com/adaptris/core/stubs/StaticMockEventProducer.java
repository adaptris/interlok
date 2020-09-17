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

package com.adaptris.core.stubs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceException;

/**
 * Variation of {@link MockEventProducer} that keeps the messages produced in a static list.
 */
public class StaticMockEventProducer extends MockEventProducer {

  private static transient List<AdaptrisMessage> producedMessages = new ArrayList<AdaptrisMessage>();

  public StaticMockEventProducer() throws CoreException {
    super();
  }

  public StaticMockEventProducer(Collection<Class> eventsToKeep) throws CoreException {
    super(eventsToKeep);
  }

  /**
   * @see com.adaptris.core.AdaptrisMessageProducer#produce (com.adaptris.core.AdaptrisMessage)
   */
  @Override
  protected void doProduce(AdaptrisMessage msg, String endpoint) throws ProduceException {
    if (msg == null) {
      throw new ProduceException("msg is null");
    }
    if (keepMessage(msg)) {
      producedMessages.add(msg);
    }
  }

  @Override
  public List<AdaptrisMessage> getMessages() {
    return producedMessages;
  }

  @Override
  public int messageCount() {
    return producedMessages.size();
  }

}
