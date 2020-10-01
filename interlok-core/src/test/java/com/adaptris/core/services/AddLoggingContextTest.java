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

package com.adaptris.core.services;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.slf4j.MDC;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.GeneralServiceExample;

public class AddLoggingContextTest extends GeneralServiceExample {

  @Override
  protected AddLoggingContext retrieveObjectForSampleConfig() {
    return new AddLoggingContext("contextKey", "contextValue");
  }

  @Test
  public void testDefaultLoggingContext() throws Exception {
    AddLoggingContext srv = new AddLoggingContext("contextKey", "contextValue");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(srv, msg);
    assertEquals("contextValue", MDC.get("contextKey"));
  }

  @Test
  public void testLoggingContext_Unique_ID() throws Exception {
    AddLoggingContext srv = new AddLoggingContext("contextKey", "$UNIQUE_ID$");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(srv, msg);
    assertEquals(msg.getUniqueId(), MDC.get("contextKey"));
  }

  @Test
  public void testLoggingContextFromMetadata() throws Exception {
    AddLoggingContext srv = new AddLoggingContext("%message{myContextKey}", "%message{myContextValue}");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("myContextKey", "contextKey");
    msg.addMetadata("myContextValue", "contextValue");
    execute(srv, msg);
    assertEquals("contextValue", MDC.get("contextKey"));
  }
}
