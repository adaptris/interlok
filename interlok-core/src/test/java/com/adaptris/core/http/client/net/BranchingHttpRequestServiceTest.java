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

package com.adaptris.core.http.client.net;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.BranchingServiceCollection;
import com.adaptris.core.Channel;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.NullService;
import com.adaptris.core.ServiceException;
import com.adaptris.core.http.HttpServiceExample;
import com.adaptris.core.http.client.ExactMatch;
import com.adaptris.core.http.client.RangeMatch;
import com.adaptris.core.http.server.HttpStatusProvider.HttpStatus;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.MockMessageProducer;

public class BranchingHttpRequestServiceTest extends HttpServiceExample {
  private static final String TEXT = "ABCDEFG";

  public BranchingHttpRequestServiceTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testIsBranching() throws Exception {
    BranchingHttpRequestService service = new BranchingHttpRequestService();
    assertTrue(service.isBranching());
    assertNull(service.getDefaultServiceId());
  }

  public void testService_Error() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    Channel c = HttpHelper.createAndStartChannel(mock);
    BranchingHttpRequestService service =
        new BranchingHttpRequestService(HttpHelper.createProduceDestination(c).getDestination())
            .withDefaultServiceId("DefaultServiceId").withContentType("text/complicated");
    AdaptrisMessage msg = new DefectiveMessageFactory().newMessage(TEXT);
    try {
      c.requestStart();
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {
    }
    finally {
      HttpHelper.stopChannelAndRelease(c);
    }
  }

  public void testService_DefaultServiceId() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    Channel c = HttpHelper.createAndStartChannel(mock);
    BranchingHttpRequestService service =
        new BranchingHttpRequestService()
            .withDefaultServiceId("DefaultServiceId")
            .withUrl(HttpHelper.createProduceDestination(c).getDestination())
            .withContentType("text/complicated");

    service.setDefaultServiceId("DefaultServiceId");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
    try {
      c.requestStart();
      execute(service, msg);
      waitForMessages(mock, 1);
    }
    finally {
      HttpHelper.stopChannelAndRelease(c);
    }
    assertEquals(1, mock.messageCount());
    assertEquals("DefaultServiceId", msg.getNextServiceId());
  }

  public void testService_ExactMatch() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    Channel c = HttpHelper.createAndStartChannel(mock, getName());
    BranchingHttpRequestService service =
        new BranchingHttpRequestService(HttpHelper.createProduceDestination(c).getDestination())
            .withDefaultServiceId("DefaultServiceId")
            .withStatusMatches(new ExactMatch(500, "500 Server Error"),
                new ExactMatch(200, "200 OK"))
            .withContentType("text/complicated");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
    try {
      c.requestStart();
      execute(service, msg);
      waitForMessages(mock, 1);
      assertEquals(getName(), msg.getContent());
    }
    finally {
      HttpHelper.stopChannelAndRelease(c);
    }
    assertEquals(1, mock.messageCount());
    assertEquals("200 OK", msg.getNextServiceId());
  }

  public void testService_ExactMatch_WithError() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    Channel c = HttpHelper.createAndStartChannel(mock, "This is the reply body", HttpStatus.INTERNAL_ERROR_500);
    BranchingHttpRequestService service =
        new BranchingHttpRequestService(HttpHelper.createProduceDestination(c).getDestination())
            .withDefaultServiceId("DefaultServiceId")
            .withStatusMatches(new ExactMatch(500, "500 Server Error"),
                new ExactMatch(200, "200 OK"))
            .withContentType("text/complicated");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
    try {
      c.requestStart();
      execute(service, msg);
      waitForMessages(mock, 1);
      assertEquals("This is the reply body", msg.getContent());
    }
    finally {
      HttpHelper.stopChannelAndRelease(c);
    }
    assertEquals(1, mock.messageCount());
    assertEquals("500 Server Error", msg.getNextServiceId());
  }

  public void testService_RangeMatch() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    Channel c = HttpHelper.createAndStartChannel(mock);
    BranchingHttpRequestService service =
        new BranchingHttpRequestService(HttpHelper.createProduceDestination(c).getDestination())
    .withDefaultServiceId("DefaultServiceId")
            .withStatusMatches(new RangeMatch(100, 199, "1XX Informational"),
                new RangeMatch(300, 399, "3XX Moved"), new RangeMatch(200, 299, "2XX OK"))
    .withContentType("text/complicated");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
    try {
      c.requestStart();
      execute(service, msg);
      waitForMessages(mock, 1);
    }
    finally {
      HttpHelper.stopChannelAndRelease(c);
    }
    assertEquals(1, mock.messageCount());
    assertEquals("2XX OK", msg.getNextServiceId());
  }

  @Override
  protected BranchingServiceCollection retrieveObjectForSampleConfig() {
    BranchingHttpRequestService service = createForExamples();
    BranchingServiceCollection sl = new BranchingServiceCollection();
    sl.addService(service);
    sl.setFirstServiceId(service.getUniqueId());
    sl.addService(new ThrowExceptionService("5XX Server Error", new ConfiguredException("Got 5XX error from server")));
    sl.addService(new ThrowExceptionService("4XX Client Error", new ConfiguredException("Got 4XX error from server")));
    sl.addService(new NullService("Not Found"));
    sl.addService(new LogMessageService("2XX OK"));
    sl.addService(new ThrowExceptionService("DefaultServiceId", new ConfiguredException("Unmatched Response")));

    return sl;
  }

  private BranchingHttpRequestService createForExamples() {
    BranchingHttpRequestService service = new BranchingHttpRequestService("http://myhost.com/url/to/get/data/from/or/post/data/to");

    service.setContentType("text/complicated");
    service.setDefaultServiceId("DefaultServiceId");
    service.setUniqueId("GetData");
    service.setMethod("GET");
    service.getStatusMatches().add(new RangeMatch(500, 599, "5XX Server Error"));
    service.getStatusMatches().add(new RangeMatch(200, 299, "2XX OK"));
    service.getStatusMatches().add(new ExactMatch(404, "Not Found"));
    service.getStatusMatches().add(new RangeMatch(400, 499, "4XX Client Error"));
    service.setAuthenticator(HttpRequestServiceTest.buildAuthenticator("username", "password"));

    return service;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return BranchingHttpRequestService.class.getName();
  }

}
