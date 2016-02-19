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

package com.adaptris.core.services.jdbc.raw;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.Service;
import com.adaptris.core.jdbc.AdvancedJdbcPooledConnection;
import com.adaptris.core.jdbc.JdbcConnection;
import com.adaptris.core.jdbc.JdbcPooledConnection;
import com.adaptris.core.jdbc.PooledConnectionHelper;
import com.adaptris.core.services.jdbc.BinaryStreamStatementParameter;
import com.adaptris.core.services.jdbc.BytePayloadStatementParameter;
import com.adaptris.core.services.jdbc.CharacterStreamStatementParameter;
import com.adaptris.core.services.jdbc.DateStatementParameter;
import com.adaptris.core.services.jdbc.JdbcServiceExample;
import com.adaptris.core.services.jdbc.NamedParameterApplicator;
import com.adaptris.core.services.jdbc.StatementParameter;
import com.adaptris.core.services.jdbc.TimeStatementParameter;
import com.adaptris.core.services.jdbc.TimestampStatementParameter;
import com.adaptris.core.services.metadata.AddTimestampMetadataService;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.util.TimeInterval;

public class JdbcRawDataCaptureServiceTest extends JdbcServiceExample {
  private static final String CONSTANT_VALUE = "Some Constant";
  private static final String METADATA_VALUE = "Any Old Value";
  private static final String METADATA_KEY = "jdbcRawDataCaptureServiceTest";
  private static final String CONTENT = "Quick zephyrs blow, vexing daft Jim";

  protected static final String JDBC_CAPTURE_SERVICE_DRIVER = "jdbc.captureservice.driver";
  protected static final String JDBC_CAPTURE_SERVICE_URL = "jdbc.captureservice.url";

  public JdbcRawDataCaptureServiceTest(String arg0) {
    super(arg0);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    JdbcRawDataCaptureService service = createService();
    JdbcConnection connection = new JdbcConnection();
    connection.setConnectUrl("jdbc:mysql://localhost:3306/mydatabase");
    connection.setConnectionAttempts(2);
    connection.setConnectionRetryInterval(new TimeInterval(3L, "SECONDS"));
    service.setConnection(connection);
    return service;
  }

  public void testService() throws Exception {
    createDatabase();
    JdbcRawDataCaptureService service = createService();
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    doBasicCaptureAsserts(1);
  }

  private void doBasicCaptureAsserts(int expectedCount) throws Exception {
    Connection c = null;
    PreparedStatement p = null;
    try {
      c = createConnection();
      p = c.prepareStatement("SELECT * FROM jdbc_raw_data_capture_basic");
      ResultSet rs = p.executeQuery();
      int count = 0;
      while (rs.next()) {
        count++;
        assertEquals(CONTENT, rs.getString("payload_clob"));
        assertEquals(CONTENT, rs.getString("payload_clob2"));
        assertNotNull(rs.getBytes("payload_blob"));
        assertNotNull(rs.getBytes("payload_blob2"));
        assertNotNull(rs.getTime("time_Value"));
        assertTrue(rs.getDate("date_value").after(onceUponATime()));
        assertTrue(rs.getTimestamp("timestamp_value").after(onceUponATime()));
        assertEquals(METADATA_VALUE, rs.getString("string_value"));
        assertEquals(CONSTANT_VALUE, rs.getString("constant_value"));
      }
      assertEquals(expectedCount, count);
      JdbcUtil.closeQuietly(rs);
    }
    finally {
      JdbcUtil.closeQuietly(p);
      JdbcUtil.closeQuietly(c);
    }
  }

  public void testService_PooledConnection() throws Exception {
    int maxServices = 5;
    final int iterations = 5;
    int poolsize = maxServices - 1;

    createDatabase();
    List<Service> serviceList = new ArrayList<Service>();
    String name = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    JdbcPooledConnection conn = PooledConnectionHelper.createPooledConnection(PROPERTIES.getProperty(JDBC_CAPTURE_SERVICE_DRIVER),
        PROPERTIES.getProperty(JDBC_CAPTURE_SERVICE_URL), poolsize);

    try {
      for (int i = 0; i < maxServices; i++) {
        JdbcRawDataCaptureService service = createService(false);
        service.setConnection(conn);
        serviceList.add(service);
        start(service);
      }
      assertEquals(0, conn.currentBusyConnectionCount());
      PooledConnectionHelper.executeTest(serviceList, iterations, new PooledConnectionHelper.MessageCreator() {

        @Override
        public AdaptrisMessage createMsgForPooledConnectionTest() throws Exception {
          return createMessage();
        }
      });
      assertEquals(0, conn.currentBusyConnectionCount());
      assertEquals(poolsize, conn.currentIdleConnectionCount());
      assertEquals(poolsize, conn.currentConnectionCount());
      doBasicCaptureAsserts(iterations * maxServices);
    }
    finally {
      stop(serviceList.toArray(new ComponentLifecycle[0]));
      Thread.currentThread().setName(name);
    }
  }
  
  public void testService_AdvancedPooledConnection() throws Exception {
    int maxServices = 5;
    final int iterations = 5;
    int poolsize = maxServices - 1;

    createDatabase();
    List<Service> serviceList = new ArrayList<Service>();
    String name = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    AdvancedJdbcPooledConnection conn = PooledConnectionHelper.createAdvancedPooledConnection(PROPERTIES.getProperty(JDBC_CAPTURE_SERVICE_DRIVER),
        PROPERTIES.getProperty(JDBC_CAPTURE_SERVICE_URL), poolsize);

    try {
      for (int i = 0; i < maxServices; i++) {
        JdbcRawDataCaptureService service = createService(false);
        service.setConnection(conn);
        serviceList.add(service);
        start(service);
      }
      assertEquals(0, conn.currentBusyConnectionCount());
      PooledConnectionHelper.executeTest(serviceList, iterations, new PooledConnectionHelper.MessageCreator() {

        @Override
        public AdaptrisMessage createMsgForPooledConnectionTest() throws Exception {
          return createMessage();
        }
      });
      assertEquals(0, conn.currentBusyConnectionCount());
      assertEquals(poolsize, conn.currentIdleConnectionCount());
      assertEquals(poolsize, conn.currentConnectionCount());
      doBasicCaptureAsserts(iterations * maxServices);
    }
    finally {
      stop(serviceList.toArray(new ComponentLifecycle[0]));
      Thread.currentThread().setName(name);
    }
  }

  public void testServiceWithUniqueId() throws Exception {
    createDatabase();
    JdbcRawDataCaptureService service = createService();
    service.setUniqueId("testServiceWithUniqueId");
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    Connection c = null;
    PreparedStatement p = null;
    try {
      c = createConnection();
      p = c.prepareStatement("SELECT * FROM jdbc_raw_data_capture_basic");
      ResultSet rs = p.executeQuery();
      assertEquals(true, rs.next());
      assertEquals(CONTENT, rs.getString("payload_clob"));
      assertEquals(CONTENT, rs.getString("payload_clob2"));
      assertNotNull(rs.getBytes("payload_blob"));
      assertNotNull(rs.getBytes("payload_blob2"));
      assertNotNull(rs.getTime("time_Value"));
      assertTrue(rs.getDate("date_value").after(onceUponATime()));
      assertTrue(rs.getTimestamp("timestamp_value").after(onceUponATime()));
      assertEquals(METADATA_VALUE, rs.getString("string_value"));
      assertEquals(CONSTANT_VALUE, rs.getString("constant_value"));
      assertEquals(msg.getUniqueId(), rs.getString("id_value"));
    }
    finally {
      JdbcUtil.closeQuietly(p);
      JdbcUtil.closeQuietly(c);
    }
  }

  public void testServiceWithNamedParameters() throws Exception {
    createDatabase();
    JdbcRawDataCaptureService service = createServiceNamedParameters(true);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    doBasicCaptureAsserts(1);
  }
  
  @Override
  public void testBackReferences() throws Exception {
    this.testBackReferences(new JdbcRawDataCaptureService());
  }

  private Date onceUponATime() {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_YEAR, 0 - (new Random().nextInt(10) + 1));
    return cal.getTime();
  }

  private AdaptrisMessage createMessage() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CONTENT);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    execute(new AddTimestampMetadataService(), msg);
    return msg;
  }

  private JdbcRawDataCaptureService createService() {
    return createService(true);
  }

  private JdbcRawDataCaptureService createService(boolean createConnection) {
    JdbcRawDataCaptureService service = new JdbcRawDataCaptureService();
    if (createConnection) {
      JdbcConnection connection = new JdbcConnection();
      connection.setConnectUrl(PROPERTIES.getProperty(JDBC_CAPTURE_SERVICE_URL));
      connection.setDriverImp(PROPERTIES.getProperty(JDBC_CAPTURE_SERVICE_DRIVER));
      service.setConnection(connection);
    }
    service.addStatementParameter(new StatementParameter(METADATA_KEY, "java.lang.String", StatementParameter.QueryType.metadata));
    service.addStatementParameter(new DateStatementParameter("timestamp", StatementParameter.QueryType.metadata,
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")));
    service.addStatementParameter(new TimestampStatementParameter("timestamp", StatementParameter.QueryType.metadata,
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")));
    service.addStatementParameter(new TimeStatementParameter("timestamp", StatementParameter.QueryType.metadata,
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")));
    service.addStatementParameter(new StatementParameter(null, "java.lang.String", StatementParameter.QueryType.payload));
    service
        .addStatementParameter(new BytePayloadStatementParameter(null, "java.lang.String", StatementParameter.QueryType.payload));
    service.addStatementParameter(new CharacterStreamStatementParameter());
    service.addStatementParameter(new BinaryStreamStatementParameter());
    service.addStatementParameter(new StatementParameter(null, "java.lang.String", StatementParameter.QueryType.id));
    service
        .addStatementParameter(new StatementParameter(CONSTANT_VALUE, "java.lang.String", StatementParameter.QueryType.constant));
    
    service
        .setStatement("insert into jdbc_raw_data_capture_basic "
            + "(string_value, date_value, timestamp_value, time_value, payload_clob, payload_blob, payload_clob2, payload_blob2, id_value, constant_value) "
            + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
    return service;
  }
  
  private JdbcRawDataCaptureService createServiceNamedParameters(boolean createConnection) {
    JdbcRawDataCaptureService service = new JdbcRawDataCaptureService();
    if (createConnection) {
      JdbcConnection connection = new JdbcConnection();
      connection.setConnectUrl(PROPERTIES.getProperty(JDBC_CAPTURE_SERVICE_URL));
      connection.setDriverImp(PROPERTIES.getProperty(JDBC_CAPTURE_SERVICE_DRIVER));
      service.setConnection(connection);
    }
    
    service.setParameterApplicator(new NamedParameterApplicator());
    
    StatementParameter param1 = new StatementParameter(METADATA_KEY, "java.lang.String", StatementParameter.QueryType.metadata);
    param1.setName("param1");
    service.addStatementParameter(param1);
    DateStatementParameter param2 = new DateStatementParameter("timestamp", StatementParameter.QueryType.metadata,
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
    param2.setName("param2");
    service.addStatementParameter(param2);
    TimestampStatementParameter param3 = new TimestampStatementParameter("timestamp", StatementParameter.QueryType.metadata,
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
    param3.setName("param3");
    service.addStatementParameter(param3);
    TimeStatementParameter param4 = new TimeStatementParameter("timestamp", StatementParameter.QueryType.metadata,
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
    param4.setName("param4");
    service.addStatementParameter(param4);
    StatementParameter param5 = new StatementParameter(null, "java.lang.String", StatementParameter.QueryType.payload);
    param5.setName("param5");
    service.addStatementParameter(param5);
    BytePayloadStatementParameter param6 = new BytePayloadStatementParameter(null, "java.lang.String",
        StatementParameter.QueryType.payload);
    param6.setName("param6");
    service.addStatementParameter(param6);
    CharacterStreamStatementParameter param7 = new CharacterStreamStatementParameter();
    param7.setName("param7");
    service.addStatementParameter(param7);
    BinaryStreamStatementParameter param8 = new BinaryStreamStatementParameter();
    param8.setName("param8");
    service.addStatementParameter(param8);
    StatementParameter param9 = new StatementParameter(null, "java.lang.String", StatementParameter.QueryType.id);
    param9.setName("param9");
    service.addStatementParameter(param9);
    StatementParameter param10 = new StatementParameter(CONSTANT_VALUE, "java.lang.String", StatementParameter.QueryType.constant);
    param10.setName("param10");
    service.addStatementParameter(param10);
    
    service
        .setStatement("insert into jdbc_raw_data_capture_basic "
            + "(string_value, date_value, timestamp_value, time_value, payload_clob, payload_blob, payload_clob2, payload_blob2, id_value, constant_value) "
            + "values (#param1, #param2, #param3, #param4, #param5, #param6, #param7, #param8, #param9, #param10)");
    return service;
  }
  
  private static Connection createConnection() throws Exception {
    Connection c = null;
    Class.forName(PROPERTIES.getProperty(JDBC_CAPTURE_SERVICE_DRIVER));
    c = DriverManager.getConnection(PROPERTIES.getProperty(JDBC_CAPTURE_SERVICE_URL));
    c.setAutoCommit(true);
    return c;
  }

  private static void createDatabase() throws Exception {
    Connection c = null;
    Statement s = null;
    try {
      c = createConnection();
      s = c.createStatement();
      executeQuietly(s, "DROP TABLE jdbc_raw_data_capture_basic");
      s.execute("CREATE TABLE jdbc_raw_data_capture_basic " + "(" + "string_value VARCHAR(128) NOT NULL, "
          + "date_value DATE NOT NULL," + "timestamp_value TIMESTAMP NOT NULL," + "time_value TIME NOT NULL,"
          + "payload_clob CLOB NOT NULL, " + "payload_blob BLOB NOT NULL, " + "payload_clob2 CLOB NOT NULL, "
          + "payload_blob2 BLOB NOT NULL, " + "id_value VARCHAR(128) NOT NULL, "
          + "constant_value VARCHAR(128) NOT NULL"
          + ")");
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
