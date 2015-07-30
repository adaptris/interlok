package com.adaptris.core.management.jmx.provider.junit;

import java.io.IOException;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerProvider;
import javax.management.remote.JMXServiceURL;

//This is used by JmxRemoteComponentTest as a server provider.
// It doesn't actually do anything!
public class ServerProvider implements JMXConnectorServerProvider {

  private static JMXConnectorServer instance;

  @Override
  public JMXConnectorServer newJMXConnectorServer(JMXServiceURL url, Map environment, MBeanServer server) throws IOException {
    if (instance == null) {
      instance = new DummyJmxConnectorServer(url, environment, server);
    }
    return instance;
  }

  public static JMXConnectorServer getConnectorServer() {
    return instance;
  }

  private static class DummyJmxConnectorServer extends JMXConnectorServer {
    private JMXServiceURL address;
    private Map<String, ?> attributes;
    public DummyJmxConnectorServer(JMXServiceURL url, Map environment, MBeanServer server)
        throws IOException {
      super(server);
      address = url;
      attributes = environment;
    }

    /**
     * @return the url
     */
    public JMXServiceURL getAddress() {
      return address;
    }

    /**
     * @return the env
     */
    public Map<String, ?> getAttributes() {
      return attributes;
    }

    /**
     * @param env the env to set
     */
    public void setAttributes(Map<String, ?> env) {
      attributes = env;
    }

    @Override
    public void start() throws IOException {
    }

    @Override
    public void stop() throws IOException {
    }

    @Override
    public boolean isActive() {
      return false;
    }

  }
}
