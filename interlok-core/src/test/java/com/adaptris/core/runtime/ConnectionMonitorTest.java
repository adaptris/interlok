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

package com.adaptris.core.runtime;

import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_CONNECTION_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.adaptris.core.Adapter;
import com.adaptris.core.Channel;
import com.adaptris.core.CoreException;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.StartedState;
import com.adaptris.core.stubs.MockConnection;
import javax.management.MalformedObjectNameException;
import org.junit.jupiter.api.Test;

class ConnectionMonitorTest {

  @Test
  void testType() throws MalformedObjectNameException, CoreException {
    ParentRuntimeInfoComponent parent = new AdapterManager(createAdapter("adapter-1"));
    MockConnection connection = new MockConnection("conn-1");
    ConnectionMonitor monitor = new ConnectionMonitor(parent, connection);

    assertEquals(JMX_CONNECTION_TYPE, monitor.getType());
  }

  @Test
  void testUniqueIdAndSuffix() throws MalformedObjectNameException, CoreException {
    ParentRuntimeInfoComponent parent =
        new ChannelManager(
            createChannel("channel-1"), new AdapterManager(createAdapter("adapter-1")));
    MockConnection connection = new MockConnection("conn-1");
    ConnectionMonitor monitor = new ConnectionMonitor(parent, connection);

    assertEquals("conn-1", monitor.uniqueId());
    assertEquals("conn-1", monitor.connectionId());
    assertEquals("conn-1", monitor.getUniqueId());

    monitor.appendObjectNameSuffix("-suffix");

    assertEquals("conn-1-suffix", monitor.uniqueId());
    assertEquals("conn-1", monitor.connectionId());
    assertEquals("conn-1", monitor.getUniqueId());
  }

  @Test
  void testParentAndState() throws MalformedObjectNameException, CoreException {
    ParentRuntimeInfoComponent parent =
        new WorkflowManager(
            createWorkflow("workflow-1"),
            new ChannelManager(
                createChannel("channel-1"), new AdapterManager(createAdapter("adapter-1"))));
    MockConnection connection = new MockConnection("conn-1");
    connection.changeState(StartedState.getInstance());
    ConnectionMonitor monitor = new ConnectionMonitor(parent, connection);

    assertSame(parent, monitor.getParentRuntimeInfoComponent());
    assertSame(StartedState.getInstance(), monitor.getComponentState());
  }

  protected Adapter createAdapter(String uid) {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(uid);
    return adapter;
  }

  protected Channel createChannel(String uid) {
    Channel c = new Channel();
    c.setUniqueId(uid);
    return c;
  }

  protected StandardWorkflow createWorkflow(String uid) {
    StandardWorkflow wf = new StandardWorkflow();
    wf.setUniqueId(uid);
    return wf;
  }
}
