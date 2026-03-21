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

import org.junit.jupiter.api.Test;

import com.adaptris.core.StartedState;
import com.adaptris.core.stubs.MockConnection;

public class ConnectionMonitorTest {

  @Test
  public void testType() {
    RuntimeInfoComponent parent = new RuntimeInfoComponent() {
    };
    MockConnection connection = new MockConnection("conn-1");
    ConnectionMonitor monitor = new ConnectionMonitor(parent, connection);

    assertEquals(JMX_CONNECTION_TYPE, monitor.getType());
  }

  @Test
  public void testUniqueIdAndSuffix() {
    RuntimeInfoComponent parent = new RuntimeInfoComponent() {
    };
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
  public void testParentAndState() {
    RuntimeInfoComponent parent = new RuntimeInfoComponent() {
    };
    MockConnection connection = new MockConnection("conn-1");
    connection.changeState(StartedState.getInstance());
    ConnectionMonitor monitor = new ConnectionMonitor(parent, connection);

    assertSame(parent, monitor.getParentRuntimeInfoComponent());
    assertSame(StartedState.getInstance(), monitor.getComponentState());
  }
}
