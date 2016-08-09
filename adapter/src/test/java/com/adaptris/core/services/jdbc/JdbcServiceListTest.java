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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.NullService;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceCollectionCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.jdbc.AdvancedJdbcPooledConnection;
import com.adaptris.core.jdbc.DatabaseConnection;
import com.adaptris.core.jdbc.JdbcConnection;
import com.adaptris.core.jdbc.JdbcConstants;
import com.adaptris.core.jdbc.JdbcPooledConnection;
import com.adaptris.core.jdbc.PooledConnectionHelper;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.TimeInterval;

public class JdbcServiceListTest extends ServiceCollectionCase {

  private static final String CFG_JDBC_URL = "jdbc.jdbcservicelist.url";
  private static final String CFG_JDBC_DRIVER = "jdbc.jdbcservicelist.driver";

  public JdbcServiceListTest(String name) {
    super(name);
  }

  @Override
  public void setUp() throws Exception {
  }

  public void testSetDatabaseConnection() {
    JdbcConnection connection = new JdbcConnection();
    JdbcServiceList list = new JdbcServiceList();
    assertNull(list.getDatabaseConnection());
    list.setDatabaseConnection(connection);
    assertEquals(connection, list.getDatabaseConnection());
    list.setDatabaseConnection(null);
    assertEquals(null, list.getDatabaseConnection());
  }

  public void testServiceList_NoConnectionInObjectMetadata() throws Exception {
    createDatabase();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    JdbcServiceList list = new JdbcServiceList();
    list.add(createSequenceNumberService(createJdbcConnection(), getName(), SequenceNumberCase.DEFAULT_ID));
    execute(list, msg);
    doStandardAssertions(msg);
    assertFalse(msg.getObjectHeaders().containsKey(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY));
  }

  public void testServiceList_SqlConnectionInObjectMetadata() throws Exception {
    createDatabase();
    JdbcServiceList service = createServiceCollection();
    service.setDatabaseConnection(createJdbcConnection());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

    execute(service, msg);
    assertTrue(msg.getObjectHeaders().containsKey(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY));
    Connection sqlCon = (Connection) msg.getObjectHeaders().get(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY);
  }

  public void testServiceList_NoConnectionInObjectMetadata_WithException() throws Exception {
    createDatabase();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    JdbcServiceList list = new JdbcServiceList();
    list.add(createSequenceNumberService(createJdbcConnection(), getName(), SequenceNumberCase.DEFAULT_ID));
    list.add(new NullService() {
      @Override
      public void doService(AdaptrisMessage msg) throws ServiceException {
        throw new ServiceException("testServiceList_NoConnectionInObjectMetadata_WithException throws an Exception");
      }
    });
    try {
      execute(list, msg);
    }
    catch (ServiceException expected) {
      //
    }
    doStandardAssertions(msg); // This checks the database has been commited, as the next seq_no == 2.
    assertFalse(msg.getObjectHeaders().containsKey(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY));
  }

  public void testServiceList_SequenceNumber_Commit_AutoCommit() throws Exception {
    createDatabase();
    JdbcServiceList service = createServiceCollection();
    service.setDatabaseConnection(createJdbcConnection());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

    service.add(createSequenceNumberService(null, getName(), SequenceNumberCase.DEFAULT_ID));
    execute(service, msg);
    doStandardAssertions(msg);
    assertTrue(msg.getObjectHeaders().containsKey(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY));
    Connection sqlCon = (Connection) msg.getObjectHeaders().get(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY);
  }

  public void testServiceList_PooledConnection() throws Exception {
    int maxServices = 5;
    final int iterations = 5;
    int poolsize = maxServices - 1;

    createDatabase();
    List<Service> serviceList = new ArrayList<Service>();
    String name = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    JdbcPooledConnection conn = PooledConnectionHelper.createPooledConnection(PROPERTIES.getProperty(CFG_JDBC_DRIVER),
        PROPERTIES.getProperty(CFG_JDBC_URL), poolsize);

    try {
      GuidGenerator guid = new GuidGenerator();
      for (int i = 0; i < maxServices; i++) {
        // The Connection should never be used by the wrappedService, as it will exist in objectMetadata.
        JdbcServiceList service = createServiceCollection(createSequenceNumberService(conn, guid.safeUUID()),
            createSequenceNumberService(conn, guid.safeUUID()));
        service.setDatabaseConnection(conn);
        serviceList.add(service);
        start(service);
      }
      assertEquals(0, conn.currentBusyConnectionCount());
      PooledConnectionHelper.executeTest(serviceList, iterations, new PooledConnectionHelper.MessageCreator() {

        @Override
        public AdaptrisMessage createMsgForPooledConnectionTest() throws Exception {
          return AdaptrisMessageFactory.getDefaultInstance().newMessage();
        }
      });
      assertEquals(0, conn.currentBusyConnectionCount());
      assertEquals(poolsize, conn.currentIdleConnectionCount());
      assertEquals(poolsize, conn.currentConnectionCount());
    }
    finally {
      stop(serviceList.toArray(new ComponentLifecycle[0]));
      Thread.currentThread().setName(name);
    }
  }
  
  public void testServiceList_AdvancedPooledConnection() throws Exception {
    int maxServices = 5;
    final int iterations = 5;
    int poolsize = maxServices - 1;

    createDatabase();
    List<Service> serviceList = new ArrayList<Service>();
    String name = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    AdvancedJdbcPooledConnection conn = PooledConnectionHelper.createAdvancedPooledConnection(PROPERTIES.getProperty(CFG_JDBC_DRIVER),
        PROPERTIES.getProperty(CFG_JDBC_URL), poolsize);

    try {
      GuidGenerator guid = new GuidGenerator();
      for (int i = 0; i < maxServices; i++) {
        // The Connection should never be used by the wrappedService, as it will exist in objectMetadata.
        JdbcServiceList service = createServiceCollection(createSequenceNumberService(conn, guid.safeUUID()),
            createSequenceNumberService(conn, guid.safeUUID()));
        service.setDatabaseConnection(conn);
        serviceList.add(service);
        start(service);
      }
      assertEquals(0, conn.currentBusyConnectionCount());
      PooledConnectionHelper.executeTest(serviceList, iterations, new PooledConnectionHelper.MessageCreator() {

        @Override
        public AdaptrisMessage createMsgForPooledConnectionTest() throws Exception {
          return AdaptrisMessageFactory.getDefaultInstance().newMessage();
        }
      });
      assertEquals(0, conn.currentBusyConnectionCount());
      assertEquals(poolsize, conn.currentIdleConnectionCount());
      assertEquals(poolsize, conn.currentConnectionCount());
    }
    finally {
      stop(serviceList.toArray(new ComponentLifecycle[0]));
      Thread.currentThread().setName(name);
    }
  }

  public void testServiceList_SequenceNumber_NoAutoCommit() throws Exception {
    createDatabase();
    JdbcServiceList service = createServiceCollection();
    DatabaseConnection c = createJdbcConnection();
    c.setAutoCommit(false);
    service.setDatabaseConnection(c);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

    service.add(createSequenceNumberService(null, getName(), SequenceNumberCase.DEFAULT_ID));
    execute(service, msg);
    doStandardAssertions(msg);
    assertTrue(msg.getObjectHeaders().containsKey(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY));
    Connection sqlCon = (Connection) msg.getObjectHeaders().get(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY);
  }

  public void testServiceList_ExceptionRollsback() throws Exception {
    createDatabase();
    JdbcServiceList service = createServiceCollection();
    DatabaseConnection c = createJdbcConnection();
    c.setAutoCommit(false);
    c.setDebugMode(true);
    service.setDatabaseConnection(c);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    String oldName = Thread.currentThread().getName();
    Thread.currentThread().setName("testServiceList_ExceptionRollsback");
    try {
      service.add(createSequenceNumberService(null, getName(), SequenceNumberCase.DEFAULT_ID));
      service.add(new NullService() {
        @Override
        public void doService(AdaptrisMessage msg) throws ServiceException {
          throw new ServiceException("testServiceList_ExceptionRollsback throws an Exception");
        }
      });
      try {
        execute(service, msg);
      }
      catch (ServiceException expected) {
      }
      assertTrue(msg.getObjectHeaders().containsKey(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY));
      Connection conn = (Connection) msg.getObjectHeaders().get(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY);
      assertTrue(conn.isClosed());
      // Here, we will expect there to be in row inserted.
      // getCurrentSequenceNumber returns -1 in that instance, C-Styley
      assertEquals(-1, getCurrentSequenceNumber(SequenceNumberCase.DEFAULT_ID));
    }
    finally {
      Thread.currentThread().setName(oldName);
    }
  }

  public void testServiceList_RuntimeExceptionRollsback() throws Exception {
    createDatabase();
    JdbcServiceList service = createServiceCollection();
    DatabaseConnection c = createJdbcConnection();
    c.setAutoCommit(false);
    service.setDatabaseConnection(c);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

    service.add(createSequenceNumberService(null, getName(), SequenceNumberCase.DEFAULT_ID));
    service.add(new NullService() {
      @Override
      public void doService(AdaptrisMessage msg) throws ServiceException {
        throw new RuntimeException("testServiceList_RuntimeExceptionRollsback");
      }
    });
    try {
      execute(service, msg);
    }
    catch (ServiceException expected) {

    }
    assertTrue(msg.getObjectHeaders().containsKey(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY));
    Connection conn = (Connection) msg.getObjectHeaders().get(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY);
    assertTrue(conn.isClosed());
    // Here, we will expect there to be in row inserted.
    // getCurrentSequenceNumber returns -1 in that instance, C-Styley
    assertEquals(-1, getCurrentSequenceNumber(SequenceNumberCase.DEFAULT_ID));

  }

  private void doStandardAssertions(AdaptrisMessage msg) throws Exception {
    assertTrue(msg.containsKey(SequenceNumberCase.DEFAULT_METADATA_KEY));
    assertEquals("000000001", msg.getMetadataValue(SequenceNumberCase.DEFAULT_METADATA_KEY));
    assertEquals(2, getCurrentSequenceNumber(SequenceNumberCase.DEFAULT_ID));
  }

  private void createDatabase() throws Exception {
    Connection c = createConnection();
    dropDatabase(c);
    Statement s = c.createStatement();
    s.execute("CREATE TABLE sequences (id VARCHAR(255) NOT NULL, seq_number INT)");
    JdbcUtil.closeQuietly(s);
    JdbcUtil.closeQuietly(c);
  }

  private Connection createConnection() throws Exception {
    Class.forName(PROPERTIES.getProperty(CFG_JDBC_DRIVER));
    Connection c = DriverManager.getConnection(PROPERTIES.getProperty(CFG_JDBC_URL));
    c.setAutoCommit(true);
    c.setHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT);
    return c;
  }

  private void dropDatabase(Connection c) throws Exception {
    Connection db = c;
    Statement s = db.createStatement();
    try {
      s.execute("DROP TABLE sequences");
    }
    catch (Exception e) {
      // Ignore exceptions from the drop
      ;
    }
    JdbcUtil.closeQuietly(s);
  }

  private StaticIdentitySequenceNumberService createSequenceNumberService(DatabaseConnection conn, String uuid, String identity) {
    StaticIdentitySequenceNumberService service = new StaticIdentitySequenceNumberService();
    if (conn != null) {
      service.setConnection(conn);
    }
    service.setIdentity(identity);
    service.setUniqueId(uuid);
    service.setMetadataKey(SequenceNumberCase.DEFAULT_METADATA_KEY);
    service.setNumberFormat(SequenceNumberCase.DEFAULT_NUMBER_FORMAT);
    return service;
  }

  private StaticIdentitySequenceNumberService createSequenceNumberService(DatabaseConnection conn, String identity) {
    return createSequenceNumberService(conn, new GuidGenerator().getUUID(), identity);
  }

  private JdbcConnection createJdbcConnection() {
    return new JdbcConnection(PROPERTIES.getProperty(CFG_JDBC_URL), PROPERTIES.getProperty(CFG_JDBC_DRIVER));
  }

  private int getCurrentSequenceNumber(String id) throws Exception {
    int result = -1;
    Connection c = createConnection();
    PreparedStatement ps = c.prepareStatement("select seq_number from sequences where id=?");
    ResultSet rs = null;
    try {
      ps.setString(1, id);
      rs = ps.executeQuery();
      if (rs.next()) {
        result = rs.getInt(1);
      }
    }
    finally {
      JdbcUtil.closeQuietly(rs);
      JdbcUtil.closeQuietly(ps);
      JdbcUtil.closeQuietly(c);
    }
    return result;
  }

  @Override
  public JdbcServiceList createServiceCollection() {
    return new JdbcServiceList();
  }

  @Override
  public JdbcServiceList createServiceCollection(Collection<Service> services) {
    return new JdbcServiceList(services);
  }

  public JdbcServiceList createServiceCollection(Service... services) {
    return new JdbcServiceList(services);
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "<!-- \n" + "In this example config we want to execute two SequencNumberServices.\n"
        + "Both SequenceNumberServices will use the same underlying java.sql.Connection\n"
        + "which will be provided by JdbcServiceList.\n"
        + "\nAdditionally, our requirements specify that we don't commit our changes until\n"
        + "both sequence numbers have been generated. To do this, we set auto-commit to be false on the\n"
        + "connection; in the event of an exception, the connection will be rolled back to a Savepoint\n"
        + "that was created upon entry to the service-list.\n"
        + "\nIf all services are executed successfully; then the transaction is committed." + "\n-->\n";
  }

  @Override
  protected JdbcServiceList retrieveObjectForSampleConfig() {
    JdbcConnection connection = new JdbcConnection();
    connection.setConnectUrl("jdbc:mysql://localhost:3306/mydatabase");
    connection.setAutoCommit(false);
    connection.setConnectionAttempts(2);
    connection.setConnectionRetryInterval(new TimeInterval(3L, "SECONDS"));
    JdbcServiceList list = new JdbcServiceList();
    list.setDatabaseConnection(connection);
    StaticIdentitySequenceNumberService s1 = new StaticIdentitySequenceNumberService();
    s1.setIdentity("first_id");
    s1.setMetadataKey("sequence_number_1");
    s1.setNumberFormat(SequenceNumberCase.DEFAULT_NUMBER_FORMAT);
    StaticIdentitySequenceNumberService s2 = new StaticIdentitySequenceNumberService();
    s2.setIdentity("another_id");
    s2.setMetadataKey("sequence_number_2");
    s2.setNumberFormat(SequenceNumberCase.DEFAULT_NUMBER_FORMAT);

    list.add(s1);
    list.add(s2);
    return list;
  }

}
