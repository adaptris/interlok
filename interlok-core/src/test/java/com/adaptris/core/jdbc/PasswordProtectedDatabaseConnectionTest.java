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
import static org.junit.Assert.fail;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.security.password.Password;
import com.adaptris.util.TimeInterval;

// Note that this class reuses- the jdbc.storedproc test flags, because
// That explicitly relies on mysql which is password protected rather than derby (which might not be).
public class PasswordProtectedDatabaseConnectionTest
    extends com.adaptris.interlok.junit.scaffolding.BaseCase {
  protected static final String KEY_TESTS_ENABLED = "jdbc.storedproc.tests.enabled";
  protected static final String KEY_JDBC_PASSWORD = "jdbc.storedproc.password";
  protected static final String KEY_JDBC_USERNAME = "jdbc.storedproc.username";
  protected static final String KEY_JDBC_VENDOR = "jdbc.storedproc.vendor";
  protected static final String KEY_JDBC_URL = "jdbc.storedproc.url.noacl";
  protected static final String KEY_JDBC_DRIVER = "jdbc.storedproc.driver";
  protected static final String KEY_JDBC_TEST_STATEMENT = "jdbc.storedproc.teststatement";


  @Test
  public void testConnect_WithPlainPassword() throws Exception {
    if (Boolean.parseBoolean(PROPERTIES.getProperty(KEY_TESTS_ENABLED, "false"))) {
      DatabaseConnection con = createConnection();
      LifecycleHelper.init(con);
      Connection sqlConnection = con.connect();
    }
  }

  @Test
  public void testConnect_WithIncorrectPassword() throws Exception {
    if (Boolean.parseBoolean(PROPERTIES.getProperty(KEY_TESTS_ENABLED, "false"))) {
      DatabaseConnection con = createConnection();
      con.setPassword(String.valueOf(System.currentTimeMillis()));
      // We expect to see a SQL Exception after probbably 1 attempt.
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

  @Test
  public void testConnect_WithEncryptedPassword() throws Exception {
    if (Boolean.parseBoolean(PROPERTIES.getProperty(KEY_TESTS_ENABLED, "false"))) {
      DatabaseConnection con = createConnection();
      con.setPassword(Password.encode(PROPERTIES.getProperty(KEY_JDBC_PASSWORD), Password.PORTABLE_PASSWORD));
      LifecycleHelper.init(con);
      Connection sqlConnection = con.connect();
    }
  }

  @Test
  public void testConnect_WithPoorlyEncryptedPassword() throws Exception {
    if (Boolean.parseBoolean(PROPERTIES.getProperty(KEY_TESTS_ENABLED, "false"))) {
      DatabaseConnection con = createConnection();
      con.setPassword("PW:ABCDEFG");
      // We expect to see a SQL Exception after probbably 1 attempt.
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

  protected DatabaseConnection createConnection() {
    JdbcConnection conn1 = new JdbcConnection();
    conn1.setConnectUrl(PROPERTIES.getProperty(KEY_JDBC_URL));
    conn1.setDriverImp(PROPERTIES.getProperty(KEY_JDBC_DRIVER));
    conn1.setUsername(PROPERTIES.getProperty(KEY_JDBC_USERNAME));
    conn1.setPassword(PROPERTIES.getProperty(KEY_JDBC_PASSWORD));
    conn1.setConnectionAttempts(1);
    conn1.setConnectionRetryInterval(new TimeInterval(10L, TimeUnit.MILLISECONDS.name()));
    return conn1;
  }

}
