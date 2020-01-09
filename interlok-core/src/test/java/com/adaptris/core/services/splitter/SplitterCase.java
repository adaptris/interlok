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

package com.adaptris.core.services.splitter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.NullConnection;
import com.adaptris.core.NullMessageProducer;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.services.WaitService;
import com.adaptris.core.stubs.StubMessageFactory;
import com.adaptris.core.util.CloseableIterable;

public abstract class SplitterCase extends SplitterServiceExample {

  public static final String XML_MESSAGE = "<?xml version=\"1.0\" "
      + "encoding=\"UTF-8\"?>" + System.lineSeparator() + "<envelope>" + System.lineSeparator() + "<document>one</document>"
      + System.lineSeparator() + "<document>two</document>" + System.lineSeparator() + "<document>three</document>"
      + System.lineSeparator() + "</envelope>";
  public static final String LINE = "The quick brown fox jumps over the lazy dog";


  public static final String XML_WITH_DOCTYPE = "<?xml version=\"1.0\"?>\n" + "<!DOCTYPE document [\n"
      + "<!ENTITY LOCAL_ENTITY 'entity'>\n" + "<!ENTITY % StandardInfo SYSTEM \"../StandardInfo.dtd\">\n" + "%StandardInfo;\n"
      + "]>\n" + "<document>\n" + "</document>\n";

  static BasicMessageSplitterService createBasic(MessageSplitter ms) {
    BasicMessageSplitterService service = new BasicMessageSplitterService();
    service.setConnection(new NullConnection());
    service.setProducer(new NullMessageProducer());
    service.setSplitter(ms);
    return service;
  }

  static AdvancedMessageSplitterService createAdvanced(MessageSplitter ms,
                                                       StandaloneProducer p) {
    return createAdvanced(ms, new Service[]
    {
      p
    });
  }

  static AdvancedMessageSplitterService createAdvanced(MessageSplitter ms,
                                                       Service... services) {
    AdvancedMessageSplitterService service = new AdvancedMessageSplitterService();
    ServiceList sl = new ServiceList(services);
    service.setSplitter(ms);
    service.setService(sl);
    return service;
  }

  static PoolingMessageSplitterService createPooling(MessageSplitter ms, Service... services) {
    PoolingMessageSplitterService service = new PoolingMessageSplitterService();
    ServiceList sl = new ServiceList(services);
    service.setSplitter(ms);
    service.setService(sl);
    service.setMaxThreads(5);
    return service;
  }

  static List<Service> createExamples(MessageSplitter ms) {
    List<Service> services = new ArrayList<Service>();
    services.add(createBasic(ms));
    services.add(createAdvanced(ms, new WaitService(), new StandaloneProducer()));
    services.add(createPooling(ms, new WaitService(), new StandaloneProducer()));
    return services;
  }

  public static AdaptrisMessage createLineCountMessageInput() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintWriter print = new PrintWriter(out);
    for (int i = 0; i < 50; i++) {
      print.println(LINE);
      print.println("");
    }
    print.flush();
    return AdaptrisMessageFactory.getDefaultInstance().newMessage(
        out.toByteArray());
  }
  
  public static AdaptrisMessage createLineCountMessageInputWithHeader(String[] header) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintWriter print = new PrintWriter(out);
    for(String h: header) {
      print.println(h);
    }
    for (int i = 0; i < 50; i++) {
      print.println(LINE);
      print.println("");
    }
    print.flush();
    return AdaptrisMessageFactory.getDefaultInstance().newMessage(
        out.toByteArray());
  }

  @Test
  public void testSetMessageFactory() throws Exception {
    MessageSplitterImp splitter = createSplitterForTests();
    assertNull(splitter.getMessageFactory());
    assertEquals(DefaultMessageFactory.class, splitter.selectFactory(new DefaultMessageFactory().newMessage()).getClass());
    assertEquals(StubMessageFactory.class, splitter.selectFactory(new StubMessageFactory().newMessage()).getClass());

    splitter.setMessageFactory(new StubMessageFactory());
    assertEquals(StubMessageFactory.class, splitter.getMessageFactory().getClass());
    assertEquals(StubMessageFactory.class, splitter.selectFactory(new DefaultMessageFactory().newMessage()).getClass());

    splitter.setMessageFactory(new DefaultMessageFactory());
    assertEquals(DefaultMessageFactory.class, splitter.selectFactory(new StubMessageFactory().newMessage()).getClass());

    splitter.setMessageFactory(null);
    assertEquals(DefaultMessageFactory.class, splitter.selectFactory(new DefaultMessageFactory().newMessage()).getClass());
    assertEquals(StubMessageFactory.class, splitter.selectFactory(new StubMessageFactory().newMessage()).getClass());
  }

  @Test
  public void testSetCopyMetadata() throws Exception {
    MessageSplitterImp splitter = createSplitterForTests();
    assertNull(splitter.getCopyMetadata());
    assertTrue(splitter.copyMetadata());
    splitter.setCopyMetadata(Boolean.FALSE);
    assertNotNull(splitter.getCopyMetadata());
    assertEquals(Boolean.FALSE, splitter.getCopyMetadata());
    assertFalse(splitter.copyMetadata());
    splitter.setCopyMetadata(null);
    assertNull(splitter.getCopyMetadata());
    assertTrue(splitter.copyMetadata());
  }

  @Test
  public void testSetCopyObjectMetadata() throws Exception {
    MessageSplitterImp splitter = createSplitterForTests();
    assertNull(splitter.getCopyObjectMetadata());
    assertFalse(splitter.copyObjectMetadata());
    splitter.setCopyObjectMetadata(Boolean.TRUE);
    assertNotNull(splitter.getCopyObjectMetadata());
    assertEquals(Boolean.TRUE, splitter.getCopyObjectMetadata());
    assertTrue(splitter.copyObjectMetadata());
    splitter.setCopyObjectMetadata(null);
    assertNull(splitter.getCopyObjectMetadata());
    assertFalse(splitter.copyObjectMetadata());
  }

  protected abstract MessageSplitterImp createSplitterForTests();
  
  /**
   * Convert the Iterable into a List. If it's already a list, just return it. If not, 
   * it will be iterated and the resulting list returned.
   */
  protected List<AdaptrisMessage> toList(Iterable<AdaptrisMessage> iter) {
    if(iter instanceof List) {
      return (List<AdaptrisMessage>)iter;
    }
    List<AdaptrisMessage> result = new ArrayList<AdaptrisMessage>();
    try (CloseableIterable<AdaptrisMessage> messages = CloseableIterable.ensureCloseable(iter)) {
      for(AdaptrisMessage msg: messages) {
        result.add(msg);
      }
    } catch (IOException e) {
      log.warn("Could not close Iterable!", e);
    }
    return result;
  }

  protected List splitToList(MessageSplitterImp splitter, AdaptrisMessage msg) throws Exception {
    return toList(splitter.splitMessage(msg));
  }

}
