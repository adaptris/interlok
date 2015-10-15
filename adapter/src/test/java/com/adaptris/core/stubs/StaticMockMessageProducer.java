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
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ProduceException;

/**
 * Variation on MockMessageProducer that keeps messages in a static list for PoolingWorkflow tracking purposes.
 *
 * @author lchan
 *
 */
public class StaticMockMessageProducer extends MockMessageProducer {

  private static List<AdaptrisMessage> producedMessages = new ArrayList<AdaptrisMessage>();

  public StaticMockMessageProducer() {
    super();
  }

  @Override
  public void produce(AdaptrisMessage msg) throws ProduceException {
    if (msg == null) {
      throw new ProduceException("msg is null");
    }
    log.trace("Produced [" + msg.getUniqueId() + "]");
    producedMessages.add(msg);
  }

  /**
   * <p>
   * Returns the internal store of produced messages.
   * </p>
   *
   * @return the internal store of produced messages
   */
  @Override
  public List<AdaptrisMessage> getMessages() {
    return producedMessages;
  }

  @Override
  public int messageCount() {
    return producedMessages.size();
  }
}
