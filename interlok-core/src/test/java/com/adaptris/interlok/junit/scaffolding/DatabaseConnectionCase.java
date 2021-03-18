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

package com.adaptris.interlok.junit.scaffolding;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.jdbc.DatabaseConnection;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.TimeInterval;

public abstract class DatabaseConnectionCase<T extends DatabaseConnection> extends BaseCase {
  protected static final String DRIVER_IMP = "org.apache.derby.jdbc.EmbeddedDriver";

  protected static final String DEFAULT_TEST_STATEMENT = "SELECT seq_number from sequences where id='id'";

  protected static GuidGenerator nameGen = new GuidGenerator();

  public DatabaseConnectionCase() {
  }

  @Test
  public void testXmlRoundTrip() throws Exception {
    DatabaseConnection conn1 = configure(createConnection());
    DatabaseConnection conn2 = roundTrip(conn1, DefaultMarshaller.getDefaultMarshaller());
    assertRoundtripEquality(conn1, conn2);
  }

  @Test
  public void testSetConnectionRetryInterval() throws Exception {
    DatabaseConnection con = createConnection();
    assertNull(con.getConnectionRetryInterval());
    assertEquals(60000, con.connectionRetryInterval());
    TimeInterval t = new TimeInterval(1L, TimeUnit.HOURS);
    con.setConnectionRetryInterval(t);
    assertEquals(t, con.getConnectionRetryInterval());
    assertEquals(t.toMilliseconds(), con.connectionRetryInterval());
    con.setConnectionRetryInterval(null);
    assertNull(con.getConnectionRetryInterval());
    assertEquals(60000, con.connectionRetryInterval());
  }

  @Test
  public void testSetConnectionAttempts() throws Exception {
    DatabaseConnection con = createConnection();
    assertNull(con.getConnectionAttempts());
    assertEquals(-1, con.connectionAttempts());
    con.setConnectionAttempts(Integer.valueOf(1));
    assertEquals(Integer.valueOf(1), con.getConnectionAttempts());
    assertEquals(1, con.connectionAttempts());
    con.setConnectionAttempts(null);
    assertNull(con.getConnectionAttempts());
    assertEquals(-1, con.connectionAttempts());
  }

  @Test
  public void testEquality() throws Exception {
    DatabaseConnection conn1 = configure(createConnection());
    DatabaseConnection conn2 = roundTrip(conn1, DefaultMarshaller.getDefaultMarshaller());
    assertRoundtripEquality(conn1, conn2);
  }


  @Test
  public void testConnectionWhenNotInitialised() throws Exception {
    DatabaseConnection con = configure(createConnection());
    try {
      con.connect();
      fail("Expected failure as not initialised");
    }
    catch (Exception e) {
      ; // Expected
    }
  }

  @Test
  public void testConnectWithoutDebugMode() throws Exception {
    DatabaseConnection conn = configure(createConnection());
    conn.setDebugMode(false);
    LifecycleHelper.init(conn);
    conn.connect();
  }

  @Test
  public void testConnectWithDebugMode() throws Exception {
    DatabaseConnection conn = configure(createConnection());
    conn.setDebugMode(true);
    LifecycleHelper.init(conn);
    conn.connect();
  }

  @Test
  public void testConnectWithAlwaysValidate() throws Exception {
    DatabaseConnection conn = configure(createConnection());
    conn.setAlwaysValidateConnection(true);
    LifecycleHelper.init(conn);
    conn.connect();
  }

  @Test
  public void testConnectWithoutAlwaysValidate() throws Exception {
    DatabaseConnection conn = configure(createConnection());
    conn.setAlwaysValidateConnection(false);
    LifecycleHelper.init(conn);
    conn.connect();
  }

  @Test
  public void testConnectWithDebugModeAndAlwaysValidate() throws Exception {
    DatabaseConnection conn = configure(createConnection());
    conn.setDebugMode(true);
    conn.setAlwaysValidateConnection(true);
    LifecycleHelper.init(conn);
    conn.connect();
  }

  @Test
  public void testConnectionWhenInitialised() throws Exception {
    DatabaseConnection con = configure(createConnection());
    LifecycleHelper.init(con);
    con.connect();
  }

  @Test
  public void testConnectionWhenStarted() throws Exception {
    DatabaseConnection con = configure(createConnection());
    LifecycleHelper.init(con);
    LifecycleHelper.start(con);
    con.connect();
  }

  @Test
  public void testConnectWithNullProperties() throws Exception {
    DatabaseConnection con = configure(createConnection());
    con.setConnectionProperties(null);
    LifecycleHelper.init(con);
    LifecycleHelper.start(con);
    con.connect();
  }

  @Test
  public void testConnectWithProperties() throws Exception {
    DatabaseConnection con = configure(createConnection());
    con.setConnectionProperties(new KeyValuePairSet());
    LifecycleHelper.init(con);
    LifecycleHelper.start(con);
    con.connect();
  }

  @Test
  public void testConnectionWhenStopped() throws Exception {
    DatabaseConnection con = configure(createConnection());
    LifecycleHelper.init(con);
    LifecycleHelper.start(con);
    LifecycleHelper.stop(con);
    try {
      con.connect();
      fail("Expected failure as not initialised");
    }
    catch (Exception e) {
      ; // Expected
    }
  }

  @Test
  public void testConnectionWhenClosed() throws Exception {
    DatabaseConnection con = configure(createConnection());
    LifecycleHelper.init(con);
    LifecycleHelper.start(con);
    LifecycleHelper.stop(con);
    LifecycleHelper.close(con);
    try {
      con.connect();
      fail("Expected failure as not initialised");
    }
    catch (Exception e) {
      ; // Expected
    }
  }

  @Test
  public void testConnectionDataSource() throws Exception {
    DatabaseConnection con = configure(createConnection());
    try {
      LifecycleHelper.init(con);
      LifecycleHelper.start(con);
      assertNotNull(con.asDataSource());
      assertNotNull(con.asDataSource().getConnection());
    }
    finally {
      LifecycleHelper.stop(con);
      LifecycleHelper.close(con);
    }
  }


  // @Test
//  public void testConnectionDataSource_NotYetInitialised() throws Exception {
//    DatabaseConnection con = configure(createConnection());
//    try {
//      DataSource ds = con.asDataSource();
//      fail("Not yet initialised should throw exception");
//    }
//    catch (SQLException expected) {
//
//    }
//    finally {
//      LifecycleHelper.stop(con);
//      LifecycleHelper.close(con);
//    }
//  }

  protected DatabaseConnection roundTrip(DatabaseConnection src, AdaptrisMarshaller m)
      throws Exception {
    String xml = m.marshal(src);
    System.out.println(xml);
    return (DatabaseConnection) m.unmarshal(xml);
  }

  protected abstract T createConnection();

  protected abstract T configure(T c) throws Exception;

  protected String initialiseDatabase() throws Exception {
    String url = "jdbc:derby:memory:" + nameGen.safeUUID() + ";create=true";

    Class.forName(DRIVER_IMP);
    Connection dbCon = null;
    Statement stmt = null;
    try {
      dbCon = DriverManager.getConnection(url);
      dbCon.setAutoCommit(true);
      stmt = dbCon.createStatement();
      try {
        stmt.execute("DROP TABLE sequences");
      }
      catch (Exception e) {
      }
      stmt.execute("CREATE TABLE sequences " + "(id VARCHAR(255) NOT NULL, seq_number INT)");
      stmt.executeUpdate("INSERT INTO sequences (id, seq_number) values ('id', 2)");
    }
    finally {
      JdbcUtil.closeQuietly(stmt);
      JdbcUtil.closeQuietly(dbCon);
    }
    return url;
  }

}
