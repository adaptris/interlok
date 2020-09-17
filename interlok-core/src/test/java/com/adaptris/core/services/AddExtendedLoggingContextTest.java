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
import java.util.Collections;
import org.junit.Test;
import org.slf4j.MDC;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.GeneralServiceExample;
import com.adaptris.util.KeyValuePairList;

public class AddExtendedLoggingContextTest extends GeneralServiceExample {

  @Override
  protected AddExtendedLoggingContext retrieveObjectForSampleConfig() {
    return new AddExtendedLoggingContext()
        .withValuesToSet(new KeyValuePairList(Collections.singletonMap("contextKey", "contextValue")));
  }

  @Test
  public void testDefaultLoggingContext() throws Exception {
    AddExtendedLoggingContext srv =  new AddExtendedLoggingContext()
        .withValuesToSet(new KeyValuePairList(Collections.singletonMap("contextKey", "contextValue")))
        .withUseDefaultKeys(false);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(srv, msg);
    assertEquals("contextValue", MDC.get("contextKey"));
  }

  @Test
  public void testLoggingContextFromMetadata() throws Exception {
    AddExtendedLoggingContext srv = new AddExtendedLoggingContext()
        .withValuesToSet(new KeyValuePairList(Collections.singletonMap("%message{myContextKey}", "%message{myContextValue}")))
        .withUseDefaultKeys(false);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("myContextKey", "contextKey");
    msg.addMetadata("myContextValue", "contextValue");
    execute(srv, msg);
    assertEquals("contextValue", MDC.get("contextKey"));
  }

  @Test
  public void testUseDefaultKeysChannel() throws Exception {
    AddExtendedLoggingContext srv = new AddExtendedLoggingContext()
        .withUseDefaultKeys(true);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addObjectHeader(CoreConstants.CHANNEL_ID_KEY, "channel");
    execute(srv, msg);
    assertEquals("channel", MDC.get(CoreConstants.CHANNEL_ID_KEY));
  }

  @Test
  public void testUseDefaultKeysWorkflow() throws Exception {
    AddExtendedLoggingContext srv =  new AddExtendedLoggingContext()
        .withUseDefaultKeys(true);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addObjectHeader(CoreConstants.WORKFLOW_ID_KEY, "workflow");
    execute(srv, msg);
    assertEquals("workflow", MDC.get(CoreConstants.WORKFLOW_ID_KEY));
  }

  @Test
  public void testUseDefaultKeysUniqueId() throws Exception {
    AddExtendedLoggingContext srv =  new AddExtendedLoggingContext()
        .withUseDefaultKeys(true);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addObjectHeader(CoreConstants.MESSAGE_UNIQUE_ID_KEY, "unique");
    execute(srv, msg);
    assertEquals("unique", MDC.get(CoreConstants.MESSAGE_UNIQUE_ID_KEY));
  }

  @Test
  public void testUseDefaultKeysReplace() throws Exception {
    AddExtendedLoggingContext srv =  new AddExtendedLoggingContext()
        .withUseDefaultKeys(true)
        .withValuesToSet(new KeyValuePairList(Collections.singletonMap(CoreConstants.MESSAGE_UNIQUE_ID_KEY, "somethingElse")));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addObjectHeader(CoreConstants.MESSAGE_UNIQUE_ID_KEY, "unique");
    execute(srv, msg);
    assertEquals("somethingElse", MDC.get(CoreConstants.MESSAGE_UNIQUE_ID_KEY));
  }
}
