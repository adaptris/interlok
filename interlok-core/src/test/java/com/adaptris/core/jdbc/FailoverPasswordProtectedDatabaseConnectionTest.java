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
import com.adaptris.core.CoreException;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class FailoverPasswordProtectedDatabaseConnectionTest extends PasswordProtectedDatabaseConnectionTest {


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
