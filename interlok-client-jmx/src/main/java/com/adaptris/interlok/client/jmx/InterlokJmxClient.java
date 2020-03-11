package com.adaptris.interlok.client.jmx;

import static com.adaptris.interlok.client.jmx.ExceptionHelper.rethrowInterlokException;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Map;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.client.InterlokClientImpl;
import com.adaptris.interlok.client.MessageTarget;
import com.adaptris.interlok.management.MessageProcessor;
import com.adaptris.interlok.types.SerializableMessage;

/**
 * Interlok Client that uses JMX to communicate with an Interlok instance.
 * 
 * <p>
 * This implementation allows you to submit messages for processing by an Interlok instance for
 * processing in both a synchronous and asynchronous fashion. In the situation where the
 * {@link MessageTarget} does not resolve to a valid workflow (or multiple workflows) then an
 * exception will be thrown.
 * </p>
 * <p>
 * Usage (some code skipped for brevity) :
 * </p>
 * 
 * <pre>
 * {@code
 *   InterlokJmxClient client = new InterlokJmxClient(new JMXServiceURL("service:jmx:jmxmp://localhost:5555"));
 *   MessageTarget target = ...
 *   SerializableMessage msg = ...
 *   try {
 *     client.connect();
 *     client.processAsync(target, msg);
 *   } finally {
 *     client.disconnect();
 *   }
 * }
 * </pre>
 * 
 * @author lchan
 * 
 */
public class InterlokJmxClient extends InterlokClientImpl {
  private static final String JMX_WORKFLOW_MANAGER_NAME = "com.adaptris:type=Workflow,adapter=%s,channel=%s,id=%s";

  private JMXServiceURL jmxURL = null;
  private Map<String, ?> jmxEnv = null;
  private transient JMXConnector jmxClient = null;

  /**
   * Create a client that accesses JMX via {@link ManagementFactory#getPlatformMBeanServer()}
   */
  public InterlokJmxClient() {

  }

  /**
   * 
   * @param url the JMXServiceURL to connect to.
   */
  public InterlokJmxClient(JMXServiceURL url) {
    this(url, null);
  }

  /**
   * 
   * @param url the JMXServiceURL to connect to.
   * @param env the environment for the connection
   * @see JMXConnectorFactory#newJMXConnector(JMXServiceURL, Map)
   */
  public InterlokJmxClient(JMXServiceURL url, Map<String, ?> env) {
    jmxURL = url;
    jmxEnv = env;
  }

  @Override
  public void connect() throws InterlokException {
    connect(jmxURL, jmxEnv);
  }

  @Override
  public void disconnect() {
    try {
      if (jmxClient != null) {
        jmxClient.close();
      }
    } catch (Exception ignored) {

    }
    jmxClient = null;
  }



  @Override
  public void processAsync(MessageTarget target, SerializableMessage msg) throws InterlokException {
    connect(jmxURL, jmxEnv);
    findWorkflow(target).processAsync(msg);
  }


  @Override
  public SerializableMessage process(MessageTarget target, SerializableMessage msg) throws InterlokException {
    connect(jmxURL, jmxEnv);
    return findWorkflow(target).process(msg);
  }


  private void connect(JMXServiceURL url, Map<String, ?> env) throws InterlokException {
    try {
      if (url != null && jmxClient == null) {
        jmxClient = JMXConnectorFactory.newJMXConnector(url, env);
        jmxClient.connect();
      }
    } catch (Exception e) {
      rethrowInterlokException(e);
    }
  }


  private MessageProcessor findWorkflow(MessageTarget target) throws InterlokException {
    MessageProcessor workflowProxy = null;
    try {
      ObjectName queryName =
          ObjectName.getInstance(String.format(JMX_WORKFLOW_MANAGER_NAME, target.getAdapter(), target.getChannel(),
              target.getWorkflow()));
      MBeanServerConnection mbeanServer = getConnection();
      ArrayList<ObjectName> names = new ArrayList(mbeanServer.queryNames(queryName, null));
      if (names.size() == 0 || names.size() > 1) {
        throw new InterlokException(queryName + " does not narrow to a single workflow");
      }
      workflowProxy = JMX.newMBeanProxy(mbeanServer, names.get(0), MessageProcessor.class);
    } catch (Exception e) {
      rethrowInterlokException(e);

    }
    return workflowProxy;
  }


  private MBeanServerConnection getConnection() throws IOException {
    MBeanServerConnection result = null;
    if (jmxURL == null) {
      result = ManagementFactory.getPlatformMBeanServer();
    } else {
      result = jmxClient.getMBeanServerConnection();
    }
    return result;
  }
}
