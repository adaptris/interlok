/*
 * Copyright 2018 Adaptris Ltd.
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

package com.adaptris.core.services;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.GeneralServiceExample;
import com.adaptris.core.services.LoggingServiceImpl.LoggingLevel;

public class LoggingServiceTest extends GeneralServiceExample {


  @Override
  protected LoggingService retrieveObjectForSampleConfig() {
    return new LoggingService(LoggingLevel.DEBUG, "Using Metadata [%message{the-metadata-key}] to as the lookup");
  }

  public void testLogging() throws Exception {
    LoggingService srv = new LoggingService(LoggingLevel.DEBUG, "Metadata key set to [%message{the-metadata-key}]");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("hello");
    msg.addMetadata("the-metadata-key", "hello world");
    assertEquals("Metadata key set to [hello world]", msg.resolve(srv.getText()));
    execute(srv, msg);
  }

}
