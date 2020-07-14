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

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.Event;
import com.adaptris.core.ProduceException;

/**
 * Mock implementation of <code>AdaptrisMessageProducer</code> for testing events. Allows you to only keep the events that you're
 * interested in.
 */
public class MockEventProducer extends MockMessageProducer {

  private transient AdaptrisMarshaller eventMarshaller;
  private Set<Class> eventsToKeep = new HashSet<Class>();

  public MockEventProducer() {
    eventMarshaller = DefaultMarshaller.getDefaultMarshaller();
  }

  public MockEventProducer(Collection<Class> eventsToKeep) throws CoreException {
    this();
    this.eventsToKeep = new HashSet<Class>(eventsToKeep);
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
      super.doProduce(msg, endpoint);
    }
  }

  protected boolean keepMessage(AdaptrisMessage m) throws ProduceException {
    if (eventsToKeep.size() == 0) return true;
    try (InputStream in = m.getInputStream()) {
      Event event = (Event) eventMarshaller.unmarshal(in);
      if (eventsToKeep.contains(event.getClass())) {
        log.trace(event.getClass() + " matches filter, keeping it");
        return true;
      }
    }
    catch (Exception e) {
      throw new ProduceException(e);
    }
    return false;
  }
}
