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

import static com.adaptris.core.http.jetty.JettyHelper.createChannel;
import static com.adaptris.core.http.jetty.JettyHelper.createConsumer;
import static com.adaptris.core.http.jetty.JettyHelper.createWorkflow;

import com.adaptris.core.Channel;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.http.jetty.HttpConnection;
import com.adaptris.core.http.jetty.JettyMessageConsumer;
import com.adaptris.core.http.jetty.MetadataHeaderHandler;
import com.adaptris.core.http.jetty.StandardResponseProducer;
import com.adaptris.core.http.server.HttpStatusProvider;
import com.adaptris.core.http.server.HttpStatusProvider.HttpStatus;
import com.adaptris.core.services.metadata.PayloadFromTemplateService;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.junit.scaffolding.util.PortManager;

public class HttpHelper {
  public static final String CONTENT_TYPE = "content.type";
  public static final String URL_TO_POST_TO = "/url/to/post/to";


  public static Channel createAndStartChannel() throws Exception {
    return createAndStartChannel(new MockMessageProducer());
  }

  public static Channel createAndStartChannel(MockMessageProducer mock) throws Exception {
    return createAndStartChannel(mock, HttpStatus.OK_200);
  }

  public static Channel createAndStartChannel(MockMessageProducer mock, HttpStatusProvider.HttpStatus status) throws Exception {
    JettyMessageConsumer mc = createConsumer(URL_TO_POST_TO);
    mc.setHeaderHandler(new MetadataHeaderHandler());
    HttpConnection jc = createConnection();
    Channel c = createChannel(jc, createWorkflow(mc, mock, new ServiceList(new Service[]
    {
        new StandaloneProducer(new StandardResponseProducer(status))
    })));
    start(c);
    return c;
  }

  public static Channel createAndStartChannel(MockMessageProducer mock, String replyPayload) throws Exception {
    return createAndStartChannel(mock, replyPayload, HttpStatus.OK_200);
  }

  public static Channel createAndStartChannel(MockMessageProducer mock, String replyPayload, HttpStatusProvider.HttpStatus status)
      throws Exception {
    JettyMessageConsumer mc = createConsumer(URL_TO_POST_TO);
    mc.setHeaderHandler(new MetadataHeaderHandler());
    HttpConnection jc = createConnection();
    Channel c = createChannel(jc, createWorkflow(mc, mock, new ServiceList(new Service[]
    {
        new PayloadFromTemplateService().withTemplate(replyPayload),
        new StandaloneProducer(new StandardResponseProducer(status))
    })));
    start(c);
    return c;
  }

  public static HttpConnection createConnection() {
    HttpConnection c = new HttpConnection();
    int port = PortManager.nextUnusedPort(28080);
    c.setPort(port);
    return c;
  }

  public static String createProduceDestination(Channel channel) {
    return createURL(channel);
  }


  public static String createURL(Channel channel) {
    return "http://localhost:" + getPort(channel) + URL_TO_POST_TO;
  }

  private static int getPort(Channel c) {
    HttpConnection conn = (HttpConnection) c.getConsumeConnection();
    if (conn == null) {
      throw new RuntimeException();
    }
    return conn.getPort();
  }

  public static void stopChannelAndRelease(Channel c) {
    stop(c);
    PortManager.release(getPort(c));
  }


  private static void start(ComponentLifecycle c) throws CoreException {
    LifecycleHelper.init(c);
    LifecycleHelper.start(c);
  }

  private static void stop(ComponentLifecycle c) {
    LifecycleHelper.stop(c);
    LifecycleHelper.close(c);
  }
}
