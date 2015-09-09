package com.adaptris.core.jdbc;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class FailoverJdbcConnectionTest extends DatabaseConnectionCase<FailoverJdbcConnection> {


  public FailoverJdbcConnectionTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testBug2082() throws Exception {
    FailoverJdbcConnection conn = configure(createConnection());
    conn.setTestStatement("SELECT 1;");
    try {
      LifecycleHelper.init(conn);
      conn.connect();
      fail("Expected exception");
    }
    catch (Exception expected) {
      ;
    }
  }

  public void testInitialUrlConnectFailure() throws Exception {
    FailoverJdbcConnection conn = configure(createConnection());
    conn.setConnectUrls(Arrays.asList(new String[]
    {
        PROPERTIES.getProperty("jdbc.url.2") + nameGen.create(this), PROPERTIES.getProperty("jdbc.url")
    }));
    LifecycleHelper.init(conn);
    conn.connect();
  }

  public void testAlwaysValidateConnectionFalse() throws Exception {
    FailoverJdbcConnection conn = configure(createConnection());
    conn.setAlwaysValidateConnection(false);
    conn.setConnectUrls(Arrays.asList(new String[]
    {
        PROPERTIES.getProperty("jdbc.url.2") + nameGen.create(this), PROPERTIES.getProperty("jdbc.url")
    }));
    LifecycleHelper.init(conn);
    conn.connect();
  }

  public void testTestStatementEmptyString() throws Exception {
    FailoverJdbcConnection conn = configure(createConnection());
    conn.setAlwaysValidateConnection(true);
    conn.setTestStatement("");
    conn.setConnectUrls(Arrays.asList(new String[]
    {
        PROPERTIES.getProperty("jdbc.url.2") + nameGen.create(this), PROPERTIES.getProperty("jdbc.url")
    }));
    LifecycleHelper.init(conn);
    conn.connect();
  }

  @Override
  protected FailoverJdbcConnection createConnection() {
    return new FailoverJdbcConnection();
  }

  @Override
  protected FailoverJdbcConnection configure(FailoverJdbcConnection conn1) {
    conn1.addConnectUrl(PROPERTIES.getProperty("jdbc.url"));
    conn1.addConnectUrl(PROPERTIES.getProperty("jdbc.url.2"));
    conn1.setDriverImp(PROPERTIES.getProperty("jdbc.driver"));
    conn1.setTestStatement(DEFAULT_TEST_STATEMENT);
    conn1.setDebugMode(true);
    conn1.setConnectionAttempts(1);
    conn1.setConnectionRetryInterval(new TimeInterval(10L, TimeUnit.MILLISECONDS.name()));
    conn1.setAlwaysValidateConnection(true);
    return conn1;
  }

}
