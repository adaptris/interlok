package com.adaptris.interlok.client.jmx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

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

  @BeforeEach
  public void setUp() throws Exception {
    jmxPort = PortManager.nextUnusedPort(5555);
    jmxConnectorServer = createConnectorServer(String.valueOf(jmxPort));
    register(ObjectName.getInstance(String.format(JMXMP_SERVER_OBJNAME, String.valueOf(jmxPort))), jmxConnectorServer);
    jmxConnectorServer.start();
  }

  @AfterEach
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
  public void testProcessAsync_LocalJmx(TestInfo info) throws Exception {
    MyMessageProcessor proc = new MyMessageProcessor();
    MessageTarget target =
        new MessageTarget().withAdapter(info.getDisplayName()).withChannel(info.getDisplayName())
            .withWorkflow(info.getDisplayName());
    DefaultSerializableMessage msg = new DefaultSerializableMessage().withPayload(info.getDisplayName());
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
  public void testProcessAsync_SerializableMessage(TestInfo info) throws Exception {
    MyMessageProcessor proc = new MyMessageProcessor();
    MessageTarget target =
        new MessageTarget().withAdapter(info.getDisplayName()).withChannel(info.getDisplayName())
            .withWorkflow(info.getDisplayName());
    DefaultSerializableMessage msg = new DefaultSerializableMessage().withPayload(info.getDisplayName());
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
  public void testProcessAsync_Convenience(TestInfo info) throws Exception {
    MyMessageProcessor proc = new MyMessageProcessor();
    MessageTarget target =
        new MessageTarget().withAdapter(info.getDisplayName()).withChannel(info.getDisplayName())
            .withWorkflow(info.getDisplayName());
    register(createObjectName(target), proc);
    InterlokJmxClient client = new InterlokJmxClient(jmxConnectorServer.getAddress());
    try {
      client.connect();
      client.processAsync(target, info.getDisplayName(), new HashMap<String, String>());
      assertEquals(info.getDisplayName(), proc.getMessage().getContent());
    } finally {
      client.disconnect();
    }
  }


  @Test
  public void testProcess(TestInfo info) throws Exception {
    MyMessageProcessor proc = new MyMessageProcessor();
    MessageTarget target =
        new MessageTarget().withAdapter(info.getDisplayName()).withChannel(info.getDisplayName())
            .withWorkflow(info.getDisplayName());
    DefaultSerializableMessage msg = new DefaultSerializableMessage().withPayload(info.getDisplayName());
    register(createObjectName(target), proc);
    InterlokJmxClient client = new InterlokJmxClient(jmxConnectorServer.getAddress());
    try {
      client.connect();
      SerializableMessage reply = client.process(target, msg);
      assertNotSame(reply.getUniqueId(), proc.getMessage().getUniqueId());
      assertEquals(info.getDisplayName(), reply.getContent());
      assertEquals(info.getDisplayName(), reply.getContent());
    } finally {
      client.disconnect();
    }
  }

  @Test
  public void testMessageTarget_NoWorkflow(TestInfo info) throws Exception {
    MyMessageProcessor proc = new MyMessageProcessor();
    DefaultSerializableMessage msg = new DefaultSerializableMessage().withPayload(info.getDisplayName());
    register(createObjectName(new MessageTarget().withAdapter(info.getDisplayName()).withChannel(info.getDisplayName())
        .withWorkflow(info.getDisplayName())), proc);
    MessageTarget notFound =
        new MessageTarget().withAdapter(info.getDisplayName()).withChannel(info.getDisplayName()).withWorkflow("abcde");
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
  public void testMessageTarget_Wildcard(TestInfo info) throws Exception {
    MyMessageProcessor proc = new MyMessageProcessor();
    MyMessageProcessor proc2 = new MyMessageProcessor();
    MessageTarget target1 =
        new MessageTarget().withAdapter(info.getDisplayName()).withChannel(info.getDisplayName())
            .withWorkflow(info.getDisplayName());
    MessageTarget target2 =
        new MessageTarget().withAdapter(info.getDisplayName()).withChannel(info.getDisplayName())
            .withWorkflow(info.getDisplayName() + "2");
    DefaultSerializableMessage msg = new DefaultSerializableMessage().withPayload(info.getDisplayName());
    register(createObjectName(target1), proc);
    register(createObjectName(target2), proc2);


    MessageTarget wildcard =
        new MessageTarget().withAdapter(info.getDisplayName()).withChannel(info.getDisplayName()).withWorkflow("*");
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
