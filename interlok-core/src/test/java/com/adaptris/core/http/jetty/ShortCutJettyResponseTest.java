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

import static com.adaptris.core.http.jetty.HttpConsumerTest.JETTY_HTTP_PORT;
import static com.adaptris.core.http.jetty.HttpConsumerTest.URL_TO_POST_TO;
import static com.adaptris.core.http.jetty.HttpConsumerTest.XML_PAYLOAD;
import static com.adaptris.core.http.jetty.JettyHelper.createConnection;
import static org.junit.Assert.assertTrue;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;
import com.adaptris.core.PoolingWorkflow;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.http.HttpServiceExample;
import com.adaptris.core.http.client.net.HttpRequestService;
import com.adaptris.core.http.server.HttpStatusProvider.HttpStatus;
import com.adaptris.core.services.WaitService;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.stubs.StaticMockMessageProducer;
import com.adaptris.util.TimeInterval;

public class ShortCutJettyResponseTest extends HttpServiceExample {
  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new ShortCutJettyResponse();
  }

  @Test
  public void testService() throws Exception {
    HttpConnection connection = createConnection(Integer.parseInt(PROPERTIES.getProperty(JETTY_HTTP_PORT)));
    MockMessageProducer mockProducer = new StaticMockMessageProducer();
    mockProducer.getMessages().clear();
    JettyMessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    PoolingWorkflow workflow = new PoolingWorkflow();
    workflow.setShutdownWaitTime(new TimeInterval(1L, TimeUnit.SECONDS));
    StandardResponseProducer responder = new StandardResponseProducer(HttpStatus.OK_200);
    workflow.setConsumer(consumer);
    workflow.getServiceCollection().add(new WaitService(new TimeInterval(1L, TimeUnit.SECONDS)));
    workflow.getServiceCollection().add(new StandaloneProducer(mockProducer));
    workflow.getServiceCollection().add(new StandaloneProducer(responder));
    workflow.getServiceCollection().add(new ShortCutJettyResponse());
    // Workflow won't get to this before we get the response.
    workflow.getServiceCollection().add(new WaitService(new TimeInterval(10L, TimeUnit.SECONDS)));
    workflow.addInterceptor(new JettyPoolingWorkflowInterceptor());
    Channel channel = JettyHelper.createChannel(connection, workflow);
    HttpRequestService service = JettyHelper.createService(connection.getPort());
    try {
      start(channel);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(HttpConsumerTest.CONTENT_TYPE_METADATA_KEY, "text/xml");
      long start = System.currentTimeMillis();
      ServiceCase.execute(service, msg);;
      long end = System.currentTimeMillis();
      assertTrue(end - start < TimeUnit.SECONDS.toMillis(10));
    }
    finally {
      stop(channel);
    }
  }

}
