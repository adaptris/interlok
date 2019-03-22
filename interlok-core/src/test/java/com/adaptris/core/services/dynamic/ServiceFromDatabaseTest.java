/*******************************************************************************
 * Copyright 2019 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.adaptris.core.services.dynamic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceList;
import com.adaptris.core.jdbc.JdbcConnection;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.util.LifecycleHelper;

public class ServiceFromDatabaseTest {

  private static final String JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
  private static final String JDBC_URL = "jdbc:derby:memory:DSL_FROM_DB;create=true";
  private static final String TABLE_NAME = "services";
  private static final String DROP_STMT = String.format("DROP TABLE %s", TABLE_NAME);
  private static final String CREATE_STMT = String.format(
      "CREATE TABLE %s (source VARCHAR(128) NOT NULL, "
      + "destination VARCHAR(128) NOT NULL, "
          + "dynamicService CLOB NOT NULL)",
      TABLE_NAME);
  private static final String INSERT_STMT = String.format(
      "INSERT INTO %s (source, destination, dynamicService)" + "values (?,?,?)", TABLE_NAME);
  private static final String SELECT_STMT = String.format(
      "SELECT dynamicService FROM %s "
      + "\nWHERE source='%%message{source}' "
      + "\nAND destination='%%message{destination}'",
      TABLE_NAME);

  @Test
  public void testLifecycle() throws Exception {
    JdbcConnection jdbcConn = configure(new JdbcConnection());
    ServiceFromDatabase extractor =
        new ServiceFromDatabase().withQuery(SELECT_STMT);
    try {
      LifecycleHelper.initAndStart(extractor);
      fail();
    } catch (Exception expected) {

    } finally {
      LifecycleHelper.stopAndClose(extractor);
    }
    extractor.setConnection(jdbcConn);
    try {
      LifecycleHelper.initAndStart(extractor);
    } finally {
      LifecycleHelper.stopAndClose(extractor);
    }
  }

  @Test
  public void testGetInputStream() throws Exception {
    DatabaseEntry entry = new DatabaseEntry("mySourcePartner", "myDestinationPartner");
    createDatabase(Arrays.asList(entry));
    JdbcConnection jdbcConn = configure(new JdbcConnection());

    ServiceFromDatabase extractor =
        new ServiceFromDatabase().withQuery(SELECT_STMT).withConnection(jdbcConn);
    
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("source", "mySourcePartner");
    msg.addMetadata("destination", "myDestinationPartner");
    try {
      LifecycleHelper.initAndStart(extractor);
      try (InputStream in = extractor.getInputStream(msg)) {
        assertNotNull(in);
        assertEquals(ServiceList.class,
            DefaultMarshaller.getDefaultMarshaller().unmarshal(in).getClass());
      }
    } finally {
      LifecycleHelper.stopAndClose(extractor);
    }
  }

  @Test(expected = ServiceException.class)
  public void testNotFound() throws Exception {
    DatabaseEntry entry = new DatabaseEntry("mySourcePartner", "myDestinationPartner");
    createDatabase(Arrays.asList(entry));
    JdbcConnection jdbcConn = configure(new JdbcConnection());
    ServiceFromDatabase extractor =
        new ServiceFromDatabase().withQuery(SELECT_STMT).withConnection(jdbcConn);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("source", "anotherSourcePartner");
    msg.addMetadata("destination", "anotherDestinationPartner");
    try {
      LifecycleHelper.initAndStart(extractor);
      // Since there's no resultSet.next(), should throw a ServiceException.
      InputStream in = extractor.getInputStream(msg);
    } finally {
      LifecycleHelper.stopAndClose(extractor);
    }
  }


  protected static Connection createConnection() throws Exception {
    Class.forName(JDBC_DRIVER);
    Connection c = DriverManager.getConnection(JDBC_URL);
    c.setAutoCommit(true);
    return c;
  }

  protected static JdbcConnection configure(JdbcConnection c) {
    c.setDriverImp(JDBC_DRIVER);
    c.setConnectUrl(JDBC_URL);
    c.setAutoCommit(true);
    return c;
  }

  protected static void createDatabase(List<DatabaseEntry> list) throws Exception {
    createDatabase();
    try (Connection c = createConnection();
        PreparedStatement insert = c.prepareStatement(INSERT_STMT);) {
      for (DatabaseEntry entry : list) {
        insert.clearParameters();
        insert.setString(1, entry.source);
        insert.setString(2, entry.destination);
        insert.setString(3, entry.service);
        insert.executeUpdate();
      }
    }
  }

  protected static void createDatabase() throws Exception {
    try (Connection c = createConnection(); Statement s = c.createStatement()) {
      executeQuietly(s, DROP_STMT);
      s.execute(CREATE_STMT);
    }
  }

  protected static void executeQuietly(Statement s, String sql) {
    try {
      s.execute(sql);
    } catch (Exception e) {
      ;
    }
  }


  private class DatabaseEntry {
    String source;
    String destination;
    String service;

    public DatabaseEntry(String src, String dest) throws Exception {
      source = src;
      destination = dest;
      service = DynamicServiceExecutorTest.createMessage(new ServiceList(new LogMessageService()))
          .getContent();
    }
  }
}
