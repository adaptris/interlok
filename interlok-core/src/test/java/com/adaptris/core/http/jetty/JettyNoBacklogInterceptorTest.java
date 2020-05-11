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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.ExampleWorkflowCase;
import com.adaptris.core.PoolingWorkflow;
import com.adaptris.core.PortManager;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.http.client.net.HttpRequestService;
import com.adaptris.core.services.WaitService;
import com.adaptris.util.TimeInterval;

public class JettyNoBacklogInterceptorTest extends ExampleWorkflowCase {

  static final String DEFAULT_XML_COMMENT = "<!-- This interceptor when combined with a pooling workflow\n"
      + "will return a 503 Server Unavailable HTTP response if the\n" + "request will be blocked by the underlying thread pool.\n"
      + "If there are size of the pool is 10; and there are currently\n"
      + "10 messages being processed by the workflow; then the next\n" + "message will generate a '503 Server Busy' response.\n\n"
      + "As the message has already been effectively submitted\n" + "to the workflow, it is marked with the appropriate\n"
      + "stop-processing flags so that the service-list and\n" + "producer are not executed (the message is effectively\n"
      + "discarded\n" + "\n-->\n";

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testInterceptor() throws Exception {
    int jettyPort = PortManager.nextUnusedPort(Integer.parseInt(PROPERTIES.getProperty(JETTY_HTTP_PORT)));
    HttpConnection connection = createConnection(jettyPort);
    JettyMessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    PoolingWorkflow workflow = new PoolingWorkflow();
    workflow.setPoolSize(1);
    workflow.addInterceptor(new JettyPoolingWorkflowInterceptor());
    workflow.addInterceptor(new JettyNoBacklogInterceptor());
    workflow.setShutdownWaitTime(new TimeInterval(1L, TimeUnit.SECONDS));
    workflow.setConsumer(consumer);
    workflow.getServiceCollection().add(new WaitService(new TimeInterval(3L, TimeUnit.SECONDS)));
    Channel channel = JettyHelper.createChannel(connection, workflow);
    final HttpRequestService httpService = new HttpRequestService();
    httpService.setUrl("http://localhost:" + connection.getPort() + URL_TO_POST_TO);
    httpService.setContentType("text/xml");
    HttpRequestService expect503 = DefaultMarshaller.roundTrip(httpService);
    try {
      start(channel);
      new Thread(runnableWrapper(httpService)).start();
      Awaitility.await()
        .atMost(Duration.ofSeconds(5))
        .with()
        .pollInterval(Duration.ofMillis(100))
        .until(() -> workflow.currentlyActiveObjects() == 1);
      
      // LifecycleHelper.waitQuietly(500);
      // should be waiting in the WaitService.
      ServiceCase.execute(expect503, AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD));
      fail();
    }
    catch (ServiceException expected) {
      assertEquals(ProduceException.class, expected.getCause().getClass());
      assertTrue(expected.getCause().getMessage().contains("503"));
    }
    finally {
      stop(channel);
      PortManager.release(jettyPort);
    }
  }

  @Override
  protected Channel retrieveObjectForSampleConfig() {
    Channel c = new Channel();
    PoolingWorkflow wf = new PoolingWorkflow();
    wf.addInterceptor(new JettyPoolingWorkflowInterceptor());
    wf.addInterceptor(new JettyNoBacklogInterceptor());
    c.setUniqueId(UUID.randomUUID().toString());
    wf.setUniqueId(UUID.randomUUID().toString());
    c.getWorkflowList().add(wf);
    return c;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return "PoolingWorkflow-with-" + JettyNoBacklogInterceptor.class.getSimpleName();
  }

  @Override
  protected PoolingWorkflow createWorkflowForGenericTests() throws CoreException {
    return new PoolingWorkflow();
  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj) + DEFAULT_XML_COMMENT;
  }

  private Runnable runnableWrapper(final HttpRequestService service) {
    return new Runnable() {

      @Override
      public void run() {
        try {
          ServiceCase.execute(service, AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD));
        }
        catch (CoreException e) {
        }
      }

    };
  }
}
