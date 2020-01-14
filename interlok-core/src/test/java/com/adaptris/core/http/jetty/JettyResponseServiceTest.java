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

package com.adaptris.core.http.jetty;

import static com.adaptris.core.http.jetty.JettyHelper.createChannel;
import static com.adaptris.core.http.jetty.JettyHelper.createConsumer;
import static com.adaptris.core.http.jetty.JettyHelper.createWorkflow;
import static com.adaptris.core.http.jetty.StandardResponseProducerTest.CUSTOM_HEADER1;
import static com.adaptris.core.http.jetty.StandardResponseProducerTest.CUSTOM_HEADER2;
import static com.adaptris.core.http.jetty.StandardResponseProducerTest.TEXT;
import static com.adaptris.core.http.jetty.StandardResponseProducerTest.URL_TO_POST_TO;
import static com.adaptris.core.http.jetty.StandardResponseProducerTest.createConnection;
import static com.adaptris.core.http.jetty.StandardResponseProducerTest.createMessage;
import static com.adaptris.core.http.jetty.StandardResponseProducerTest.createRequestor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.Channel;
import com.adaptris.core.PortManager;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneRequestor;
import com.adaptris.core.http.HttpServiceExample;
import com.adaptris.core.stubs.MockMessageProducer;

@SuppressWarnings("deprecation")
public class JettyResponseServiceTest extends HttpServiceExample {

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testDoService() throws Exception {
    JettyResponseService responder = new JettyResponseService(200, "text/plain");
    HttpConnection httpConnection = createConnection();
    ServiceList list = new ServiceList(responder);
    Channel c = createChannel(httpConnection, createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(), list));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      assertEquals(TEXT, msg.getContent());
      assertFalse(msg.containsKey(CUSTOM_HEADER1));
      assertFalse(msg.containsKey(CUSTOM_HEADER2));

    } finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  @Test
  public void testResponseWithError() throws Exception {
    JettyResponseService responder = new JettyResponseService().withHttpStatus("500").withContentType("text/plain")
        .withResponseHeaderProvider(new NoOpResponseHeaderProvider());
    HttpConnection httpConnection = createConnection();
    ServiceList list = new ServiceList(responder);
    Channel c = createChannel(httpConnection, createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(), list));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      fail("StandaloneRequestor.doService() success even though we should have got a 500 error back");
    } catch (ServiceException expected) {
      ;
    } finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }


  @Override
  protected JettyResponseService retrieveObjectForSampleConfig() {
    return new JettyResponseService(200, "text/plain");
  }

}
