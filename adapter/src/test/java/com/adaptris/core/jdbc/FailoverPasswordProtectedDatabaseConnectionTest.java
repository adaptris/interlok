package com.adaptris.core.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.CoreException;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class FailoverPasswordProtectedDatabaseConnectionTest extends PasswordProtectedDatabaseConnectionTest {

  public FailoverPasswordProtectedDatabaseConnectionTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }


  @Override
  protected DatabaseConnection createConnection() {
    FailoverJdbcConnection conn1 = new FailoverJdbcConnection();
    conn1.addConnectUrl(PROPERTIES.getProperty(KEY_JDBC_URL));
    conn1.addConnectUrl(PROPERTIES.getProperty(KEY_JDBC_URL));
    conn1.setDriverImp(PROPERTIES.getProperty(KEY_JDBC_DRIVER));
    conn1.setUsername(PROPERTIES.getProperty(KEY_JDBC_USERNAME));
    conn1.setPassword(PROPERTIES.getProperty(KEY_JDBC_PASSWORD));
    conn1.setConnectionAttempts(1);
    conn1.setConnectionRetryInterval(new TimeInterval(10L, TimeUnit.MILLISECONDS.name()));
    conn1.setTestStatement(PROPERTIES.getProperty(KEY_JDBC_TEST_STATEMENT));
    conn1.setDebugMode(true);
    
    return conn1;
  }

  @Override
  public void testConnect_WithPoorlyEncryptedPassword() throws Exception {
    if (Boolean.parseBoolean(PROPERTIES.getProperty(KEY_TESTS_ENABLED, "false"))) {
      DatabaseConnection con = createConnection();
      con.setPassword("PW:ABCDEFG");
      try {
        LifecycleHelper.init(con);
        Connection sqlConnection = con.connect();
        fail("Successful connection with bad password");
      }
      catch (SQLException expected) {

      }
      catch (CoreException expected) {

      }
    }
  }
}
