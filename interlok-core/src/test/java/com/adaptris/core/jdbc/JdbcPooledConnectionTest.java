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

package com.adaptris.core.jdbc;

import com.adaptris.core.ClosedState;
import com.adaptris.core.CoreException;
import com.adaptris.core.StartedState;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.TimeInterval;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.awaitility.Awaitility;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JdbcPooledConnectionTest
    extends com.adaptris.interlok.junit.scaffolding.DatabaseConnectionCase<JdbcPooledConnection> {

  private static final GuidGenerator GUID = new GuidGenerator();

  public JdbcPooledConnectionTest() {}
  @Override
  protected JdbcPooledConnection createConnection() {
    return new JdbcPooledConnection();
  }


  @Override
  protected JdbcPooledConnection configure(JdbcPooledConnection conn1) throws Exception {
    String url = initialiseDatabase();
    conn1.setConnectUrl(url);
    conn1.setDriverImp(DRIVER_IMP);
    conn1.setConnectionAttempts(1);
    conn1.setConnectionRetryInterval(new TimeInterval(10L, TimeUnit.MILLISECONDS.name()));
    conn1.setAcquireIncrement(5);
    conn1.setMinimumPoolSize(10);
    conn1.setMaximumPoolSize(50);
    return conn1;
  }

  @Test
  public void testBrokenPool() throws Exception {
    JdbcPooledConnectionImpl con = configure(createConnection());
    con.setConnectUrl("jdbc:derby:memory:" + GUID.safeUUID() + ";create=true");
    con.setDriverImp("org.apache.derby.jdbc.EmbeddedDriver");
    con.setPassword("PW:WILL_NOT_DECODE");
    try {
      con.asDataSource();
      fail();
    }
    catch (SQLException expected) {

    }
    try {
      con.setConnectUrl("jdbc:derby:memory:" + GUID.safeUUID() + ";create=true");
      LifecycleHelper.initAndStart(con);
      fail();
    }
    catch (CoreException expected) {

    }
  }

  @Test
  public void testEquals() throws Exception {
    JdbcPooledConnectionImpl con = createConnection();
    String url = "jdbc:derby:memory:" + GUID.safeUUID() + ";create=true";
    con.setConnectUrl(url);
    assertTrue(con.equals(con));
    assertFalse(con.equals(null));
    assertFalse(con.equals(new Object()));
    JdbcPooledConnectionImpl con2 = createConnection();
    con2.setConnectUrl(url);
    assertTrue(con.equals(con2));
  }

  @Test
  public void testPoolSize() throws Exception {
    JdbcPooledConnection con = createConnection();
    assertEquals(JdbcPooledConnection.DEFAULT_MAXIMUM_POOL_SIZE, con.maxPoolSize());
    assertEquals(JdbcPooledConnection.DEFAULT_MINIMUM_POOL_SIZE, con.minPoolSize());
    con.setMaximumPoolSize(10);
    con.setMinimumPoolSize(10);
    assertEquals(Integer.valueOf(10), con.getMaximumPoolSize());
    assertEquals(Integer.valueOf(10), con.getMinimumPoolSize());
    assertEquals(10, con.maxPoolSize());
    assertEquals(10, con.minPoolSize());
  }

  @Test
  public void testClose() throws Exception {
    JdbcPooledConnection con = configure(createConnection());
    con.setConnectUrl("jdbc:derby:memory:" + GUID.safeUUID() + ";create=true");
    con.setDriverImp("org.apache.derby.jdbc.EmbeddedDriver");
    con.setPoolFactory(new DebugPoolFactory());
    assertEquals(ClosedState.getInstance(), con.retrieveComponentState());
    assertNull(con.connectionPool);
    con.close();
    // This should be possible.
    assertNotNull(con.asDataSource());
    assertNotNull(con.connectionPool);
    con.close();
    assertNull(con.connectionPool);
  }

  // INTERLOK-107
  @Test
  public void testConnectionDataSource_Poolsize() throws Exception {
    String originalThread = Thread.currentThread().getName();
    Thread.currentThread().setName("testConnectionDataSource_Poolsize");

    JdbcPooledConnection con = configure(createConnection());
    con.setConnectUrl("jdbc:derby:memory:" + GUID.safeUUID() + ";create=true");
    con.setDriverImp("org.apache.derby.jdbc.EmbeddedDriver");
    con.setMinimumPoolSize(1);
    con.setAcquireIncrement(1);
    con.setMaximumPoolSize(7);
    con.setConnectionAcquireWait(new TimeInterval(30L, TimeUnit.SECONDS));
    try {
      LifecycleHelper.initAndStart(con);
      Awaitility.await()
      .atMost(Duration.ofSeconds(5))
      .with()
      .pollInterval(Duration.ofMillis(100))
      .until(() ->con.retrieveComponentState().equals(StartedState.getInstance()));
      ComboPooledDataSource poolDs = ((C3P0PooledDataSource) con.asDataSource()).wrapped();
      assertEquals(0, poolDs.getNumBusyConnections());
      Connection c1 = poolDs.getConnection();
      log.info("1 get: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections() + ", NumIdleConnections" + poolDs.getNumIdleConnections());
      c1.close();
      log.info("1 close: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections() + ", NumIdleConnections" + poolDs.getNumIdleConnections());
      c1 = poolDs.getConnection();
      log.info("1 get (again): NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections() + ", NumIdleConnections" + poolDs.getNumIdleConnections());
      Connection c2 = poolDs.getConnection();
      log.info("2 get: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections() + ", NumIdleConnections" + poolDs.getNumIdleConnections());
      Connection c3 = poolDs.getConnection();
      log.info("3 get: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections() + ", NumIdleConnections" + poolDs.getNumIdleConnections());
      Connection c4 = poolDs.getConnection();
      log.info("4 get: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections() + ", NumIdleConnections" + poolDs.getNumIdleConnections());
      Connection c5 = poolDs.getConnection();
      log.info("5 get: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections() + ", NumIdleConnections" + poolDs.getNumIdleConnections());
      Connection c6 = poolDs.getConnection();
      log.info("6 get: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections() + ", NumIdleConnections" + poolDs.getNumIdleConnections());

      Connection c7 = poolDs.getConnection();
      log.info("7 get: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections() + ", NumIdleConnections" + poolDs.getNumIdleConnections());

      assertEquals(7, poolDs.getNumBusyConnections());
      JdbcUtil.closeQuietly(c1, c2, c3, c4, c5, c6, c7);
      Awaitility.await()
      .atMost(Duration.ofSeconds(5))
      .with()
      .pollInterval(Duration.ofMillis(100))
      .until(() ->poolDs.getNumBusyConnections() == 0);
      log.info("closed: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections() + ", NumIdleConnections" + poolDs.getNumIdleConnections());

      assertEquals(0, poolDs.getNumBusyConnections());
    }
    finally {
      Thread.currentThread().setName(originalThread);
      LifecycleHelper.stop(con);
      LifecycleHelper.close(con);
    }

  }

}
