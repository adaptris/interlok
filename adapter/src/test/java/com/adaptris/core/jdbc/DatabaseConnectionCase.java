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
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.BaseCase;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.IdGenerator;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.PlainIdGenerator;
import com.adaptris.util.TimeInterval;

public abstract class DatabaseConnectionCase<T extends DatabaseConnection> extends BaseCase {

  protected static final String DEFAULT_TEST_STATEMENT = "SELECT seq_number from sequences where id='id'";

  protected static IdGenerator nameGen;
  static {
    try {
      nameGen = new GuidGenerator();
    }
    catch (Exception e) {
      nameGen = new PlainIdGenerator("_");
    }
  }
  public DatabaseConnectionCase(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {
    initialiseDatabase(PROPERTIES.getProperty("jdbc.driver"), PROPERTIES.getProperty("jdbc.url"));
  }

  public void testXmlRoundTrip() throws Exception {
    DatabaseConnection conn1 = configure(createConnection());
    DatabaseConnection conn2 = roundTrip(conn1, DefaultMarshaller.getDefaultMarshaller());
    assertRoundtripEquality(conn1, conn2);
  }

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

  public void testEquality() throws Exception {
    DatabaseConnection conn1 = configure(createConnection());
    DatabaseConnection conn2 = roundTrip(conn1, DefaultMarshaller.getDefaultMarshaller());
    assertEquals(conn1, conn2);
  }

  public void testInvalidSelectStatement() throws Exception {
    DatabaseConnection conn = configure(createConnection());
    conn.setTestStatement("What Ho");
    conn.setAlwaysValidateConnection(true);
    try {
      LifecycleHelper.init(conn);
      conn.connect();
      fail("Expected exception");
    }
    catch (Exception expected) {
      ;
    }
  }


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

  public void testConnectWithoutDebugMode() throws Exception {
    DatabaseConnection conn = configure(createConnection());
    conn.setTestStatement(DEFAULT_TEST_STATEMENT);
    conn.setDebugMode(false);
    LifecycleHelper.init(conn);
    conn.connect();
  }

  public void testConnectWithDebugMode() throws Exception {
    DatabaseConnection conn = configure(createConnection());
    conn.setDebugMode(true);
    conn.setTestStatement(DEFAULT_TEST_STATEMENT);
    LifecycleHelper.init(conn);
    conn.connect();
  }

  public void testConnectWithAlwaysValidate() throws Exception {
    DatabaseConnection conn = configure(createConnection());
    conn.setAlwaysValidateConnection(true);
    conn.setTestStatement(DEFAULT_TEST_STATEMENT);
    LifecycleHelper.init(conn);
    conn.connect();
  }

  public void testConnectWithoutAlwaysValidate() throws Exception {
    DatabaseConnection conn = configure(createConnection());
    conn.setAlwaysValidateConnection(false);
    conn.setTestStatement(DEFAULT_TEST_STATEMENT);
    LifecycleHelper.init(conn);
    conn.connect();
  }

  public void testConnectWithDebugModeAndAlwaysValidate() throws Exception {
    DatabaseConnection conn = configure(createConnection());
    conn.setDebugMode(true);
    conn.setAlwaysValidateConnection(true);
    conn.setTestStatement(DEFAULT_TEST_STATEMENT);
    LifecycleHelper.init(conn);
    conn.connect();
  }

  public void testConnectionWhenInitialised() throws Exception {
    DatabaseConnection con = configure(createConnection());
    LifecycleHelper.init(con);
    con.connect();
  }

  public void testConnectionWhenStarted() throws Exception {
    DatabaseConnection con = configure(createConnection());
    LifecycleHelper.init(con);
    LifecycleHelper.start(con);
    con.connect();
  }

  public void testConnectWithNullProperties() throws Exception {
    DatabaseConnection con = configure(createConnection());
    con.setConnectionProperties(null);
    LifecycleHelper.init(con);
    LifecycleHelper.start(con);
    con.connect();
  }

  public void testConnectWithProperties() throws Exception {
    DatabaseConnection con = configure(createConnection());
    con.setConnectionProperties(new KeyValuePairSet());
    LifecycleHelper.init(con);
    LifecycleHelper.start(con);
    con.connect();
  }

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

  protected abstract T configure(T c);

  private void initialiseDatabase(String driver, String url) throws Exception {
    Class.forName(driver);
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
  }
}
