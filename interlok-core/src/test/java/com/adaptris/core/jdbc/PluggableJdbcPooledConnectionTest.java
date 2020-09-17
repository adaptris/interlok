package com.adaptris.core.jdbc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.Test;
import com.adaptris.core.CoreException;
import com.adaptris.core.StartedState;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.TimeInterval;

public class PluggableJdbcPooledConnectionTest extends
    com.adaptris.interlok.junit.scaffolding.DatabaseConnectionCase<PluggableJdbcPooledConnection> {

  private static final GuidGenerator GUID = new GuidGenerator();

  public PluggableJdbcPooledConnectionTest() {
  }

  @Override
  protected PluggableJdbcPooledConnection createConnection() {
    return new PluggableJdbcPooledConnection().withBuilder(new HikariPoolBuilder());
  }

  @Override
  protected PluggableJdbcPooledConnection configure(PluggableJdbcPooledConnection conn1) throws Exception {
    String url = initialiseDatabase();
    conn1.setConnectUrl(url);
    conn1.setDriverImp(DRIVER_IMP);
    conn1.setConnectionAttempts(1);
    conn1.setConnectionRetryInterval(new TimeInterval(10L, TimeUnit.MILLISECONDS.name()));
    KeyValuePairSet poolProps = new KeyValuePairSet();
    poolProps.add(new KeyValuePair("maximumPoolSize", "50"));
    poolProps.add(new KeyValuePair("minimumIdle", "1"));
    conn1.setPoolProperties(poolProps);
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
    // Since the builder != null, then EqualsBuilder isn't going to find them equal...
    assertFalse(con.equals(con2));
  }

  @Test
  public void testConnection_UsesPool() throws Exception {
    String originalThread = Thread.currentThread().getName();
    Thread.currentThread().setName("testConnectionDataSource_Poolsize");

    PluggableJdbcPooledConnection con = configure(createConnection());
    con.setConnectUrl("jdbc:derby:memory:" + GUID.safeUUID() + ";create=true");
    con.setDriverImp("org.apache.derby.jdbc.EmbeddedDriver");
    KeyValuePairSet poolProps = new KeyValuePairSet();
    poolProps.add(new KeyValuePair("maximumPoolSize", "50"));
    poolProps.add(new KeyValuePair("minimumIdle", "1"));
    con.withPoolProperties(poolProps);
    try {
      LifecycleHelper.initAndStart(con);
      Awaitility.await()
      .atMost(Duration.ofSeconds(5))
      .with()
      .pollInterval(Duration.ofMillis(100))
      .until(() ->con.retrieveComponentState().equals(StartedState.getInstance()));

      Connection c1 = con.connect();
      Connection c2 = con.connect();
      // Shouldn't be the same object...
      assertNotSame(c1, c2);
      JdbcUtil.closeQuietly(c1, c2);
    } finally {
      Thread.currentThread().setName(originalThread);
      LifecycleHelper.stopAndClose(con);
    }

  }
}
