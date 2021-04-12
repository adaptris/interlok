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

import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.slf4j.MDC;
import com.adaptris.core.GeneralServiceExample;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class ClearLoggingContextTest extends GeneralServiceExample {

  @Override
  protected ClearLoggingContext retrieveObjectForSampleConfig() {
    return new ClearLoggingContext();
  }

  @Test
  public void testRemove() throws Exception {
    ClearLoggingContext srv = new ClearLoggingContext();
    MDC.put("contextKey", "contextValue");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(srv, msg);
    assertNull(MDC.get("contextKey"));
  }

}
