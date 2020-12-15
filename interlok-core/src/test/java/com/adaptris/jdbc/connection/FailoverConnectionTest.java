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

package com.adaptris.jdbc.connection;

import com.adaptris.core.util.JdbcUtil;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static com.adaptris.jdbc.connection.FailoverConfig.JDBC_ALWAYS_VERIFY;
import static com.adaptris.jdbc.connection.FailoverConfig.JDBC_AUTO_COMMIT;
import static com.adaptris.jdbc.connection.FailoverConfig.JDBC_DEBUG;
import static com.adaptris.jdbc.connection.FailoverConfig.JDBC_DRIVER;
import static com.adaptris.jdbc.connection.FailoverConfig.JDBC_PASSWORD;
import static com.adaptris.jdbc.connection.FailoverConfig.JDBC_URL_ROOT;
import static com.adaptris.jdbc.connection.FailoverConfig.JDBC_USERNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author howard
 */
public class FailoverConnectionTest extends com.adaptris.interlok.junit.scaffolding.BaseCase {

  @Before
  public void setUp() throws Exception {
    initialiseDatabase();
  }

  @Test
  public void testFailoverConfigSetters() throws Exception {
    FailoverConfig fc1 = new FailoverConfig();
    try {
      fc1.setConnectionUrls(null);
      fail("Expected IllegalArgumentException");
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      fc1.addConnectionUrl(null);
      fail("Expected IllegalArgumentException");
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      fc1.setDatabaseDriver(null);
      fail("Expected IllegalArgumentException");
    }
    catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void testInit() throws Exception {
    Properties p = createProperties();
    p.setProperty(JDBC_USERNAME, "myUser");
    p.setProperty(JDBC_PASSWORD, "myPassword");
    FailoverConfig fc = new FailoverConfig(p);
    assertEquals("myUser", fc.getUsername());
    assertEquals("myPassword", fc.getPassword());

    fc = new FailoverConfig(createProperties());
    assertNull(fc.getUsername());
    assertNull(fc.getPassword());
  }

  @Test
  public void testFailoverConfigEquality() throws Exception {
    FailoverConfig fc1 = createFailoverConfig();
    fc1.toString();
    assertFalse(fc1.equals(null));
    assertFalse(fc1.equals(new Object()));
    assertFalse(fc1.equals(new FailoverConfig()));
    assertTrue(fc1.equals(fc1));

    FailoverConfig fc2 = createFailoverConfig();
    assertEquals(fc1, fc2);
    fc2 = new FailoverConfig(createProperties());
    assertEquals(fc1, fc2);
  }

  @Test
  public void testFailoverConfigClone() throws Exception {
    FailoverConfig fc1 = createFailoverConfig();
    FailoverConfig fc2 = (FailoverConfig) fc1.clone();
    assertEquals(fc1.toString(), fc1, fc2);
    assertEquals(fc1.toString(), fc1.hashCode(), fc2.hashCode());
  }

  @Test
  public void testNoDriver() throws Exception {
    FailoverConfig cfg = createFailoverConfig();
    cfg.setDatabaseDriver("hello.there");
    try {
      FailoverConnection c = new FailoverConnection(cfg);
      fail();
    } catch (SQLException expected) {

    }
  }

  @Test
  public void testConnection() throws Exception {
    try {
      FailoverConnection c = new FailoverConnection(new FailoverConfig());
      fail("suceeded w/o any urls");
    } catch (Exception e) {
      ;
    }
    FailoverConfig cfg = createFailoverConfig();
    FailoverConnection c = new FailoverConnection(cfg);
    assertEquals(cfg, c.getConfig());
    Connection con = c.getConnection();
    try {
      assertFalse(con.isClosed());
      createTables(con);
    } finally {
      JdbcUtil.closeQuietly(con);
      c.close();
    }
  }

  @Test
  public void testConnection_BadPassword() throws Exception {
    FailoverConfig cfg = createFailoverConfig();
    cfg.setPassword("ALTPW:abcdef");
    FailoverConnection c = new FailoverConnection(cfg);
    try {
      Connection con = c.getConnection();
      fail("Shouldn't be able to decrypt password");
    } catch (SQLException expected) {
    }
  }

  @Test
  public void testConnection_NoDebug() throws Exception {
    try {
      FailoverConnection c = new FailoverConnection(new FailoverConfig());
      fail("suceeded w/o any urls");
    } catch (Exception e) {
      ;
    }
    FailoverConfig cfg = createFailoverConfig();
    cfg.setAlwaysValidateConnection(true);
    cfg.setDebugMode(false);
    FailoverConnection c = new FailoverConnection(cfg);
    assertEquals(cfg, c.getConfig());
    Connection con = c.getConnection();
    try {
      assertFalse(con.isClosed());
      createTables(con);
    } finally {
      JdbcUtil.closeQuietly(con);
      c.close();
    }
  }

  @Test
  public void testConnection_NoValidate() throws Exception {
    try {
      FailoverConnection c = new FailoverConnection(new FailoverConfig());
      fail("suceeded w/o any urls");
    } catch (Exception e) {
      ;
    }
    FailoverConfig cfg = createFailoverConfig();
    cfg.setAlwaysValidateConnection(false);
    cfg.setDebugMode(false);
    FailoverConnection c = new FailoverConnection(cfg);
    assertEquals(cfg, c.getConfig());
    Connection con = c.getConnection();
    try {
      assertFalse(con.isClosed());
      createTables(con);
    } finally {
      JdbcUtil.closeQuietly(con);
      c.close();
    }
  }


  private FailoverConfig createFailoverConfig() {
    FailoverConfig fc = new FailoverConfig();
    fc.addConnectionUrl(PROPERTIES.getProperty("jdbc.url"));
    fc.addConnectionUrl(PROPERTIES.getProperty("jdbc.url.2"));
    fc.setAlwaysValidateConnection(true);
    fc.setAutoCommit(true);
    fc.setDatabaseDriver(PROPERTIES.getProperty("jdbc.driver"));
    fc.setDebugMode(true);
    return fc;
  }

  protected static Properties createProperties() {
    Properties p = new Properties();
    p.setProperty(JDBC_DRIVER, PROPERTIES.getProperty("jdbc.driver"));
    p.setProperty(JDBC_AUTO_COMMIT, "true");
    p.setProperty(JDBC_DEBUG, "true");
    p.setProperty(JDBC_ALWAYS_VERIFY, "true");
    p.setProperty(JDBC_URL_ROOT + ".1", PROPERTIES.getProperty("jdbc.url"));
    p.setProperty(JDBC_URL_ROOT + ".2", PROPERTIES.getProperty("jdbc.url.2"));
    return p;
  }

  private void initialiseDatabase() throws Exception {
    Connection dbCon = connect();
    try {
      createTables(dbCon);
    }
    finally {
      JdbcUtil.closeQuietly(dbCon);
    }
  }

  protected static Connection connect() throws Exception {
    Class.forName(PROPERTIES.getProperty("jdbc.driver"));
    return DriverManager.getConnection(PROPERTIES.getProperty("jdbc.url"));
  }

  protected static void createTables(Connection dbCon) throws SQLException {
    Statement stmt = null;
    try {
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
    }
  }
}
