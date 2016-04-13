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

import java.sql.Connection;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.TimeInterval;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class JdbcPooledConnectionTest extends DatabaseConnectionCase<JdbcPooledConnection> {
  
  private static final GuidGenerator GUID = new GuidGenerator();
  private transient boolean testsEnabled = false;

  public JdbcPooledConnectionTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    if (Boolean.parseBoolean(PROPERTIES.getProperty(StoredProcedureProducerTest.JDBC_STOREDPROC_TESTS_ENABLED, "false"))) {
      super.setUp();
      testsEnabled = true;
    }
  }

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

  // INTERLOK-107
  public void testConnectionDataSource_Poolsize() throws Exception {
    if (!testsEnabled) {
      return;
    }
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
      LifecycleHelper.init(con);
      LifecycleHelper.start(con);
      ComboPooledDataSource poolDs = (ComboPooledDataSource) con.asDataSource();
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
      c1.close();
      c2.close();
      c3.close();
      c4.close();
      c5.close();
      c6.close();
      c7.close();
      Thread.sleep(2000);
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
