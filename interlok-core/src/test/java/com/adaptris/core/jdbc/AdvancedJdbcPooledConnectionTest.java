package com.adaptris.core.jdbc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.TimeInterval;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class AdvancedJdbcPooledConnectionTest extends DatabaseConnectionCase<AdvancedJdbcPooledConnection> {

  private static final GuidGenerator GUID = new GuidGenerator();

  public AdvancedJdbcPooledConnectionTest() {
  }

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Override
  protected AdvancedJdbcPooledConnection createConnection() {
    return new AdvancedJdbcPooledConnection();
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
      ComboPooledDataSource poolDs = ((C3P0PooledDataSource) con.asDataSource()).wrapped();
      assertEquals(0, poolDs.getNumBusyConnections());
      Connection c0 = null;
      try {
        c0 = poolDs.getConnection();
        slf4jLogger.info("1 get: NumConnections={}, NumBusyConnnections={}, NumIdleConnections={}", poolDs.getNumConnections() ,poolDs.getNumBusyConnections(), poolDs.getNumIdleConnections());
      } finally {
        JdbcUtil.closeQuietly(c0);
      }
      Thread.sleep(1500);
      Connection c1 = poolDs.getConnection();
      slf4jLogger.info("1 get (again): NumConnections={}, NumBusyConnnections={}, NumIdleConnections={}", poolDs.getNumConnections() ,poolDs.getNumBusyConnections(), poolDs.getNumIdleConnections());
      Connection c2 = poolDs.getConnection();
      slf4jLogger.info("2 get: NumConnections={}, NumBusyConnnections={}, NumIdleConnections={}", poolDs.getNumConnections() ,poolDs.getNumBusyConnections(), poolDs.getNumIdleConnections());
      Connection c3 = poolDs.getConnection();
      slf4jLogger.info("3 get: NumConnections={}, NumBusyConnnections={}, NumIdleConnections={}", poolDs.getNumConnections() ,poolDs.getNumBusyConnections(), poolDs.getNumIdleConnections());
      Connection c4 = poolDs.getConnection();
      slf4jLogger.info("4 get: NumConnections={}, NumBusyConnnections={}, NumIdleConnections={}", poolDs.getNumConnections() ,poolDs.getNumBusyConnections(), poolDs.getNumIdleConnections());
      Connection c5 = poolDs.getConnection();
      slf4jLogger.info("5 get: NumConnections={}, NumBusyConnnections={}, NumIdleConnections={}", poolDs.getNumConnections() ,poolDs.getNumBusyConnections(), poolDs.getNumIdleConnections());
      Connection c6 = poolDs.getConnection();
      slf4jLogger.info("6 get: NumConnections={}, NumBusyConnnections={}, NumIdleConnections={}", poolDs.getNumConnections() ,poolDs.getNumBusyConnections(), poolDs.getNumIdleConnections());
      Connection c7 = poolDs.getConnection();
      slf4jLogger.info("7 get: NumConnections={}, NumBusyConnnections={}, NumIdleConnections={}", poolDs.getNumConnections() ,poolDs.getNumBusyConnections(), poolDs.getNumIdleConnections());
      assertTrue(poolDs.getNumBusyConnections() > 0);
      JdbcUtil.closeQuietly(c1,c2,c3,c4,c5,c6,c7);
      Thread.sleep(2000);
      slf4jLogger.info("closed: NumConnections={}, NumBusyConnnections={}, NumIdleConnections={}", poolDs.getNumConnections() ,poolDs.getNumBusyConnections(), poolDs.getNumIdleConnections());
      assertEquals(0, poolDs.getNumBusyConnections());
    } finally {
      Thread.currentThread().setName(originalThread);
      LifecycleHelper.stopAndClose(con);
    }

  }
}
