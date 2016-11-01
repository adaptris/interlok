package com.adaptris.tester.runtime.clients;

import com.adaptris.tester.runtime.ServiceTestException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;

@XStreamAlias("external-jmx-test-client")
public class ExternalJMXTestClient extends JMXTestClient {

  private String jmxUrl;

  @XStreamOmitField
  private JMXConnector jmxConnector;

  public void setJmxUrl(String jmxUrl) {
    this.jmxUrl = jmxUrl;
  }

  public String getJmxUrl() {
    return jmxUrl;
  }


  @Override
  public MBeanServerConnection initMBeanServerConnection() throws ServiceTestException{
    try {
      jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(getJmxUrl()));
      return jmxConnector.getMBeanServerConnection();
    } catch (IOException e) {
      throw new ServiceTestException(e);
    }
  }

  @Override
  public void close() throws IOException {
    jmxConnector.close();
  }
}
