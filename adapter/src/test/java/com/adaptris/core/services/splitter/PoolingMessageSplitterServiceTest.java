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
package com.adaptris.core.services.splitter;

import static com.adaptris.core.ServiceCase.execute;
import static com.adaptris.core.services.splitter.SplitterCase.XML_MESSAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.services.AlwaysFailService;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.stubs.StaticMockMessageProducer;

@SuppressWarnings("deprecation")
public class PoolingMessageSplitterServiceTest {

  @Test
  public void testMaxThreads() throws Exception {
    PoolingMessageSplitterService service = new PoolingMessageSplitterService();
    assertNull(service.getMaxThreads());
    assertEquals(10, service.maxThreads());
    service.setMaxThreads(20);
    assertEquals(20, service.maxThreads());
    service.setMaxThreads(null);
    assertEquals(10, service.maxThreads());
  }

  @Test
  public void testServiceWithXmlSplitter() throws Exception {
    MockMessageProducer producer = createMockProducer();
    PoolingMessageSplitterService service = SplitterCase.createPooling(new XpathMessageSplitter("/envelope/document", "UTF-8"),
        new StandaloneProducer(producer));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    XpathMessageSplitter splitter = new XpathMessageSplitter("/envelope/document", "UTF-8");
    execute(service, msg);
    assertEquals("Number of messages", 3, producer.getMessages().size());
  }

  @Test
  public void testServiceWithFailures() throws Exception {

    PoolingMessageSplitterService service = SplitterCase.createPooling(new XpathMessageSplitter("/envelope/document", "UTF-8"),
        new AlwaysFailService());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    XpathMessageSplitter splitter = new XpathMessageSplitter("/envelope/document", "UTF-8");
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

  protected StaticMockMessageProducer createMockProducer() {
    StaticMockMessageProducer p = new StaticMockMessageProducer();
    p.getMessages().clear();
    return p;
  }
}
