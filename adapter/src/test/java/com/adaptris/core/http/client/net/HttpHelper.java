package com.adaptris.core.http.client.net;

import static com.adaptris.core.http.jetty.JettyHelper.createChannel;
import static com.adaptris.core.http.jetty.JettyHelper.createConsumer;
import static com.adaptris.core.http.jetty.JettyHelper.createWorkflow;

import com.adaptris.core.Channel;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.PortManager;
import com.adaptris.core.ServiceList;
import com.adaptris.core.http.jetty.HttpConnection;
import com.adaptris.core.http.jetty.MessageConsumer;
import com.adaptris.core.http.server.MetadataHeaderHandler;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.KeyValuePair;

public class HttpHelper {
  public static final String CONTENT_TYPE = "content.type";
  public static final String URL_TO_POST_TO = "/url/to/post/to";


  public static Channel createAndStartChannel() throws Exception {
    return createAndStartChannel(new MockMessageProducer());
  }

  public static Channel createAndStartChannel(MockMessageProducer mock) throws Exception {
    MessageConsumer mc = createConsumer(URL_TO_POST_TO);
    mc.setHeaderHandler(new MetadataHeaderHandler());
    HttpConnection jc = createConnection();
    Channel c = createChannel(jc, createWorkflow(mc, mock, new ServiceList()));
    start(c);
    return c;
  }

  public static HttpConnection createConnection() {
    HttpConnection c = new HttpConnection();
    int port = PortManager.nextUnusedPort(28080);
    c.setPort(port);
    c.getHttpProperties().add(new KeyValuePair(HttpConnection.HttpProperty.MaxIdleTime.name(), "30000"));
    c.setSendServerVersion(true);
    return c;
  }

  public static ConfiguredProduceDestination createProduceDestination(Channel channel) {
    ConfiguredProduceDestination d = new ConfiguredProduceDestination("http://localhost:" + getPort(channel) + URL_TO_POST_TO);
    return d;
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
