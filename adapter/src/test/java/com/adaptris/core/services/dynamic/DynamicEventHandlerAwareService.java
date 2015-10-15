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

package com.adaptris.core.services.dynamic;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.EventHandler;
import com.adaptris.core.EventHandlerAware;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("dynamic-event-handler-aware-service")
public class DynamicEventHandlerAwareService extends DynamicService implements EventHandlerAware {

  private static transient EventHandler eventHandler;

  public DynamicEventHandlerAwareService() {
    super();
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
  }

  @Override
  public void registerEventHandler(EventHandler eh) {
    eventHandler = eh;
  }

  // It's a bit lame to have this static,
  // But the class goes out of scope during the tests.
  public static EventHandler registeredEventHandler() {
    return eventHandler;
  }
}
