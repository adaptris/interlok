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

package com.adaptris.core.lifecycle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;
import com.adaptris.core.Adapter;
import com.adaptris.core.BaseCase;
import com.adaptris.core.ClosedState;
import com.adaptris.core.CoreException;
import com.adaptris.core.InitialisedState;
import com.adaptris.core.StartedState;
import com.adaptris.core.stubs.MockConnection;

public class FilteredSharedComponentStartTest extends BaseCase {
  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testConstructors() throws Exception {
    FilteredSharedComponentStart starter = new FilteredSharedComponentStart();
    assertFalse(starter.threadedStart());
    assertNull(starter.getThreadedStart());
    assertNotNull(starter.getIncludes());
    assertEquals(0, starter.getIncludes().size());
    assertNotNull(starter.getExcludes());
    assertEquals(0, starter.getExcludes().size());

    starter = new FilteredSharedComponentStart(true);
    assertTrue(starter.threadedStart());
    assertNotNull(starter.getThreadedStart());
    assertNotNull(starter.getIncludes());
    assertEquals(0, starter.getIncludes().size());
    assertNotNull(starter.getExcludes());
    assertEquals(0, starter.getExcludes().size());

  }

  @Test
  public void testSetThreaded() throws Exception {
    FilteredSharedComponentStart starter = new FilteredSharedComponentStart();
    assertFalse(starter.threadedStart());
    assertNull(starter.getThreadedStart());

    starter.setThreadedStart(true);
    assertTrue(starter.threadedStart());
    assertEquals(Boolean.TRUE, starter.getThreadedStart());

    starter.setThreadedStart(null);
    assertFalse(starter.threadedStart());
    assertNull(starter.getThreadedStart());

  }

  @Test
  public void testSetIncludes() throws Exception {
    FilteredSharedComponentStart starter = new FilteredSharedComponentStart();
    assertNotNull(starter.getIncludes());
    assertEquals(0, starter.getIncludes().size());

    starter.setIncludes(new ArrayList(Arrays.asList("abcde")));
    assertNotNull(starter.getIncludes());
    assertEquals(1, starter.getIncludes().size());
    assertEquals("abcde", starter.getIncludes().get(0));

    starter.addInclude("xyz");
    assertEquals(2, starter.getIncludes().size());
    assertEquals("xyz", starter.getIncludes().get(1));

    try {
      starter.setIncludes(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(2, starter.getIncludes().size());
    assertEquals("abcde", starter.getIncludes().get(0));
    assertEquals("xyz", starter.getIncludes().get(1));
  }

  @Test
  public void testSetExcludes() throws Exception {
    FilteredSharedComponentStart starter = new FilteredSharedComponentStart();
    assertNotNull(starter.getExcludes());
    assertEquals(0, starter.getExcludes().size());

    starter.setExcludes(new ArrayList(Arrays.asList("abcde")));
    assertNotNull(starter.getExcludes());
    assertEquals(1, starter.getExcludes().size());
    assertEquals("abcde", starter.getExcludes().get(0));

    starter.addExclude("xyz");
    assertEquals(2, starter.getExcludes().size());
    assertEquals("xyz", starter.getExcludes().get(1));

    try {
      starter.setExcludes(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(2, starter.getExcludes().size());
    assertEquals("abcde", starter.getExcludes().get(0));
    assertEquals("xyz", starter.getExcludes().get(1));
  }

  @Test
  public void testFilteredStart_Includes() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    MockConnection sc1 = new MockConnection(getName() + "_1");
    MockConnection sc2 = new MockConnection(getName() + "_2");
    MockConnection sc3 = new MockConnection(getClass().getSimpleName() + "_1");
    MockConnection sc4 = new MockConnection(getClass().getSimpleName() + "_2");
    MockConnection sc5 = new MockConnection(getClass().getSimpleName() + "_3");

    try {
      adapter.getSharedComponents().addConnection(sc1);
      adapter.getSharedComponents().addConnection(sc2);
      adapter.getSharedComponents().addConnection(sc3);
      adapter.getSharedComponents().addConnection(sc4);
      adapter.getSharedComponents().addConnection(sc5);
      assertEquals(5, adapter.getSharedComponents().getConnections().size());

      FilteredSharedComponentStart starter = new FilteredSharedComponentStart();
      starter.addInclude(".*" + getName() + ".*");
      adapter.getSharedComponents().setLifecycleStrategy(starter);
      adapter.requestInit();
      assertEquals(InitialisedState.getInstance(), adapter.retrieveComponentState());
      assertEquals(InitialisedState.getInstance(), sc1.retrieveComponentState());
      assertEquals(InitialisedState.getInstance(), sc2.retrieveComponentState());
      assertEquals(ClosedState.getInstance(), sc3.retrieveComponentState());
      assertEquals(ClosedState.getInstance(), sc4.retrieveComponentState());
      assertEquals(ClosedState.getInstance(), sc5.retrieveComponentState());
      adapter.requestStart();
      assertEquals(StartedState.getInstance(), adapter.retrieveComponentState());
      assertEquals(StartedState.getInstance(), sc1.retrieveComponentState());
      assertEquals(StartedState.getInstance(), sc2.retrieveComponentState());
      assertEquals(ClosedState.getInstance(), sc3.retrieveComponentState());
      assertEquals(ClosedState.getInstance(), sc4.retrieveComponentState());
      assertEquals(ClosedState.getInstance(), sc5.retrieveComponentState());
    }
    finally {
      stop(adapter);
    }
  }

  @Test
  public void testFilteredStart_Excludes() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    MockConnection sc1 = new MockConnection(getName() + "_1");
    MockConnection sc2 = new MockConnection(getName() + "_2");
    MockConnection sc3 = new MockConnection(getClass().getSimpleName() + "_1");
    MockConnection sc4 = new MockConnection(getClass().getSimpleName() + "_2");
    MockConnection sc5 = new MockConnection(getClass().getSimpleName() + "_3");

    try {
      adapter.getSharedComponents().addConnection(sc1);
      adapter.getSharedComponents().addConnection(sc2);
      adapter.getSharedComponents().addConnection(sc3);
      adapter.getSharedComponents().addConnection(sc4);
      adapter.getSharedComponents().addConnection(sc5);
      assertEquals(5, adapter.getSharedComponents().getConnections().size());

      FilteredSharedComponentStart starter = new FilteredSharedComponentStart();
      starter.addExclude(".*" + getClass().getSimpleName() + ".*");
      adapter.getSharedComponents().setLifecycleStrategy(starter);
      adapter.requestInit();
      assertEquals(InitialisedState.getInstance(), adapter.retrieveComponentState());
      assertEquals(InitialisedState.getInstance(), sc1.retrieveComponentState());
      assertEquals(InitialisedState.getInstance(), sc2.retrieveComponentState());
      assertEquals(ClosedState.getInstance(), sc3.retrieveComponentState());
      assertEquals(ClosedState.getInstance(), sc4.retrieveComponentState());
      assertEquals(ClosedState.getInstance(), sc5.retrieveComponentState());
      adapter.requestStart();
      assertEquals(StartedState.getInstance(), adapter.retrieveComponentState());
      assertEquals(StartedState.getInstance(), sc1.retrieveComponentState());
      assertEquals(StartedState.getInstance(), sc2.retrieveComponentState());
      assertEquals(ClosedState.getInstance(), sc3.retrieveComponentState());
      assertEquals(ClosedState.getInstance(), sc4.retrieveComponentState());
      assertEquals(ClosedState.getInstance(), sc5.retrieveComponentState());
    }
    finally {
      stop(adapter);
    }
  }

  @Test
  public void testNonBlockingStart() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    TriggeredMockConnection sharedConnection = new TriggeredMockConnection(getName());
    try {
      adapter.getSharedComponents().addConnection(sharedConnection);
      adapter.getSharedComponents().setLifecycleStrategy(new FilteredSharedComponentStart(true));
      adapter.requestInit();
      // Will return straight away.
      assertEquals(InitialisedState.getInstance(), adapter.retrieveComponentState());
      assertNotSame(InitialisedState.getInstance(), sharedConnection.retrieveComponentState());
      sharedConnection.wakeup = true;
      waitFor(sharedConnection, InitialisedState.getInstance());
      assertEquals(InitialisedState.getInstance(), sharedConnection.retrieveComponentState());
      sharedConnection.wakeup = false;

      adapter.requestStart();
      assertEquals(StartedState.getInstance(), adapter.retrieveComponentState());
      assertNotSame(StartedState.getInstance(), sharedConnection.retrieveComponentState());
      sharedConnection.wakeup = true;
      waitFor(sharedConnection, StartedState.getInstance());
      assertEquals(StartedState.getInstance(), sharedConnection.retrieveComponentState());

      stop(adapter);

      // Initialise again, to make sure that we can.
      sharedConnection.wakeup = false;
      adapter.requestInit();
      // Will return straight away.
      assertEquals(InitialisedState.getInstance(), adapter.retrieveComponentState());
      assertNotSame(InitialisedState.getInstance(), sharedConnection.retrieveComponentState());
      sharedConnection.wakeup = true;
      waitFor(sharedConnection, InitialisedState.getInstance());
      assertEquals(InitialisedState.getInstance(), sharedConnection.retrieveComponentState());
    }
    finally {
      stop(adapter);
    }
  }

  @Test
  public void testNonBlockingStart_NoSharedConnection() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    try {
      adapter.getSharedComponents().setLifecycleStrategy(new FilteredSharedComponentStart(true));
      adapter.requestInit();
      assertEquals(InitialisedState.getInstance(), adapter.retrieveComponentState());
    }
    finally {
      stop(adapter);
    }
  }

  @Test
  public void testNonBlockingStart_WithException() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    TriggeredFailingMockConnection sharedConnection = new TriggeredFailingMockConnection(getName());
    try {
      adapter.getSharedComponents().addConnection(sharedConnection);
      adapter.getSharedComponents().setLifecycleStrategy(new FilteredSharedComponentStart(true));
      adapter.requestInit();
      // Will return straight away.
      assertEquals(InitialisedState.getInstance(), adapter.retrieveComponentState());
      assertNotSame(InitialisedState.getInstance(), sharedConnection.retrieveComponentState());
      sharedConnection.wakeup = true;
      Thread.sleep(1000);
      // The connection should never start.
      assertEquals(ClosedState.getInstance(), sharedConnection.retrieveComponentState());
    }
    finally {
      stop(adapter);
    }
  }

  class TriggeredMockConnection extends MockConnection {
    transient long sleepInterval = 100L;
    transient boolean wakeup = false;

    public TriggeredMockConnection(String uniqueId) {
      super(uniqueId);
    }

    public TriggeredMockConnection(String uniqueId, long sleepTime) {
      super(uniqueId, sleepTime);
    }

    @Override
    protected void initConnection() throws CoreException {
      super.initConnection();
      sleepItOff();
    }

    @Override
    protected void startConnection() throws CoreException {
      super.startConnection();
      sleepItOff();
    }

    @Override
    protected void stopConnection() {
      super.stopConnection();
      sleepItOff();
    }

    @Override
    protected void closeConnection() {
      super.closeConnection();
      sleepItOff();
    }

    protected void sleepItOff() {
      while (!wakeup) {
        try {
          Thread.sleep(sleepInterval);
        }
        catch (InterruptedException e) {

        }
      }
    }
  }

  class TriggeredFailingMockConnection extends TriggeredMockConnection {

    public TriggeredFailingMockConnection(String uniqueId) {
      super(uniqueId);
    }

    public TriggeredFailingMockConnection(String uniqueId, long sleepTime) {
      super(uniqueId, sleepTime);
    }

    @Override
    protected void sleepItOff() {
      super.sleepItOff();
      throw new RuntimeException();
    }
  }
}
