package com.adaptris.core.management.jmx;

import java.util.Map;
import java.util.Properties;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServerFactory;

import com.adaptris.core.BaseCase;
import com.adaptris.core.PortManager;
import com.adaptris.core.management.Constants;
import com.adaptris.core.management.SystemPropertiesUtilTest;
import com.adaptris.core.management.jmx.provider.junit.ServerProvider;
import com.adaptris.core.management.properties.PropertyResolver;
import com.adaptris.core.runtime.AdapterComponentMBean;
import com.adaptris.core.util.JmxHelper;

public class JmxRemoteComponentTest extends BaseCase {

  private static final String DEFAULT_VALUE = "Back At The Chicken Shack 1960";
  private static final String JMXMP_PREFIX = "service:jmx:jmxmp://localhost:";

  private Integer unusedPort = -1;

  public JmxRemoteComponentTest(String name) {
    super(name);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    unusedPort = PortManager.nextUnusedPort(5555);

  }

  @Override
  public void tearDown() throws Exception {
    PortManager.release(unusedPort);
    ObjectName jmxObjName = new ObjectName(JmxRemoteComponent.DEFAULT_JMX_OBJECT_NAME);
    MBeanServer mBeanServer = JmxHelper.findMBeanServer();
    if (mBeanServer.isRegistered(jmxObjName)) {
      mBeanServer.unregisterMBean(jmxObjName);
    }
  }

  public void testDefaultObjectName_Lifecycle() throws Exception {
    MBeanServer mBeanServer = JmxHelper.findMBeanServer();
    JmxRemoteComponent jmxr = new JmxRemoteComponent();
    jmxr.init(createProperties());
    ObjectName jmxObjName = new ObjectName(JmxRemoteComponent.DEFAULT_JMX_OBJECT_NAME);
    try {
      assertTrue(mBeanServer.isRegistered(jmxObjName));
      jmxr.start();
      jmxr.stop();
    }
    finally {
      jmxr.destroy();
    }
    assertFalse(mBeanServer.isRegistered(jmxObjName));
  }

  public void testDefaultObjectName_MultipleInstance() throws Exception {
    MBeanServer mBeanServer = JmxHelper.findMBeanServer();
    JmxRemoteComponent jmxr = new JmxRemoteComponent();
    jmxr.init(createProperties());
    JmxRemoteComponent jmx2 = new JmxRemoteComponent();
    try {
      jmx2.init(createProperties());
      fail();
    }
    catch (InstanceAlreadyExistsException expected) {

    }
    finally {
    }
  }

  public void testNoProperties() throws Exception {
    ObjectName jmxObjName = new ObjectName(JmxRemoteComponent.DEFAULT_JMX_OBJECT_NAME);
    MBeanServer mBeanServer = JmxHelper.findMBeanServer();
    JmxRemoteComponent jmxr = new JmxRemoteComponent();
    jmxr.init(new Properties());
    try {
      assertFalse(mBeanServer.isRegistered(jmxObjName));
      jmxr.start();
      jmxr.stop();
    }
    finally {
      jmxr.destroy();
    }
  }

  public void testNonDefaultObjectName_Lifecycle() throws Exception {
    ObjectName jmxObjName = new ObjectName(AdapterComponentMBean.JMX_DOMAIN_NAME + ":type=" + getName());
    MBeanServer mBeanServer = JmxHelper.findMBeanServer();
    JmxRemoteComponent jmxr = new JmxRemoteComponent();
    Properties p = createProperties();
    p.setProperty(JmxRemoteComponent.CFG_KEY_JMX_SERVICE_URL_OBJECT_NAME, jmxObjName.toString());
    try {
      jmxr.init(p);
      assertTrue(mBeanServer.isRegistered(jmxObjName));
      jmxr.start();
      jmxr.stop();
    }
    finally {
      jmxr.destroy();
    }
    assertFalse(mBeanServer.isRegistered(jmxObjName));
  }

  public void testResolveEnvironment() throws Exception {
    MBeanServer mBeanServer = JmxHelper.findMBeanServer();
    ObjectName jmxObjName = new ObjectName(AdapterComponentMBean.JMX_DOMAIN_NAME + ":type=" + getName());
    JmxRemoteComponent jmxr = new JmxRemoteComponent();
    Properties p = new Properties();
    p.put(Constants.CFG_KEY_JMX_SERVICE_URL_KEY, "service:jmx:junit://localhost:9999");
    p.setProperty(JmxRemoteComponent.JMX_SERVICE_URL_ENV_PREFIX + "encodedProperty", SystemPropertiesUtilTest.encode("myPassword"));
    p.setProperty(JmxRemoteComponent.JMX_SERVICE_URL_ENV_PREFIX + "plainProperty", "Blah blah");
    p.setProperty(JmxRemoteComponent.CFG_KEY_JMX_SERVICE_URL_OBJECT_NAME, jmxObjName.toString());

    // Add our dummy provider to the list.
    p.setProperty(JmxRemoteComponent.JMX_SERVICE_URL_ENV_PREFIX + JMXConnectorServerFactory.PROTOCOL_PROVIDER_PACKAGES,
        "com.adaptris.core.management.jmx.provider");
    System.out.append(p.toString());
    jmxr.init(p);
    // Now make sure the password has been decrypted.
    assertNotNull(ServerProvider.getConnectorServer());
    Map<String, ?> attr = ServerProvider.getConnectorServer().getAttributes();
    assertEquals("myPassword", attr.get("encodedProperty"));
    assertEquals("Blah blah", attr.get("plainProperty"));
  }

  public void testNoDecoding() throws Exception {
    PropertyResolver resolver = PropertyResolver.getDefaultInstance();
    resolver.init();
    assertEquals(DEFAULT_VALUE, resolver.resolve(DEFAULT_VALUE));
  }

  private Properties createProperties() {
    Properties result = new Properties();
    result.put(Constants.CFG_KEY_JMX_SERVICE_URL_KEY, JMXMP_PREFIX + unusedPort);
    return result;
  }

}
