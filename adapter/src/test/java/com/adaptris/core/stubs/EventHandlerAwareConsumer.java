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

import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.EventHandler;
import com.adaptris.core.EventHandlerAware;

/**
 * <p>
 * Mock implementation of <code>AdaptrisMessageConsumer</code> which allows
 * e.g. test cases to create and submit messages to the registered
 * <code>AdaptrisMessageListener</code>.
 * </p>
 */
public class EventHandlerAwareConsumer extends MockMessageConsumer implements EventHandlerAware {

  private EventHandler evtHandler;

  public EventHandlerAwareConsumer() {
    super();
  }

  public EventHandlerAwareConsumer(ConsumeDestination d, AdaptrisMessageListener m) {
    super(d, m);
  }

  public EventHandlerAwareConsumer(ConsumeDestination d) {
    super(d);
  }

  public EventHandlerAwareConsumer(AdaptrisMessageListener aml) {
    super(aml);
  }

  @Override
  public void registerEventHandler(EventHandler eh) {
    evtHandler = eh;
  }

  public EventHandler retrieveEventHandler() {
    return evtHandler;
  }

}
