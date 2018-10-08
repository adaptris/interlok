package com.adaptris.core.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.CoreException;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.TimeInterval;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class AdvancedJdbcPooledConnectionTest extends DatabaseConnectionCase<AdvancedJdbcPooledConnection> {

  private static final GuidGenerator GUID = new GuidGenerator();
  private transient boolean testsEnabled = false;

  public AdvancedJdbcPooledConnectionTest(String arg0) {
    super(arg0);
  }

  @Override
  protected AdvancedJdbcPooledConnection createConnection() {
    return new AdvancedJdbcPooledConnection();
  }

  protected void setUp() throws Exception {
    if (Boolean.parseBoolean(PROPERTIES.getProperty(StoredProcedureProducerTest.JDBC_STOREDPROC_TESTS_ENABLED, "false"))) {
      super.setUp();
      testsEnabled = true;
    }
  }

  @Override
  protected AdvancedJdbcPooledConnection configure(AdvancedJdbcPooledConnection conn1) throws Exception {
    String url = initialiseDatabase();
    conn1.setConnectUrl(url);
    conn1.setDriverImp(DRIVER_IMP);
    conn1.setConnectionAttempts(1);
    conn1.setConnectionRetryInterval(new TimeInterval(10L, TimeUnit.MILLISECONDS.name()));
    KeyValuePairSet poolProps = new KeyValuePairSet();
    poolProps.add(new KeyValuePair(PooledConnectionProperties.acquireIncrement.name(), "5"));
    poolProps.add(new KeyValuePair(PooledConnectionProperties.minPoolSize.name(), "10"));
    poolProps.add(new KeyValuePair(PooledConnectionProperties.maxPoolSize.name(), "50"));
    conn1.setConnectionPoolProperties(poolProps);
    return conn1;
  }

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

  // INTERLOK-107
  public void testConnectionDataSource_Poolsize() throws Exception {
    String originalThread = Thread.currentThread().getName();
    Thread.currentThread().setName("testConnectionDataSource_Poolsize");

    AdvancedJdbcPooledConnection con = configure(createConnection());
    con.setConnectUrl("jdbc:derby:memory:" + GUID.safeUUID() + ";create=true");
    con.setDriverImp("org.apache.derby.jdbc.EmbeddedDriver");
    KeyValuePairSet poolProps = new KeyValuePairSet();
    poolProps.add(new KeyValuePair(PooledConnectionProperties.acquireIncrement.name(), "5"));
    poolProps.add(new KeyValuePair(PooledConnectionProperties.minPoolSize.name(), "10"));
    poolProps.add(new KeyValuePair(PooledConnectionProperties.maxPoolSize.name(), "50"));
    poolProps.add(new KeyValuePair(PooledConnectionProperties.checkoutTimeout.name(), "30000"));
    con.setConnectionPoolProperties(poolProps);
    try {
      LifecycleHelper.initAndStart(con);
      Thread.sleep(500);
      ComboPooledDataSource poolDs = (ComboPooledDataSource) con.asDataSource();
      assertEquals(0, poolDs.getNumBusyConnections());
      Connection c1 = null;
      try {
        c1 = poolDs.getConnection();
        log.info("1 get: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections()
            + ", NumIdleConnections" + poolDs.getNumIdleConnections());
      } finally {
        Thread.sleep(1500);
        c1.close();
      }
      log.info("1 close: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections()
          + ", NumIdleConnections" + poolDs.getNumIdleConnections());
      c1 = poolDs.getConnection();
      log.info("1 get (again): NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections="
          + poolDs.getNumBusyConnections() + ", NumIdleConnections" + poolDs.getNumIdleConnections());
      Connection c2 = poolDs.getConnection();
      log.info("2 get: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections()
          + ", NumIdleConnections" + poolDs.getNumIdleConnections());
      Connection c3 = poolDs.getConnection();
      log.info("3 get: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections()
          + ", NumIdleConnections" + poolDs.getNumIdleConnections());
      Connection c4 = poolDs.getConnection();
      log.info("4 get: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections()
          + ", NumIdleConnections" + poolDs.getNumIdleConnections());
      Connection c5 = poolDs.getConnection();
      log.info("5 get: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections()
          + ", NumIdleConnections" + poolDs.getNumIdleConnections());
      Connection c6 = poolDs.getConnection();
      log.info("6 get: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections()
          + ", NumIdleConnections" + poolDs.getNumIdleConnections());

      Connection c7 = poolDs.getConnection();
      log.info("7 get: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections()
          + ", NumIdleConnections" + poolDs.getNumIdleConnections());

      assertEquals(7, poolDs.getNumBusyConnections());
      c1.close();
      c2.close();
      c3.close();
      c4.close();
      c5.close();
      c6.close();
      c7.close();
      Thread.sleep(2000);
      log.info("closed: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections()
          + ", NumIdleConnections" + poolDs.getNumIdleConnections());

      assertEquals(0, poolDs.getNumBusyConnections());

    } finally {
      Thread.currentThread().setName(originalThread);
      LifecycleHelper.stop(con);
      LifecycleHelper.close(con);
    }

  }
}
