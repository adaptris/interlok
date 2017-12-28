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
package com.adaptris.core.services.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.Test;

import com.adaptris.core.CoreException;
import com.adaptris.core.jdbc.JdbcConnection;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public abstract class JdbcMapInsertCase {

  protected static final String CONTENT =
      "firstname=alice\n" + 
      "lastname=smith\n" + 
      "dob=2017-01-01";

  protected static final String INVALID_COLUMN =
      "fi$rstname=alice\n" + "la$stname=smith\n" + "dob=2017-01-01";

  protected static final String JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
  protected static final String JDBC_URL = "jdbc:derby:memory:JDCB_OBJ_DB;create=true";
  protected static final String TABLE_NAME = "people";
  protected static final String DROP_STMT = String.format("DROP TABLE %s", TABLE_NAME);
  protected static final String CREATE_STMT = String.format("CREATE TABLE %s (firstname VARCHAR(128) NOT NULL, lastname VARCHAR(128) NOT NULL, dob DATE)",
      TABLE_NAME);
  protected static final String CREATE_QUOTED = String.format(
      "CREATE TABLE %s (\"firstname\" VARCHAR(128) NOT NULL, \"lastname\" VARCHAR(128) NOT NULL, \"dob\" DATE)", TABLE_NAME);

  @Test
  public void testService_Init() throws Exception {
    JdbcMapInsert service = createService();
    try {
      LifecycleHelper.init(service);
      fail();
    } catch (CoreException expected) {

    }
    service.setTable("hello");
    LifecycleHelper.init(service);
  }

  protected abstract JdbcMapInsert createService();

  protected static void doAssert(int expectedCount) throws Exception {
    Connection c = null;
    PreparedStatement p = null;
    try {
      c = createConnection();
      p = c.prepareStatement(String.format("SELECT * FROM %s", TABLE_NAME));
      ResultSet rs = p.executeQuery();
      int count = 0;
      while (rs.next()) {
        count++;
        assertEquals("smith", rs.getString("lastname"));
      }
      assertEquals(expectedCount, count);
      JdbcUtil.closeQuietly(rs);
    } finally {
      JdbcUtil.closeQuietly(p);
      JdbcUtil.closeQuietly(c);
    }
  }


  protected static Connection createConnection() throws Exception {
    Connection c = null;
    Class.forName(JDBC_DRIVER);
    c = DriverManager.getConnection(JDBC_URL);
    c.setAutoCommit(true);
    return c;
  }

  protected static void createDatabase() throws Exception {
    createDatabase(CREATE_STMT);
  }

  protected static void createDatabase(String createStmt) throws Exception {
    Connection c = null;
    Statement s = null;
    try {
      c = createConnection();
      s = c.createStatement();
      executeQuietly(s, DROP_STMT);
      s.execute(createStmt);
    }
    finally {
      JdbcUtil.closeQuietly(s);
      JdbcUtil.closeQuietly(c);
    }
  }

  protected static void executeQuietly(Statement s, String sql) {
    try {
      s.execute(sql);
    } catch (Exception e) {
      ;
    }
  }

  protected static <T> T configureForTests(T t) {
    JdbcMapInsert service = (JdbcMapInsert) t;
    JdbcConnection connection = new JdbcConnection();
    connection.setConnectUrl(JDBC_URL);
    connection.setDriverImp(JDBC_DRIVER);
    service.setConnection(connection);
    KeyValuePairSet mappings = new KeyValuePairSet();
    mappings.add(new KeyValuePair("dob", JdbcMapInsert.BasicType.Date.name()));
    service.withTable(TABLE_NAME).withMappings(mappings);
    return t;
  }


}
