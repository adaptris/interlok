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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.jdbc.AdvancedJdbcPooledConnection;
import com.adaptris.core.jdbc.JdbcConnection;
import com.adaptris.core.jdbc.JdbcPooledConnection;
import com.adaptris.core.jdbc.PooledConnectionHelper;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.TimeInterval;

public class JdbcDataCaptureServiceTest extends JdbcServiceExample {
  private static final String METADATA_VALUE = "Any Old Value";
  private static final String METADATA_KEY = "jdbcDataCaptureServiceTest";
  private static final String CONTENT = "Quick zephyrs blow, vexing daft Jim";
  private static final String SUBJECT = "This is the Subject";
  private static final String ATTACHMENT_DATA = "Attachment1";
  private static final String ATTACHMENT_DATA_2 = "Attachment2";

  protected static final String JDBC_CAPTURE_SERVICE_DRIVER = "jdbc.captureservice.driver";
  protected static final String JDBC_CAPTURE_SERVICE_URL = "jdbc.captureservice.url";
  private static final String LF = System.getProperty("line.separator");
  private static final String XML_DOCUMENT = "<?xml version=\"1.0\"?>" + LF + "<document>" + LF + "<subject>" + SUBJECT
      + "</subject>" + LF + "<content>" + CONTENT + "</content>" + LF + "<attachment><data>" + ATTACHMENT_DATA
      + "</data></attachment>" + LF + "<attachment><data>" + ATTACHMENT_DATA + "</data></attachment>" + LF + "</document>";
  private static final String XML_DOCUMENT_2 = "<?xml version=\"1.0\"?>" + LF + "<document>" + LF + "<subject>" + SUBJECT
      + "</subject>" + LF + "<content>" + CONTENT + "</content>" + LF + "<attachment><data>" + ATTACHMENT_DATA
      + "</data></attachment>" + LF + "<attachment><data>" + ATTACHMENT_DATA_2 + "</data></attachment>" + LF + "</document>";

  private static final String XPATH_ITERATE = "/document/attachment";
  private static final String XPATH_ITERATE_RELATIVE = "./data";
  private static final String XPATH_TO_SUBJECT = "/document/subject";

  private enum CaptureClassesForSamples {
    IdCaptureColumn {

      @Override
      public JdbcStatementParameter create() {
        return new StatementParameter(null, String.class.getName(), StatementParameter.QueryType.id);
      }
    },
    MetadataCaptureColumn {

      @Override
      public JdbcStatementParameter create() {
        return new StatementParameter("A Metadata Key", String.class.getName(), StatementParameter.QueryType.metadata);
      }
    },
    XpathCaptureColumn {

      @Override
      public JdbcStatementParameter create() {
        return new StatementParameter("xpath/to/value", String.class.getName(), StatementParameter.QueryType.xpath, true);
      }
    },
    ConstantCaptureColumn {

      @Override
      public JdbcStatementParameter create() {
        return new StatementParameter("The Constant", String.class.getName(), StatementParameter.QueryType.constant);
      }
    },
    PayloadCaptureColumn {

      @Override
      public JdbcStatementParameter create() {
        return new StatementParameter(null, String.class.getName(), StatementParameter.QueryType.payload);
      }
    },
    BooleanCaptureColumn {

      @Override
      public JdbcStatementParameter create() {
        return new BooleanStatementParameter("relative/xpath/to/boolean/value", StatementParameter.QueryType.xpath, null, null);
      }
    },
    DoubleCaptureColumn {

      @Override
      public JdbcStatementParameter create() {
        return new DoubleStatementParameter("relative/xpath/to/double/value", StatementParameter.QueryType.xpath, true, null);
      }
    },
    FloatCaptureColumn {

      @Override
      public JdbcStatementParameter create() {
        return new FloatStatementParameter("relative/xpath/to/float/value", StatementParameter.QueryType.xpath, true, null);
      }
    },
    IntegerCaptureColumn {

      @Override
      public JdbcStatementParameter create() {
        return new IntegerStatementParameter("metadata-key-containing-an-integer", StatementParameter.QueryType.metadata, true,
            null);
      }
    },
    LongCaptureColumn {

      @Override
      public JdbcStatementParameter create() {
        return new LongStatementParameter("metadata-key-containing-a-long", StatementParameter.QueryType.metadata, true, null);
      }
    },
    ShortCaptureColumn {

      @Override
      public JdbcStatementParameter create() {
        return new ShortStatementParameter("metadata-key-containing-a-short", StatementParameter.QueryType.metadata, true, null);
      }
    },
    TimestampCaptureColumn {
      @Override
      public JdbcStatementParameter create() {
        return new TimestampStatementParameter("relative/xpath/to/timestamp/value", StatementParameter.QueryType.xpath, true, null,
            new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ssZ"));
      }
    },
    DateCaptureColumn {

      @Override
      public JdbcStatementParameter create() {
        return new DateStatementParameter("relative/xpath/to/date/value", StatementParameter.QueryType.xpath, true, null,
            new SimpleDateFormat("yyyy-MM-dd"));
      }
    },
    TimeCaptureColumn {

      @Override
      public JdbcStatementParameter create() {
        return new TimeStatementParameter("relative/xpath/to/time/value", StatementParameter.QueryType.xpath, true, null,
            new SimpleDateFormat("HH:mm:ssZ"));
      }
    };

    public abstract JdbcStatementParameter create();

  }

  public JdbcDataCaptureServiceTest(String arg0) {
    super(arg0);
  }

  protected Object retrieveObjectForSampleConfig() {
    return null;
  }
  
  @Override
  protected List<Object> retrieveObjectsForSampleConfig() {
    List<Object> returnedObjects = new ArrayList<>();
    
    JdbcDataCaptureService service = new JdbcDataCaptureService();
    JdbcConnection connection = new JdbcConnection();
    connection.setConnectUrl("jdbc:mysql://localhost:3306/mydatabase");
    connection.setConnectionAttempts(2);
    connection.setConnectionRetryInterval(new TimeInterval(3L, "SECONDS"));
    service.setConnection(connection);
    service.setIterates(true);
    service.setIterationXpath("/xpath/to/repeating/element");
    String columns = "";
    String values = "";
    for (CaptureClassesForSamples qc : CaptureClassesForSamples.values()) {
      service.addStatementParameter(qc.create());
      columns += qc.name() + ", ";
      values += "?, ";
    }
    columns = columns.substring(0, columns.lastIndexOf(','));
    values = values.substring(0, values.lastIndexOf(','));
    service.setStatement("insert into mytable (" + columns + ") values (" + values + ");");
    
    JdbcDataCaptureService service2 = new JdbcDataCaptureService();
    JdbcConnection connection2 = new JdbcConnection();
    connection2.setConnectUrl("jdbc:mysql://localhost:3306/mydatabase");
    connection2.setConnectionAttempts(2);
    connection2.setConnectionRetryInterval(new TimeInterval(3L, "SECONDS"));
    service2.setConnection(connection2);
    service2.setIterates(true);
    service2.setIterationXpath("/xpath/to/repeating/element");
    service2.setParameterApplicator(new NamedParameterApplicator());
    String columns2 = "";
    String values2 = "";
    int paramCount = 0;
    for (CaptureClassesForSamples qc : CaptureClassesForSamples.values()) {
      paramCount ++;
      NamedStatementParameter jdbcStatementParameter = (NamedStatementParameter) qc.create();
      jdbcStatementParameter.setName("param" + paramCount);
      service2.addStatementParameter(jdbcStatementParameter);
      columns2 += qc.name() + ", ";
      values2 += "#param" + paramCount + ", ";
    }
    columns2 = columns2.substring(0, columns2.lastIndexOf(','));
    values2 = values2.substring(0, values2.lastIndexOf(','));
    service2.setStatement("insert into mytable (" + columns2 + ") values (" + values2 + ");");
    
    returnedObjects.add(service);
    returnedObjects.add(service2);
    
    return returnedObjects;
  }
  
  @Override
  protected String createBaseFileName(Object o) {
    JdbcDataCaptureService sc = (JdbcDataCaptureService) o;
    String name = super.createBaseFileName(o);
    if(sc.getParameterApplicator() instanceof NamedParameterApplicator)
      name = name + "-WithNamedParameterApplicator";
    return name;
  }

  public void testSetNamespaceContext() throws Exception {
    JdbcDataCaptureService service = new JdbcDataCaptureService();
    assertNull(service.getNamespaceContext());
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.add(new KeyValuePair("hello", "world"));
    service.setNamespaceContext(kvps);
    assertEquals(kvps, service.getNamespaceContext());
    service.setNamespaceContext(null);
    assertNull(service.getNamespaceContext());
  }

  public void testService() throws Exception {
    createDatabase();
    JdbcDataCaptureService service = createBasicService();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_DOCUMENT);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    execute(service, msg);
    doBasicCaptureAsserts(1);
  }
  
  public void testServiceSequentialParameterApplicator() throws Exception {
    createDatabase();    
    JdbcDataCaptureService service = createBasicService();
    service.setParameterApplicator(new SequentialParameterApplicator()); // default anyway, so identical to the test above, but good to be explicit in case the default changes.
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_DOCUMENT);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    execute(service, msg);
    doBasicCaptureAsserts(1);
  }
  
  public void testServiceNamedParameterApplicator() throws Exception {
    createDatabase();
    
    StatementParameterList statementParameterList = new StatementParameterList();
    statementParameterList.add(new StatementParameter("/document/content", String.class.getName(), StatementParameter.QueryType.xpath, null, "param3"));
    statementParameterList.add(new StatementParameter(null, String.class.getName(), StatementParameter.QueryType.payload, null, "param2"));
    statementParameterList.add(new StatementParameter(METADATA_KEY, String.class.getName(), StatementParameter.QueryType.metadata, null, "param1"));
    
    String statement = "insert into jdbc_data_capture_basic (metadata_value, payload_value, xpath_value) values (#param1, #param2, #param3)";
    
    JdbcDataCaptureService service = createBasicService(true, statementParameterList, statement);
    service.setParameterApplicator(new NamedParameterApplicator());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_DOCUMENT);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    execute(service, msg);
    doBasicCaptureAsserts(1);
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
        JdbcDataCaptureService service = createBasicService(false);
        service.setConnection(conn);
        serviceList.add(service);
        start(service);
      }
      assertEquals(0, conn.currentBusyConnectionCount());
      PooledConnectionHelper.executeTest(serviceList, iterations, new PooledConnectionHelper.MessageCreator() {

        @Override
        public AdaptrisMessage createMsgForPooledConnectionTest() {
          AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_DOCUMENT);
          msg.addMetadata(METADATA_KEY, METADATA_VALUE);
          return msg;
        }
      });
      assertEquals(0, conn.currentBusyConnectionCount());
      assertEquals(poolsize, conn.currentIdleConnectionCount());
      assertEquals(poolsize, conn.currentConnectionCount());
      doBasicCaptureAsserts(maxServices * iterations);
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
        JdbcDataCaptureService service = createBasicService(false);
        service.setConnection(conn);
        serviceList.add(service);
        start(service);
      }
      assertEquals(0, conn.currentBusyConnectionCount());
      PooledConnectionHelper.executeTest(serviceList, iterations, new PooledConnectionHelper.MessageCreator() {

        @Override
        public AdaptrisMessage createMsgForPooledConnectionTest() {
          AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_DOCUMENT);
          msg.addMetadata(METADATA_KEY, METADATA_VALUE);
          return msg;
        }
      });
      assertEquals(0, conn.currentBusyConnectionCount());
      assertEquals(poolsize, conn.currentIdleConnectionCount());
      assertEquals(poolsize, conn.currentConnectionCount());
      doBasicCaptureAsserts(maxServices * iterations);
    }
    finally {
      stop(serviceList.toArray(new ComponentLifecycle[0]));
      Thread.currentThread().setName(name);
    }
  }

  private void doBasicCaptureAsserts(int expectedCount) throws Exception {
    Connection c = null;
    PreparedStatement p = null;
    try {
      c = createConnection();
      p = c.prepareStatement("SELECT * FROM jdbc_data_capture_basic");
      ResultSet rs = p.executeQuery();
      int count = 0;
      while (rs.next()) {
        count++;
        assertEquals(XML_DOCUMENT, rs.getString("payload_value"));
        assertEquals(METADATA_VALUE, rs.getString("metadata_value"));
        assertEquals(CONTENT, rs.getString("xpath_value"));
      }
      assertEquals(expectedCount, count);
      JdbcUtil.closeQuietly(rs);
    }
    finally {
      JdbcUtil.closeQuietly(p);
      JdbcUtil.closeQuietly(c);
    }
  }
  
  public void testServiceSaveKeys() throws Exception {
    createDatabase();
    JdbcDataCaptureService service = createSaveKeysService();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_DOCUMENT);
    execute(service, msg);
    Connection c = null;
    PreparedStatement p = null;
    try {
      c = createConnection();
      p = c.prepareStatement("SELECT * FROM jdbc_data_capture_savekeys");
      ResultSet rs = p.executeQuery();
      assertEquals(true, rs.next());
      assertEquals(SUBJECT, rs.getString("xpath_value"));
    }
    finally {
      JdbcUtil.closeQuietly(p);
      JdbcUtil.closeQuietly(c);
    }
    log.debug(msg);
    // this is a pecularity of derby I think, the generatedKeys() returns a
    // resultsetMetadata where columnLabel and columnName =1;
    assertTrue(msg.headersContainsKey("1"));
  }

  public void testIteratesService() throws Exception {
    createDatabase();
    JdbcDataCaptureService service = createIteratesService();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_DOCUMENT_2);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    execute(service, msg);
    Connection c = null;
    PreparedStatement p = null;
    try {
      c = createConnection();
      p = c.prepareStatement("SELECT * FROM jdbc_data_capture_iteration");
      ResultSet rs = p.executeQuery();
      int count = 0;
      while (rs.next()) {
        count += 1;
        assertEquals(XML_DOCUMENT_2, rs.getString("payload_value"));
        if (count == 1) assertEquals(ATTACHMENT_DATA, rs.getString("xpath_value"));
        else
          assertEquals(ATTACHMENT_DATA_2, rs.getString("xpath_value"));
      }
      assertEquals(2, count);
    }
    finally {
      JdbcUtil.closeQuietly(p);
      JdbcUtil.closeQuietly(c);
    }
  }

  public void testServiceInvalidType() throws Exception {
    createDatabase();
    JdbcDataCaptureService service = createBasicService();
    service.addStatementParameter(new StatementParameter(null, String.class, null));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_DOCUMENT);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException e) {
      assertNotNull(e.getCause());
      assertEquals(IllegalArgumentException.class, e.getCause().getClass());
      // expected service exception;
    }
  }

  public void testServiceNonXml() throws Exception {
    createDatabase();
    JdbcDataCaptureService service = createSaveKeysService();
    service.getStatementParameters().clear();
    service.addStatementParameter(new StatementParameter(null, String.class, StatementParameter.QueryType.payload));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SUBJECT);
    execute(service, msg);
    Connection c = null;
    PreparedStatement p = null;
    try {
      c = createConnection();
      p = c.prepareStatement("SELECT * FROM jdbc_data_capture_savekeys");
      ResultSet rs = p.executeQuery();
      assertEquals(true, rs.next());
      assertEquals(SUBJECT, rs.getString("xpath_value"));
    }
    finally {
      JdbcUtil.closeQuietly(p);
      JdbcUtil.closeQuietly(c);
    }
  }

  @Override
  public void testBackReferences() throws Exception {
    this.testBackReferences(new JdbcDataCaptureService());
  }

  private JdbcDataCaptureService createBasicService() throws Exception {
    return createBasicService(true);
  }

  private JdbcDataCaptureService createBasicService(boolean createConnection) throws Exception {
    StatementParameterList statementParameterList = new StatementParameterList();
    statementParameterList.add(new StatementParameter(METADATA_KEY, String.class, StatementParameter.QueryType.metadata));
    statementParameterList.add(new StatementParameter(null, String.class, StatementParameter.QueryType.payload));
    statementParameterList.add(new StatementParameter("/document/content", String.class, StatementParameter.QueryType.xpath));

    return createBasicService(createConnection, statementParameterList);
  }
  
  private JdbcDataCaptureService createBasicService(boolean createConnection, StatementParameterList parameterList) throws Exception {
    String statement = "insert into jdbc_data_capture_basic (metadata_value, payload_value, xpath_value) values (?, ?, ?)";

    return createBasicService(createConnection, parameterList, statement);
  }
  
  private JdbcDataCaptureService createBasicService(boolean createConnection, StatementParameterList parameterList, String statement) throws Exception {
    JdbcDataCaptureService service = new JdbcDataCaptureService();
    if (createConnection) {
      JdbcConnection connection = new JdbcConnection();
      connection.setConnectUrl(PROPERTIES.getProperty(JDBC_CAPTURE_SERVICE_URL));
      connection.setDriverImp(PROPERTIES.getProperty(JDBC_CAPTURE_SERVICE_DRIVER));
      service.setConnection(connection);
    }
    service.setStatementParameters(parameterList);
    service.setStatement(statement);

    return service;
  }

  private JdbcDataCaptureService createIteratesService() throws Exception {
    JdbcDataCaptureService service = new JdbcDataCaptureService();
    JdbcConnection connection = new JdbcConnection();
    connection.setConnectUrl(PROPERTIES.getProperty(JDBC_CAPTURE_SERVICE_URL));
    connection.setDriverImp(PROPERTIES.getProperty(JDBC_CAPTURE_SERVICE_DRIVER));
    service.setConnection(connection);
    service.setIterates(true);
    service.setIterationXpath(XPATH_ITERATE);
    service.addStatementParameter(new StatementParameter(null, String.class, StatementParameter.QueryType.payload));

    service.addStatementParameter(new StatementParameter(XPATH_ITERATE_RELATIVE, String.class, StatementParameter.QueryType.xpath));

    service.setStatement("insert into jdbc_data_capture_iteration (payload_value, xpath_value) values (?, ?)");

    return service;
  }

  private JdbcDataCaptureService createSaveKeysService() throws Exception {
    JdbcDataCaptureService service = new JdbcDataCaptureService();
    JdbcConnection connection = new JdbcConnection();
    connection.setConnectUrl(PROPERTIES.getProperty(JDBC_CAPTURE_SERVICE_URL));
    connection.setDriverImp(PROPERTIES.getProperty(JDBC_CAPTURE_SERVICE_DRIVER));
    service.setConnection(connection);
    service.addStatementParameter(new StatementParameter(XPATH_TO_SUBJECT, String.class, StatementParameter.QueryType.xpath));

    service.setStatement("insert into jdbc_data_capture_savekeys (xpath_value) values (?)");
    service.setSaveReturnedKeys(true);
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
      executeQuietly(s, "DROP TABLE jdbc_data_capture_basic");
      executeQuietly(s, "DROP TABLE jdbc_data_capture_iteration");
      executeQuietly(s, "DROP TABLE jdbc_data_capture_savekeys");
      s.execute("CREATE TABLE jdbc_data_capture_basic " + "(metadata_value VARCHAR(128) NOT NULL, "
          + "payload_value CLOB NOT NULL, " + " xpath_value VARCHAR(128) NOT NULL)");
      s.execute("CREATE TABLE jdbc_data_capture_iteration " + "(payload_value CLOB NOT NULL, xpath_value VARCHAR(128) NOT NULL)");
      s.execute("CREATE TABLE jdbc_data_capture_savekeys ("
          + "mykey INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
          + "xpath_value VARCHAR(128) NOT NULL)");
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
