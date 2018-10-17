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
