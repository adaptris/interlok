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

import static com.adaptris.core.ServiceCase.execute;
import static com.adaptris.core.services.splitter.XpathSplitterTest.ENCODING_UTF8;
import static com.adaptris.core.services.splitter.XpathSplitterTest.ENVELOPE_DOCUMENT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullService;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceCollection;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceList;
import com.adaptris.core.services.aggregator.MessageAggregator;
import com.adaptris.core.services.aggregator.MimeAggregator;
import com.adaptris.core.services.aggregator.XmlDocumentAggregator;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.util.MimeHelper;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.text.mime.MultiPartInput;
import com.adaptris.util.text.xml.InsertNode;
import com.adaptris.util.text.xml.XPath;

@SuppressWarnings("deprecation")
public class SplitJoinServiceTest extends TestCase {

  public static final String XPATH_ENVELOPE = "/envelope";

  public SplitJoinServiceTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

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

  public void testInit() throws Exception {
    SplitJoinService service = new SplitJoinService();
    try {
      service.init();
      fail();
    }
    catch (CoreException expected) {

    }
    service.setSplitter(new LineCountSplitter());
    try {
      service.init();
      fail();
    }
    catch (CoreException expected) {

    }
    service.setAggregator(new MimeAggregator());
    try {
      service.init();
      fail();
    }
    catch (CoreException expected) {

    }
    service.setService(new NullService());
    service.init();
    service.close();
  }

  public void testService_WithException() throws Exception {
    // This is a 100 line message, so we expect to get 11 parts.
    AdaptrisMessage msg = SplitterCase.createLineCountMessageInput();
    SplitJoinService service = new SplitJoinService();
    // The service doesn't actually matter right now.
    service.setService(wrap(new ThrowExceptionService(new ConfiguredException(getName()))));
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

  public void testService_WithUnmarshalException() throws Exception {
    // This is a 100 line message, so we expect to get 11 parts.
    AdaptrisMessage msg = SplitterCase.createLineCountMessageInput();
    SplitJoinService service = new SplitJoinService();
    // The service doesn't actually matter right now.
    // ThrowExceptionService doesn't have a castor-mapping entry so it will fail to unmarshal.
    service.setService(new ThrowExceptionService(new ConfiguredException(getName())));
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

  public void testService_WithMimeJoiner() throws Exception {
    // This is a 100 line message, so we expect to get 11 parts.
    AdaptrisMessage msg = SplitterCase.createLineCountMessageInput();
    SplitJoinService service = new SplitJoinService();
    // The service doesn't actually matter right now.
    service.setService(wrap(new NullService()));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new LineCountSplitter());
    service.setAggregator(new MimeAggregator());
    execute(service, msg);
    MultiPartInput input = MimeHelper.create(msg, false);
    assertEquals(11, input.size());
  }

  public void testService_WithNoSplitMessages() throws Exception {
    // This is a 100 line message, so we expect to get 11 parts.
    AdaptrisMessage msg = SplitterCase.createLineCountMessageInput();
    String originalInput = msg.getContent();
    SplitJoinService service = new SplitJoinService();
    // The service doesn't actually matter right now.
    service.setService(wrap(new NullService()));
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

  public void testService_WithSplitFailure() throws Exception {
    // This is a 100 line message, so we expect to get 11 parts.
    AdaptrisMessage msg = SplitterCase.createLineCountMessageInput();
    SplitJoinService service = new SplitJoinService();
    // The service doesn't actually matter right now.
    service.setService(wrap(new NullService()));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new MessageSplitter() {

      @Override
      public List<AdaptrisMessage> splitMessage(AdaptrisMessage msg) throws CoreException {
        throw new CoreException(getName());
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

  public void testService_WithAggregatorFailure() throws Exception {
    // This is a 100 line message, so we expect to get 11 parts.
    AdaptrisMessage msg = SplitterCase.createLineCountMessageInput();
    SplitJoinService service = new SplitJoinService();
    // The service doesn't actually matter right now.
    service.setService(wrap(new NullService()));
    service.setSplitter(new LineCountSplitter());
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setAggregator(new MessageAggregator() {
      
      @Override
      public void joinMessage(AdaptrisMessage msg, Collection<AdaptrisMessage> msgs) throws CoreException {
        throw new CoreException(getName());
      }
    });
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  public void testService_WithXmlJoiner() throws Exception {
    // This is a XML doc with 3 iterable elements...
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SplitterCase.XML_MESSAGE);
    SplitJoinService service = new SplitJoinService();
    // The service doesn't actually matter right now.
    service.setService(wrap(new NullService()));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new XpathMessageSplitter(ENVELOPE_DOCUMENT, ENCODING_UTF8));
    service.setAggregator(new XmlDocumentAggregator(new InsertNode(XPATH_ENVELOPE)));
    execute(service, msg);

    // Should now be 6 document nodes
    XPath xpath = new XPath();
    assertEquals(6, xpath.selectNodeList(XmlHelper.createDocument(msg), ENVELOPE_DOCUMENT).getLength());
  }

  public static ServiceCollection wrap(Service... services) {
    return new ServiceList(services);
  }
}
