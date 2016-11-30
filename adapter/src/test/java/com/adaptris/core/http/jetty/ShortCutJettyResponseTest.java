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

import java.util.concurrent.TimeUnit;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.NullConnection;
import com.adaptris.core.PoolingWorkflow;
import com.adaptris.core.PortManager;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.http.HttpProducer;
import com.adaptris.core.http.HttpServiceExample;
import com.adaptris.core.http.JdkHttpProducer;
import com.adaptris.core.http.server.HttpStatusProvider.HttpStatus;
import com.adaptris.core.services.WaitService;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.stubs.StaticMockMessageProducer;
import com.adaptris.util.TimeInterval;

@SuppressWarnings("deprecation")
public class ShortCutJettyResponseTest extends HttpServiceExample {

  public ShortCutJettyResponseTest(String name) {
    super(name);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new ShortCutJettyResponse();
  }

  public void testService() throws Exception {
    HttpConnection connection = createConnection();
    MockMessageProducer mockProducer = new StaticMockMessageProducer();
    mockProducer.getMessages().clear();
    MessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    PoolingWorkflow workflow = new PoolingWorkflow();
    workflow.setShutdownWaitTime(new TimeInterval(1L, TimeUnit.SECONDS));
    ResponseProducer responder = new ResponseProducer(HttpStatus.OK_200);
    workflow.setConsumer(consumer);
    workflow.getServiceCollection().add(new WaitService(new TimeInterval(1L, TimeUnit.SECONDS)));
    workflow.getServiceCollection().add(new StandaloneProducer(mockProducer));
    workflow.getServiceCollection().add(new StandaloneProducer(responder));
    workflow.getServiceCollection().add(new ShortCutJettyResponse());
    // Workflow won't get to this before we get the response.
    workflow.getServiceCollection().add(new WaitService(new TimeInterval(10L, TimeUnit.SECONDS)));
    workflow.addInterceptor(new JettyPoolingWorkflowInterceptor());
    Channel channel = JettyHelper.createChannel(connection, workflow);
    HttpProducer httpProducer = createProducer();
    try {
      start(channel);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(HttpConsumerTest.CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      long start = System.currentTimeMillis();
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getContent());
      long end = System.currentTimeMillis();
      assertTrue((end - start) < TimeUnit.SECONDS.toMillis(10));
    }
    finally {
      stop(httpProducer);
      stop(channel);
    }
  }

  protected HttpConnection createConnection() {
    HttpConnection c = new HttpConnection();
    int port = PortManager.nextUnusedPort(Integer.parseInt(PROPERTIES.getProperty(JETTY_HTTP_PORT)));
    c.setPort(port);
    return c;
  }

  protected HttpProducer createProducer() {
    JdkHttpProducer p = new JdkHttpProducer();
    p.setContentTypeKey("content.type");
    p.setIgnoreServerResponseCode(true);
    p.registerConnection(new NullConnection());
    return p;
  }

  protected ConfiguredProduceDestination createProduceDestination(int port) {
    ConfiguredProduceDestination d = new ConfiguredProduceDestination("http://localhost:" + port + URL_TO_POST_TO);
    return d;
  }
}
