package com.adaptris.tester.runtime.clients;

import com.adaptris.core.Adapter;
import com.adaptris.core.CoreException;
import com.adaptris.core.runtime.AdapterManager;
import com.adaptris.core.util.JmxHelper;
import com.adaptris.tester.runtime.ServiceTestException;
import com.adaptris.util.GuidGenerator;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import java.io.IOException;

@XStreamAlias("embedded-jmx-test-client")
public class EmbeddedTestClient extends JMXTestClient {

  @XStreamOmitField
  private transient AdapterManager adapterManager;

  @Override
  public MBeanServerConnection initMBeanServerConnection() throws ServiceTestException {
    try {
      Adapter adapter = new Adapter();
      GuidGenerator guidGenerator = new GuidGenerator();
      adapter.setUniqueId(guidGenerator.getUUID());
      adapterManager = new AdapterManager(adapter);
      adapterManager.registerMBean();
      adapterManager.requestStart();
      return JmxHelper.findMBeanServer();
    } catch (CoreException | MalformedObjectNameException e) {
      throw new ServiceTestException(e);
    }
  }


  @Override
  public void close() throws IOException {
    try {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    } catch (CoreException e) {
      throw new IOException(e);
    }
  }
}
