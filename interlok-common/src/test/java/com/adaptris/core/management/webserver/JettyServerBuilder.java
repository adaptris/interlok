package com.adaptris.core.management.webserver;
import static org.awaitility.Awaitility.await;
import java.time.Duration;
import org.eclipse.jetty.http.HttpCompliance;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class JettyServerBuilder {
  public static Server build() throws Exception {
    final Server server = createSimpleServer();

    server.addConnector(createConnector(server));

    // Setting up handler collection
    final HandlerCollection handlerCollection = new HandlerCollection();
    final ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
    handlerCollection.addHandler(contextHandlerCollection);
    handlerCollection.addHandler(new DefaultHandler());

    server.setHandler(handlerCollection);
    return server;
  }

  public static void destroy(Server server)  {
    if (server.isStarted()) {
      executeQuietly(() -> server.stop());
      executeQuietly(() -> server.join());
      executeQuietly(() -> server.destroy());
    }
    for (Connector c : server.getConnectors()) {
      PortManager.release(((ServerConnector) c).getPort());
    }
  }
  
  public static void start(Server server) throws Exception {
    server.start();
    waitUntilStarted(server);
  }
  
  private static void waitUntilStarted(Server server) {
    await().atMost(Duration.ofSeconds(5))
      .with()
      .pollInterval(Duration.ofMillis(100))
      .until(() -> server.isStarted());
  }
  
  private static Server createSimpleServer() {
    final Server server = new Server();
    // Setting up extra options
    server.setStopAtShutdown(true);
    server.setStopTimeout(5000);
    server.setDumpAfterStart(false);
    server.setDumpBeforeStop(false);
    return server;
  }

  private static ServerConnector createConnector(Server server) {
    ServerConnector connector = new ServerConnector(server, -1, -1,
        new HttpConnectionFactory(configure(new HttpConfiguration()), HttpCompliance.RFC2616));
    connector.setPort(PortManager.nextUnusedPort(8080));
    return connector;
  }

  private static HttpConfiguration configure(final HttpConfiguration httpConfig) {
    httpConfig.setSecureScheme("http");
    httpConfig.setSecurePort(8443);
    httpConfig.setOutputBufferSize(32768);
    httpConfig.setOutputAggregationSize(8192);
    httpConfig.setRequestHeaderSize(8192);
    httpConfig.setResponseHeaderSize(8192);
    httpConfig.setSendDateHeader(true);
    httpConfig.setSendServerVersion(true);
    httpConfig.setHeaderCacheSize(512);
    httpConfig.setDelayDispatchUntilContent(true);
    httpConfig.setMaxErrorDispatches(10);
    // httpConfig.setBlockingTimeout(-1);
    httpConfig.setMinRequestDataRate(-1);
    httpConfig.setMinResponseDataRate(-1);
    httpConfig.setPersistentConnectionsEnabled(true);
    return httpConfig;
  }
  
  private static void executeQuietly(Runner r) {
    try {
      r.execute();
    } catch (Exception e) {
      
    }
  }
  
  @FunctionalInterface
  public interface Runner {
    void execute() throws Exception;
  }
}
