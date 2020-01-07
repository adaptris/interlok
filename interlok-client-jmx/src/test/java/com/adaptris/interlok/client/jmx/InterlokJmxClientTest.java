package com.adaptris.interlok.client.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.client.MessageTarget;
import com.adaptris.interlok.types.DefaultSerializableMessage;
import com.adaptris.interlok.types.SerializableMessage;

public class InterlokJmxClientTest {
  private static final String JMXMP_LOCALHOST = "service:jmx:jmxmp://localhost:%s";
  private static final String JMX_WORKFLOW_MANAGER_NAME = "com.adaptris:type=Workflow,adapter=%s,channel=%s,id=%s";
  private static final String JMXMP_SERVER_OBJNAME = "com.adaptris:type=JMXMP,id=%s";

  private List<ObjectName> registeredMBeans = new ArrayList<>();
  private JMXConnectorServer jmxConnectorServer;
  private Integer jmxPort;

  @Rule
  public TestName testName = new TestName();

  @Before
  public void setUp() throws Exception {
    jmxPort = PortManager.nextUnusedPort(5555);
    jmxConnectorServer = createConnectorServer(String.valueOf(jmxPort));
    register(ObjectName.getInstance(String.format(JMXMP_SERVER_OBJNAME, String.valueOf(jmxPort))), jmxConnectorServer);
    jmxConnectorServer.start();
  }

  @After
  public void tearDown() throws Exception {
    jmxConnectorServer.stop();
    unregister();
    PortManager.release(jmxPort);
  }

  @Test
  public void testConnectFailure() throws Exception {
    InterlokJmxClient client = new InterlokJmxClient(new JMXServiceURL(String.format(JMXMP_LOCALHOST, "1234")));
    try {
      client.connect();
      fail();
    } catch (InterlokException expected) {

    } finally {
      client.disconnect();
    }
  }

  @Test
  public void testConnectDisconnect() throws Exception {
    InterlokJmxClient client = new InterlokJmxClient(jmxConnectorServer.getAddress());
    try {
      client.connect();
    } finally {
      client.disconnect();
    }
  }

  @Test
  public void testProcessAsync_LocalJmx() throws Exception {
    MyMessageProcessor proc = new MyMessageProcessor();
    MessageTarget target =
        new MessageTarget().withAdapter(testName.getMethodName()).withChannel(testName.getMethodName())
            .withWorkflow(testName.getMethodName());
    DefaultSerializableMessage msg = new DefaultSerializableMessage().withPayload(testName.getMethodName());
    register(createObjectName(target), proc);
    InterlokJmxClient client = new InterlokJmxClient();
    try {
      client.connect();
      client.processAsync(target, msg);
      assertEquals(msg.getUniqueId(), proc.getMessage().getUniqueId());
      assertEquals(msg.getContent(), proc.getMessage().getContent());
    } finally {
      client.disconnect();
    }

  }

  @Test
  public void testProcessAsync_SerializableMessage() throws Exception {
    MyMessageProcessor proc = new MyMessageProcessor();
    MessageTarget target =
        new MessageTarget().withAdapter(testName.getMethodName()).withChannel(testName.getMethodName())
            .withWorkflow(testName.getMethodName());
    DefaultSerializableMessage msg = new DefaultSerializableMessage().withPayload(testName.getMethodName());
    register(createObjectName(target), proc);
    InterlokJmxClient client = new InterlokJmxClient(jmxConnectorServer.getAddress());
    try {
      client.connect();
      client.processAsync(target, msg);
      assertEquals(msg.getUniqueId(), proc.getMessage().getUniqueId());
      assertEquals(msg.getContent(), proc.getMessage().getContent());
    } finally {
      client.disconnect();
    }
  }

  @Test
  public void testProcessAsync_Convenience() throws Exception {
    MyMessageProcessor proc = new MyMessageProcessor();
    MessageTarget target =
        new MessageTarget().withAdapter(testName.getMethodName()).withChannel(testName.getMethodName())
            .withWorkflow(testName.getMethodName());
    register(createObjectName(target), proc);
    InterlokJmxClient client = new InterlokJmxClient(jmxConnectorServer.getAddress());
    try {
      client.connect();
      client.processAsync(target, testName.getMethodName(), new HashMap<String, String>());
      assertEquals(testName.getMethodName(), proc.getMessage().getContent());
    } finally {
      client.disconnect();
    }
  }


  @Test
  public void testProcess() throws Exception {
    MyMessageProcessor proc = new MyMessageProcessor();
    MessageTarget target =
        new MessageTarget().withAdapter(testName.getMethodName()).withChannel(testName.getMethodName())
            .withWorkflow(testName.getMethodName());
    DefaultSerializableMessage msg = new DefaultSerializableMessage().withPayload(testName.getMethodName());
    register(createObjectName(target), proc);
    InterlokJmxClient client = new InterlokJmxClient(jmxConnectorServer.getAddress());
    try {
      client.connect();
      SerializableMessage reply = client.process(target, msg);
      assertNotSame(reply.getUniqueId(), proc.getMessage().getUniqueId());
      assertEquals(testName.getMethodName(), reply.getContent());
      assertEquals(testName.getMethodName(), reply.getContent());
    } finally {
      client.disconnect();
    }
  }

  @Test
  public void testMessageTarget_NoWorkflow() throws Exception {
    MyMessageProcessor proc = new MyMessageProcessor();
    DefaultSerializableMessage msg = new DefaultSerializableMessage().withPayload(testName.getMethodName());
    register(createObjectName(new MessageTarget().withAdapter(testName.getMethodName()).withChannel(testName.getMethodName())
        .withWorkflow(testName.getMethodName())), proc);
    MessageTarget notFound =
        new MessageTarget().withAdapter(testName.getMethodName()).withChannel(testName.getMethodName()).withWorkflow("abcde");
    InterlokJmxClient client = new InterlokJmxClient(jmxConnectorServer.getAddress());
    try {
      client.connect();
      client.processAsync(notFound, msg);
      fail();
    } catch (InterlokException expected) {
      assertTrue(expected.getMessage().endsWith(" does not narrow to a single workflow"));
    } finally {
      client.disconnect();
    }
  }

  @Test
  public void testMessageTarget_Wildcard() throws Exception {
    MyMessageProcessor proc = new MyMessageProcessor();
    MyMessageProcessor proc2 = new MyMessageProcessor();
    MessageTarget target1 =
        new MessageTarget().withAdapter(testName.getMethodName()).withChannel(testName.getMethodName())
            .withWorkflow(testName.getMethodName());
    MessageTarget target2 =
        new MessageTarget().withAdapter(testName.getMethodName()).withChannel(testName.getMethodName())
            .withWorkflow(testName.getMethodName() + "2");
    DefaultSerializableMessage msg = new DefaultSerializableMessage().withPayload(testName.getMethodName());
    register(createObjectName(target1), proc);
    register(createObjectName(target2), proc2);


    MessageTarget wildcard =
        new MessageTarget().withAdapter(testName.getMethodName()).withChannel(testName.getMethodName()).withWorkflow("*");
    InterlokJmxClient client = new InterlokJmxClient(jmxConnectorServer.getAddress());
    try {
      client.connect();
      client.processAsync(wildcard, msg);
      fail();
    } catch (InterlokException expected) {
      assertTrue(expected.getMessage().endsWith(" does not narrow to a single workflow"));
    } finally {
      client.disconnect();
    }
  }

  private void register(ObjectName name, Object obj) throws Exception {
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    mbs.registerMBean(obj, name);
    registeredMBeans.add(name);
  }

  private void unregister() throws Exception {
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    for (ObjectName b : registeredMBeans) {
      try {
        mbs.unregisterMBean(b);
      } catch (Exception ignore) {

      }
    }
    registeredMBeans.clear();
  }

  private static ObjectName createObjectName(MessageTarget target) throws Exception {
    ObjectName objectName =
        ObjectName.getInstance(String.format(JMX_WORKFLOW_MANAGER_NAME, target.getAdapter(), target.getChannel(),
            target.getWorkflow()));
    return objectName;
  }

  private static JMXConnectorServer createConnectorServer(String port) throws Exception {
    JMXConnectorServer jmx =
        JMXConnectorServerFactory.newJMXConnectorServer(new JMXServiceURL(String.format(JMXMP_LOCALHOST, port)), null, null);
    return jmx;
  }
}
