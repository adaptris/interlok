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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.jdbc.JdbcConnection;
import com.adaptris.core.services.jdbc.StatementParameterImpl.QueryType;
import com.adaptris.core.services.jdbc.raw.JdbcRawDataCaptureService;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;

public class JdbcCaptureNullsTest {

  private static final String ANYTHING = "anything";
  protected static final String JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
  protected static final String JDBC_URL = "jdbc:derby:memory:JdbcCaptureNullsTest;create=true";
  private static final String CONTENT_NULl = "<root></root>";
  private static final String CONTENT_NOT_NULL = "<root><PurchaseDate>2000-01-01</PurchaseDate></root>";
  private static final String CONTENT_EMPTY = "<root><PurchaseDate></PurchaseDate></root>";

  @Test
  public void testService_NullContent() throws Exception {
    createDatabase();
    JdbcDataCaptureServiceImpl service = configure(new JdbcDataCaptureService());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CONTENT_NULl);
    ExampleServiceCase.execute(service, msg);
    doBasicCaptureAsserts(null);
  }

  @Test
  public void testService_WithContent() throws Exception {
    createDatabase();
    JdbcDataCaptureServiceImpl service = configure(new JdbcDataCaptureService());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CONTENT_NOT_NULL);
    ExampleServiceCase.execute(service, msg);
    doBasicCaptureAsserts("2000-01-01");
  }

  @Test
  public void testService_WithEmptyContent() throws Exception {
    createDatabase();
    JdbcDataCaptureServiceImpl service = configure(new JdbcDataCaptureService());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CONTENT_EMPTY);
    ExampleServiceCase.execute(service, msg);
    doBasicCaptureAsserts("");
  }

  @Test(expected = ServiceException.class)
  public void testRawService_NotXML() throws Exception {
    createDatabase();
    JdbcDataCaptureServiceImpl service = configure(new JdbcRawDataCaptureService());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("hello world");
    ExampleServiceCase.execute(service, msg);
  }

  @Test
  public void testRawService_NullContent() throws Exception {
    createDatabase();
    JdbcDataCaptureServiceImpl service = configure(new JdbcRawDataCaptureService());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CONTENT_NULl);
    ExampleServiceCase.execute(service, msg);
    doBasicCaptureAsserts(null);
  }

  @Test
  public void testRawService_WithContent() throws Exception {
    createDatabase();
    JdbcDataCaptureServiceImpl service = configure(new JdbcRawDataCaptureService());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CONTENT_NOT_NULL);
    ExampleServiceCase.execute(service, msg);
    doBasicCaptureAsserts("2000-01-01");
  }

  @Test
  public void testRawService_WithEmptyContent() throws Exception {
    createDatabase();
    JdbcDataCaptureServiceImpl service = configure(new JdbcRawDataCaptureService());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CONTENT_EMPTY);
    ExampleServiceCase.execute(service, msg);
    doBasicCaptureAsserts("");
  }

  private void doBasicCaptureAsserts(String expectedValue) throws Exception {
    Connection c = null;
    PreparedStatement p = null;
    try {
      c = createConnection();
      p = c.prepareStatement("SELECT * FROM jdbc_capture_null");
      ResultSet rs = p.executeQuery();
      while (rs.next()) {
        assertEquals(ANYTHING, rs.getString("another_string_value"));
        assertEquals(expectedValue, rs.getString("string_value"));
      }
      JdbcUtil.closeQuietly(rs);
    }
    finally {
      JdbcUtil.closeQuietly(p);
      JdbcUtil.closeQuietly(c);
    }
  }

  private JdbcDataCaptureServiceImpl configure(JdbcDataCaptureServiceImpl service) {
    JdbcConnection connection = new JdbcConnection();
    connection.setConnectUrl(JDBC_URL);
    connection.setDriverImp(JDBC_DRIVER);
    service.setConnection(connection);
    service.addStatementParameter(new StringStatementParameter("/root/PurchaseDate", QueryType.xpath, false, null));
    service.addStatementParameter(new StringStatementParameter(ANYTHING, QueryType.constant, false, null));
    service.setStatement("insert into jdbc_capture_null (string_value, another_string_value) values (?, ?)");
    return service;
  }

  private static Connection createConnection() throws Exception {
    Connection c = null;
    Class.forName(JDBC_DRIVER);
    c = DriverManager.getConnection(JDBC_URL);
    c.setAutoCommit(true);
    return c;
  }

  private static void createDatabase() throws Exception {
    Connection c = null;
    Statement s = null;
    try {
      c = createConnection();
      s = c.createStatement();
      executeQuietly(s, "DROP TABLE jdbc_capture_null");
      s.execute(
          "CREATE TABLE jdbc_capture_null (string_value VARCHAR(128) DEFAULT NULL, another_string_value VARCHAR(128) NOT NULL)");
    }
    finally {
      JdbcUtil.closeQuietly(s);
      JdbcUtil.closeQuietly(c);
    }
  }

  private static void executeQuietly(Statement s, String sql) {
    try {
      s.execute(sql);
    }
    catch (Exception e) {
      ;
    }
  }
}
