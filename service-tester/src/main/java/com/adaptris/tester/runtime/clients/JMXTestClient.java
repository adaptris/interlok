package com.adaptris.tester.runtime.clients;

import com.adaptris.core.CoreException;
import com.adaptris.core.runtime.AdapterComponentChecker;
import com.adaptris.core.runtime.AdapterComponentCheckerMBean;
import com.adaptris.tester.runtime.ServiceTestException;
import com.adaptris.tester.runtime.messages.MessageTranslator;
import com.adaptris.tester.runtime.messages.TestMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.Set;

import static com.adaptris.core.runtime.AdapterComponentCheckerMBean.COMPONENT_CHECKER_TYPE;
import static com.adaptris.core.runtime.AdapterComponentMBean.ADAPTER_PREFIX;
import static com.adaptris.core.runtime.AdapterComponentMBean.ID_PREFIX;

@XStreamAlias("jmx-test-client")
public class JMXTestClient implements TestClient {

  private String jmxUrl;

  @XStreamOmitField
  private JMXConnector jmxConnector;

  @XStreamOmitField
  private AdapterComponentCheckerMBean manager;

  public void setJmxUrl(String jmxUrl) {
    this.jmxUrl = jmxUrl;
  }

  public String getJmxUrl() {
    return jmxUrl;
  }

  @Override
  public void init() throws ServiceTestException {
    try {
      jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(getJmxUrl()));
      MBeanServerConnection mBeanServer = jmxConnector.getMBeanServerConnection();
      manager = JMX.newMBeanProxy(mBeanServer, createComponentCheckerObjectName(mBeanServer), AdapterComponentCheckerMBean.class);
    } catch (MalformedObjectNameException | IOException | InstanceNotFoundException e) {
      throw new ServiceTestException(e);
    }

  }

  @Override
  public void close() throws IOException {
    jmxConnector.close();
  }

  @Override
  public TestMessage applyService(String xml, TestMessage message) throws CoreException {
    MessageTranslator messageTranslator = new MessageTranslator();
    return messageTranslator.translate(manager.applyService(xml, messageTranslator.translate(message)));
  }

  private ObjectName createComponentCheckerObjectName(MBeanServerConnection mBeanServer) throws MalformedObjectNameException, IOException, InstanceNotFoundException {
    return ObjectName.getInstance(COMPONENT_CHECKER_TYPE + ADAPTER_PREFIX + getAdapterName(mBeanServer) + ID_PREFIX
        + AdapterComponentChecker.class.getSimpleName());
  }

  private String getAdapterName(MBeanServerConnection mBeanServer) throws MalformedObjectNameException, IOException, InstanceNotFoundException {
    return getAdapterObject(mBeanServer).getKeyProperty("id");
  }

  private ObjectName getAdapterObject(MBeanServerConnection mBeanServer) throws MalformedObjectNameException, IOException, InstanceNotFoundException {
    String interlokBaseObject = "com.adaptris:type=Adapter,id=*";
    ObjectName patternName = ObjectName.getInstance(interlokBaseObject);
    Set<ObjectInstance> instances = mBeanServer.queryMBeans(patternName, null);

    if (instances.size() == 0)
      throw new InstanceNotFoundException("No configured Adapters");
    else
      return instances.iterator().next().getObjectName();
  }
}
