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

import static com.adaptris.core.ServiceCase.asCollection;
import static com.adaptris.core.ServiceCase.execute;
import static com.adaptris.core.services.splitter.XpathSplitterTest.ENCODING_UTF8;
import static com.adaptris.core.services.splitter.XpathSplitterTest.ENVELOPE_DOCUMENT;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultEventHandler;
import com.adaptris.core.NullService;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.WaitService;
import com.adaptris.core.services.aggregator.MessageAggregator;
import com.adaptris.core.services.aggregator.MimeAggregator;
import com.adaptris.core.services.aggregator.XmlDocumentAggregator;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.MimeHelper;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.text.mime.BodyPartIterator;
import com.adaptris.util.text.xml.InsertNode;
import com.adaptris.util.text.xml.XPath;

@SuppressWarnings("deprecation")
public class SplitJoinServiceTest {

  public static final String XPATH_ENVELOPE = "/envelope";

  protected static final Duration ONE_HUNDRED_MILLISECONDS = Duration.ofMillis(100);
  protected static final Duration FIVE_SECONDS = Duration.ofSeconds(5);

  @Rule
  public TestName testName = new TestName();

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testSetTimeout() throws Exception {
    TimeInterval ten = new TimeInterval(10L, TimeUnit.MINUTES);
    TimeInterval one = new TimeInterval(1L, TimeUnit.MINUTES);
    SplitJoinService service = new SplitJoinService();
    assertNull(service.getTimeout());
    assertEquals(ten.toMilliseconds(), service.timeoutMs());
    service.setTimeout(one);
    assertEquals(one, service.getTimeout());
    assertEquals(one.toMilliseconds(), service.timeoutMs());
    service.setTimeout(null);
    assertNull(service.getTimeout());
    assertEquals(ten.toMilliseconds(), service.timeoutMs());
  }

  @Test
  public void testLifecycle() throws Exception {
    SplitJoinService service = createServiceForTests();
    service.registerEventHandler(null);
    try {
      LifecycleHelper.initAndStart(service);
      fail();
    }
    catch (CoreException expected) {

    }
    try {
      service.setSplitter(new LineCountSplitter());
      LifecycleHelper.initAndStart(service);
      fail();
    } catch (CoreException expected) {

    }
    try {
      service.setSplitter(new LineCountSplitter());
      service.setAggregator(new MimeAggregator());
      service.setService(new NullService());
      LifecycleHelper.initAndStart(service);
      assertNotNull(service.wrappedServices());
      assertEquals(1, service.wrappedServices().length);
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  @Test
  public void testService_WithException() throws Exception {
    // This is a 100 line message, so we expect to get 11 parts.
    AdaptrisMessage msg = SplitterCase.createLineCountMessageInput();
    SplitJoinService service = createServiceForTests();
    // The service doesn't actually matter right now.
    service.setService(asCollection(new ThrowExceptionService(new ConfiguredException(testName.getMethodName()))));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new LineCountSplitter());
    service.setAggregator(new MimeAggregator());
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  @Test
  public void testService_WithMimeJoiner() throws Exception {
    // This is a 100 line message, so we expect to get 11 parts.
    AdaptrisMessage msg = SplitterCase.createLineCountMessageInput();
    SplitJoinService service = createServiceForTests();
    // The service doesn't actually matter right now.
    service.setService(asCollection(new NullService()));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new LineCountSplitter());
    service.setAggregator(new MimeAggregator());
    service.registerEventHandler(LifecycleHelper.initAndStart(new DefaultEventHandler()));

    execute(service, msg);
    BodyPartIterator input = MimeHelper.createBodyPartIterator(msg);
    assertEquals(11, input.size());
  }

  @Test
  public void testService_WithNoSplitMessages() throws Exception {
    // This is a 100 line message, so we expect to get 11 parts.
    AdaptrisMessage msg = SplitterCase.createLineCountMessageInput();
    String originalInput = msg.getContent();
    SplitJoinService service = createServiceForTests();
    // The service doesn't actually matter right now.
    service.setService(asCollection(new NullService()));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new MessageSplitter() {

      @Override
      public List<AdaptrisMessage> splitMessage(AdaptrisMessage msg) throws CoreException {
        return new ArrayList();
      }

    });
    service.setAggregator(new MimeAggregator());
    execute(service, msg);
    assertEquals(originalInput, msg.getContent());
  }

  @Test
  public void testService_WithSplitFailure() throws Exception {
    // This is a 100 line message, so we expect to get 11 parts.
    AdaptrisMessage msg = SplitterCase.createLineCountMessageInput();
    SplitJoinService service = createServiceForTests();
    // The service doesn't actually matter right now.
    service.setService(asCollection(new NullService()));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new MessageSplitter() {

      @Override
      public List<AdaptrisMessage> splitMessage(AdaptrisMessage msg) throws CoreException {
        throw new CoreException(testName.getMethodName());
      }

    });
    service.setAggregator(new MimeAggregator());
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  @Test
  public void testService_WithAggregatorFailure() throws Exception {
    // This is a 100 line message, so we expect to get 11 parts.
    AdaptrisMessage msg = SplitterCase.createLineCountMessageInput();
    SplitJoinService service = createServiceForTests();
    // The service doesn't actually matter right now.
    service.setService(asCollection(new NullService()));
    service.setSplitter(new LineCountSplitter());
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setAggregator(new MessageAggregator() {
      
      @Override
      public void joinMessage(AdaptrisMessage msg, Collection<AdaptrisMessage> msgs) throws CoreException {
        throw new CoreException(testName.getMethodName());
      }
    });
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  @Test
  public void testService_WithXmlJoiner() throws Exception {
    // This is a XML doc with 3 iterable elements...
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SplitterCase.XML_MESSAGE);
    SplitJoinService service = createServiceForTests();
    // The service doesn't actually matter right now.
    service.setService(asCollection(new NullService()));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new XpathMessageSplitter(ENVELOPE_DOCUMENT, ENCODING_UTF8));
    service.setAggregator(new XmlDocumentAggregator(new InsertNode(XPATH_ENVELOPE)));
    service.setSendEvents(true);
    execute(service, msg);

    // Should now be 6 document nodes
    XPath xpath = new XPath();
    assertEquals(6, xpath.selectNodeList(XmlHelper.createDocument(msg), ENVELOPE_DOCUMENT).getLength());
  }


  @Test
  public void testService_Timeout() throws Exception {
    String oldname = Thread.currentThread().getName();
    Thread.currentThread().setName(testName.getMethodName());
    try {
      AdaptrisMessage msg = SplitterCase.createLineCountMessageInput();
      SplitJoinService service = createServiceForTests();
      service.setService(asCollection(new WaitService(new TimeInterval(10L, TimeUnit.SECONDS))));
      service.setTimeout(new TimeInterval(3L, TimeUnit.SECONDS));
      service.setSplitter(new LineCountSplitter());
      service.setAggregator(new MimeAggregator());
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {
      assertEquals(DefaultPoolingFutureExceptionStrategy.EXCEPTION_MSG, expected.getMessage());
    } finally {
      Thread.currentThread().setName(oldname);
    }
  }

  @Test
  public void testSendEvents() throws Exception {
    SplitJoinService service = new SplitJoinService();
    assertNull(service.getSendEvents());
    assertFalse(service.sendEvents());
    service.setSendEvents(Boolean.TRUE);
    assertEquals(Boolean.TRUE, service.getSendEvents());
    assertTrue(service.sendEvents());
  }

  @Test
  public void testService_WithEvents() throws Exception {
    // This is a XML doc with 3 iterable elements...
    AdaptrisMessage msg =
        AdaptrisMessageFactory.getDefaultInstance().newMessage(SplitterCase.XML_MESSAGE);
    SplitJoinService service = createServiceForTests();
    MockMessageProducer eventProducer = new MockMessageProducer();
    service.setSendEvents(true);
    service.registerEventHandler(LifecycleHelper.initAndStart(new DefaultEventHandler(eventProducer)));
    // The service doesn't actually matter right now.
    service.setService(asCollection(new NullService()));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new XpathMessageSplitter(ENVELOPE_DOCUMENT, ENCODING_UTF8));
    service.setAggregator(new XmlDocumentAggregator(new InsertNode(XPATH_ENVELOPE)));
    execute(service, msg);
    await()
        .atMost(FIVE_SECONDS)
        .with()
        .pollInterval(ONE_HUNDRED_MILLISECONDS)
        .until(eventProducer::messageCount, equalTo(3));

    assertTrue(eventProducer.getMessages().size() > 0);
  }

  protected SplitJoinService createServiceForTests() {
    return new SplitJoinService();
  }
}
