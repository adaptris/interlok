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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.jdbc.JdbcConnection;
import com.adaptris.core.services.jdbc.types.BlobColumnTranslator;
import com.adaptris.core.services.jdbc.types.ClobColumnTranslator;
import com.adaptris.core.services.jdbc.types.DateColumnTranslator;
import com.adaptris.core.services.jdbc.types.IntegerColumnTranslator;
import com.adaptris.core.services.jdbc.types.StringColumnTranslator;
import com.adaptris.core.services.jdbc.types.TimestampColumnTranslator;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.TimeInterval;

@SuppressWarnings("deprecation")
public abstract class JdbcQueryServiceCase extends JdbcServiceExample {

  protected static final String QUERY_SQL = "SELECT adapter_version, message_translator_type, inserted_on, counter"
      + " FROM adapter_type_version " + " WHERE adapter_unique_id = ?";
  private static final String QUERY_SQL_NO_RESULT = "SELECT adapter_version, message_translator_type, inserted_on, counter"
      + " FROM adapter_type_version " + " WHERE adapter_unique_id = 'xxxxxxx'";
  protected static final String QUERY_SQL_MULTI_RESULT = "SELECT adapter_version, message_translator_type, inserted_on, counter"
      + " FROM adapter_type_version";
  private static final String STRING_PAYLOAD = "The Quick Brown Fox Jumps Over The Lazy Dog";
  protected static final String ADAPTER_ID_KEY = "adapter_id";
  protected static final String JDBC_QUERYSERVICE_DRIVER = "jdbc.queryservice.driver";
  protected static final String JDBC_QUERYSERVICE_URL = "jdbc.queryservice.url";
  protected static final String XML_PAYLOAD_PREFIX = "<root><document>";
  protected static final String XML_PAYLOAD_SUFFIX = "</document></root>";

  protected static final String COLUMN_VERSION = "ADAPTER_VERSION";
  protected static final String COLUMN_TYPE = "MESSAGE_TRANSLATOR_TYPE";
  protected static final String COLUMN_INSERTED_ON = "INSERTED_ON";
  protected static final String COLUMN_COUNTER = "COUNTER";

  private enum QueryClasses {
    PayloadQueryColumn {
      @Override
      public StatementParameter create() {
        return new StatementParameter(null, String.class, StatementParameter.QueryType.payload);
      }
    },
    XpathQueryColumn {
      @Override
      public StatementParameter create() {
        return new StatementParameter("/path/to/query/value", String.class, StatementParameter.QueryType.xpath);
      }
    },
    StringQueryColumn {
      @Override
      public StatementParameter create() {
        return new StatementParameter("metadata key", String.class, StatementParameter.QueryType.metadata);
      }
    },
    TimestampQueryColumn {
      @Override
      public TimestampStatementParameter create() {
        return new TimestampStatementParameter("metadata containing a timestamp", StatementParameter.QueryType.metadata,
            new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ssZ"));
      }
    },
    DateQueryColumn {
      @Override
      public DateStatementParameter create() {
        return new DateStatementParameter("/xpath/to/a/date", StatementParameter.QueryType.xpath,
            new SimpleDateFormat("yyyy-MM-dd"));
      }
    },
    TimeQueryColumn {
      @Override
      public TimeStatementParameter create() {
        return new TimeStatementParameter("/xpath/to/a/time", StatementParameter.QueryType.xpath, new SimpleDateFormat("HH:mm:ssZ"));

      }
    },
    IdQueryColumn {
      @Override
      public StatementParameter create() {
        return new StatementParameter(null, String.class, StatementParameter.QueryType.id);
      }
    },
    BooleanQueryColumn {
      @Override
      public BooleanStatementParameter create() {
        return new BooleanStatementParameter("/xpath/to/boolean/value", StatementParameter.QueryType.xpath, null, null);
      }
    },
    DoubleQueryColumn {
      @Override
      public DoubleStatementParameter create() {
        return new DoubleStatementParameter("/xpath/to/double/value", StatementParameter.QueryType.xpath, null, null);
      }
    },
    FloatQueryColumn {
      @Override
      public FloatStatementParameter create() {
        return new FloatStatementParameter("/xpath/to/float/value", StatementParameter.QueryType.xpath, null, null);
      }
    },
    IntegerQueryColumn {
      @Override
      public IntegerStatementParameter create() {
        return new IntegerStatementParameter("metadata-key-containing-an-int", StatementParameter.QueryType.metadata, null, null);
      }
    },
    LongQueryColumn {
      @Override
      public LongStatementParameter create() {
        return new LongStatementParameter("metadata-key-containing-a-long", StatementParameter.QueryType.metadata, null, null);
      }
    },
    ShortQueryColumn {
      @Override
      public ShortStatementParameter create() {
        return new ShortStatementParameter("metadata-key-containing-a-short", StatementParameter.QueryType.metadata, null, null);
      }
    },
    ConstantColumn {
      @Override
      public StatementParameter create() {
        return new StatementParameter("The Constant", String.class, StatementParameter.QueryType.constant);
      }
    }
    ;

    public abstract NamedStatementParameter create();

  }

  public JdbcQueryServiceCase(String arg0) {
    super(arg0);
  }

  protected abstract ResultSetTranslatorImp createTranslatorForConfig();

//  @Override
//  protected Object retrieveObjectForSampleConfig() {
//    JdbcDataQueryService service = new JdbcDataQueryService();
//    try {
//      JdbcConnection connection = new JdbcConnection("jdbc:mysql://localhost:3306/mydatabase", "com.mysql.jdbc.Driver");
//      KeyValuePairSet connectionProperties = new KeyValuePairSet();
//      connectionProperties.add(new KeyValuePair("useCompression", "true"));
//      connection.setConnectionProperties(connectionProperties);
//      connection.setConnectionAttempts(2);
//      connection.setConnectionRetryInterval(new TimeInterval(3L, "SECONDS"));
//      service.setConnection(connection);
//      ResultSetTranslatorImp rst = createTranslatorForConfig();
//      rst.addColumnTranslator(new StringColumnTranslator());
//      rst.addColumnTranslator(new DateColumnTranslator());
//      rst.addColumnTranslator(new IntegerColumnTranslator());
//      rst.addColumnTranslator(new BlobColumnTranslator());
//      rst.addColumnTranslator(new ClobColumnTranslator());
//      service.setResultSetTranslator(rst);
//      String additionalParams = "";
//      boolean first = true;
//      for (QueryClasses qc : QueryClasses.values()) {
//        service.addStatementParameter(qc.create());
//        if (first) {
//          additionalParams += qc.name() + "=?";
//          first = false;
//        }
//        else {
//          additionalParams += " AND " + qc.name() + "=?";
//
//        }
//      }
//      service.setStatement("SELECT StringColumn1, DateColumn2, IntegerColumn3, BlobColumn, ClobColumn FROM tablename WHERE " + additionalParams);
//    }
//    catch (Exception e) {
//      throw new RuntimeException(e);
//    }
//    return service;
//  }
  
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }
  
  protected List<Object> retrieveObjectsForSampleConfig() {
    ArrayList<Object> objects = new ArrayList<>();
    
    JdbcDataQueryService service = new JdbcDataQueryService();
    try {
      JdbcConnection connection = new JdbcConnection("jdbc:mysql://localhost:3306/mydatabase", "com.mysql.jdbc.Driver");
      KeyValuePairSet connectionProperties = new KeyValuePairSet();
      connectionProperties.add(new KeyValuePair("useCompression", "true"));
      connection.setConnectionProperties(connectionProperties);
      connection.setConnectionAttempts(2);
      connection.setConnectionRetryInterval(new TimeInterval(3L, "SECONDS"));
      service.setConnection(connection);
      ResultSetTranslatorImp rst = createTranslatorForConfig();
      rst.addColumnTranslator(new StringColumnTranslator());
      rst.addColumnTranslator(new DateColumnTranslator());
      rst.addColumnTranslator(new IntegerColumnTranslator());
      rst.addColumnTranslator(new BlobColumnTranslator());
      rst.addColumnTranslator(new ClobColumnTranslator());
      service.setResultSetTranslator(rst);
      String additionalParams = "";
      boolean first = true;
      for (QueryClasses qc : QueryClasses.values()) {
        service.addStatementParameter(qc.create());
        if (first) {
          additionalParams += qc.name() + "=?";
          first = false;
        }
        else {
          additionalParams += " AND " + qc.name() + "=?";

        }
      }
      service.setStatement("SELECT StringColumn1, DateColumn2, IntegerColumn3, BlobColumn, ClobColumn FROM tablename WHERE " + additionalParams);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    objects.add(service);
    
    JdbcDataQueryService service2 = new JdbcDataQueryService();
    try {
      JdbcConnection connection = new JdbcConnection("jdbc:mysql://localhost:3306/mydatabase", "com.mysql.jdbc.Driver");
      KeyValuePairSet connectionProperties = new KeyValuePairSet();
      connectionProperties.add(new KeyValuePair("useCompression", "true"));
      connection.setConnectionProperties(connectionProperties);
      connection.setConnectionAttempts(2);
      connection.setConnectionRetryInterval(new TimeInterval(3L, "SECONDS"));
      service2.setConnection(connection);
      ResultSetTranslatorImp rst = createTranslatorForConfig();
      rst.addColumnTranslator(new StringColumnTranslator());
      rst.addColumnTranslator(new DateColumnTranslator());
      rst.addColumnTranslator(new IntegerColumnTranslator());
      rst.addColumnTranslator(new BlobColumnTranslator());
      rst.addColumnTranslator(new ClobColumnTranslator());
      service2.setParameterApplicator(new NamedParameterApplicator());
      service2.setResultSetTranslator(rst);
      String additionalParams = "";
      boolean first = true;
      int count = 0;
      for (QueryClasses qc : QueryClasses.values()) {
        count ++;
        NamedStatementParameter statementParameter = qc.create();
        statementParameter.setName("param" + count);
        service2.addStatementParameter(statementParameter);
        if (first) {
          additionalParams += qc.name() + "=#param" + count;
          first = false;
        }
        else 
          additionalParams += " AND " + qc.name() + "=#param" + count;
      }
      service2.setStatement("SELECT StringColumn1, DateColumn2, IntegerColumn3, BlobColumn, ClobColumn FROM tablename WHERE " + additionalParams);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    objects.add(service2);
    
    return objects;
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!--"
        + "\n\nUse of ColumnTranslators is optional; they will be applied in sequence to the columns that are"
        + "\npresent in the result set. If not present, then the result set translator will attempt to do"
        + "\na best guess conversion to String (which relies heavily on Object#toString())."
        + "\nIf the number of column translators configured does not match the number of columns in the result set"
        + "\nThen where possible, the column translators will be used; otherwise a best guess translation happens." + "\n\n-->\n";
  }

  @Override
  protected String createBaseFileName(Object o) {
    JdbcDataQueryService sc = (JdbcDataQueryService) o;
    String name = super.createBaseFileName(o) + "-" + sc.getResultSetTranslator().getClass().getSimpleName();
    if(sc.getParameterApplicator() instanceof NamedParameterApplicator)
      name = name + "-WithNamedParameterApplicator";
    return name;
  }

  public void testBug1762() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    ResultSetTranslator t = createTranslatorForConfig();
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    msg.setContent(STRING_PAYLOAD, msg.getContentEncoding());
    try {
      execute(s, msg);
      assertFalse(msg.containsKey(JdbcDataQueryService.class.getCanonicalName()));
    }
    catch (ServiceException e) {
      log.error(e.getMessage(), e);
      fail("Service Exception Thrown " + e.getMessage());
    }
  }

  @Override
  public void testBackReferences() throws Exception {
    this.testBackReferences(new JdbcDataQueryService());
  }

  protected static void createDatabase() throws Exception {
    Connection c = null;
    Statement s = null;
    try {
      Class.forName(PROPERTIES.getProperty(JDBC_QUERYSERVICE_DRIVER));
      c = DriverManager.getConnection(PROPERTIES.getProperty(JDBC_QUERYSERVICE_URL));
      c.setAutoCommit(true);
      s = c.createStatement();
      try {
        s.execute("DROP TABLE adapter_type_version");
      }
      catch (Exception e) {
        // Ignore exceptions from the drop
        ;
      }
      s.execute("CREATE TABLE adapter_type_version " + "(adapter_unique_id VARCHAR(128) NOT NULL, "
          + "adapter_version VARCHAR(128) NOT NULL, "
          + "message_translator_type VARCHAR(128) NOT NULL, inserted_on TIMESTAMP, counter SMALLINT)");
    }
    finally {
      JdbcUtil.closeQuietly(s);
      JdbcUtil.closeQuietly(c);
    }
  }

  protected void populateDatabase(List<AdapterTypeVersion> list, boolean doLogging) throws Exception {
    Connection c = null;
    PreparedStatement insert = null;
    PreparedStatement select = null;
    try {
      Class.forName(PROPERTIES.getProperty(JDBC_QUERYSERVICE_DRIVER));
      c = DriverManager.getConnection(PROPERTIES.getProperty(JDBC_QUERYSERVICE_URL));
      c.setAutoCommit(true);
      insert = c.prepareStatement("INSERT INTO adapter_type_version "
          + "(adapter_unique_id, adapter_version, message_translator_type, inserted_on, counter) " + "values (?, ?, ?, ?, ?)");
      select = c.prepareStatement("SELECT * FROM adapter_type_version ");

      for (AdapterTypeVersion atv : list) {
        insert.clearParameters();
        insert.setString(1, atv.getUniqueId());
        insert.setString(2, atv.getVersion());
        insert.setString(3, atv.getTranslatorType());
        insert.setTimestamp(4, new Timestamp(atv.getDate().getTime()));
        insert.setInt(5, atv.getCounter());
        insert.executeUpdate();
      }

      if (doLogging) {
        ResultSet rs = select.executeQuery();
        int rowCount = 0;
        ResultSetMetaData rsmd = rs.getMetaData();
        int colCount = rsmd.getColumnCount();

        while (rs.next()) {
          rowCount++;
          String print = "Row " + rowCount + "=";
          for (int i = 1; i <= colCount; i++) {
            String column = rsmd.getColumnName(i);
            if (!rsmd.getColumnLabel(i).equals(column)) {
              column = rsmd.getColumnLabel(i);
            }
            print += ",[" + column + "]=[" + rs.getString(i) + "]";
          }
          log.debug(print);
        }
        if (rowCount != list.size()) {
          throw new SQLException("Input Output Size mismatch");
        }
      }
    }
    finally {
      JdbcUtil.closeQuietly(insert);
      JdbcUtil.closeQuietly(select);
      JdbcUtil.closeQuietly(c);
    }

  }

  protected static List<AdapterTypeVersion> generate(int max) throws Exception {
    return generate(max, new Date());
  }

  protected static List<AdapterTypeVersion> generate(int max, Date date) throws Exception {
    GuidGenerator guid = new GuidGenerator();
    List<AdapterTypeVersion> result = new ArrayList<AdapterTypeVersion>(max);
    for (int i = 0; i < max; i++) {
      AdapterTypeVersion atv = new AdapterTypeVersion(guid.safeUUID(), guid.safeUUID(), guid.safeUUID(), i);
      atv.setDate(date);
      result.add(atv);
    }
    return result;

  }

  protected static JdbcDataQueryService createConstantService(String constant) {
    JdbcDataQueryService service = new JdbcDataQueryService();
    JdbcConnection connection = new JdbcConnection(PROPERTIES.getProperty(JDBC_QUERYSERVICE_URL),
        PROPERTIES.getProperty(JDBC_QUERYSERVICE_DRIVER));
    service.setConnection(connection);

    service.setStatement(QUERY_SQL);
    StatementParameter sp = new StatementParameter();
    sp.setQueryClass("java.lang.String");
    sp.setQueryType(StatementParameter.QueryType.constant);
    sp.setQueryString(constant);
    service.addStatementParameter(sp);
    return service;
  }

  protected static JdbcDataQueryService createMessageIdService() {
    JdbcDataQueryService service = new JdbcDataQueryService();
    JdbcConnection connection = new JdbcConnection(PROPERTIES.getProperty(JDBC_QUERYSERVICE_URL),
        PROPERTIES.getProperty(JDBC_QUERYSERVICE_DRIVER));
    service.setConnection(connection);
    service.setStatement(QUERY_SQL);
    StatementParameter sp = new StatementParameter();
    sp.setQueryClass("java.lang.String");
    sp.setQueryType(StatementParameter.QueryType.id);
    service.addStatementParameter(sp);
    return service;
  }

  protected static JdbcDataQueryService createMetadataService() {
    return createMetadataService(true);
  }
  
  protected static JdbcDataQueryService createMetadataService(boolean createConnection) {
    JdbcDataQueryService service = new JdbcDataQueryService();
    if (createConnection) {
      JdbcConnection connection = new JdbcConnection(PROPERTIES.getProperty(JDBC_QUERYSERVICE_URL),
          PROPERTIES.getProperty(JDBC_QUERYSERVICE_DRIVER));
      service.setConnection(connection);
    }
    service.setStatement(QUERY_SQL);
    StatementParameter sp = new StatementParameter();
    sp.setQueryClass("java.lang.String");
    sp.setQueryType(StatementParameter.QueryType.metadata);
    sp.setQueryString(ADAPTER_ID_KEY);
    service.addStatementParameter(sp);

    return service;
  }

  protected static JdbcDataQueryService createMetadataServiceNoResult() {
    JdbcDataQueryService service = new JdbcDataQueryService();
    JdbcConnection connection = new JdbcConnection(PROPERTIES.getProperty(JDBC_QUERYSERVICE_URL),
        PROPERTIES.getProperty(JDBC_QUERYSERVICE_DRIVER));
    service.setConnection(connection);

    service.setStatement(QUERY_SQL_NO_RESULT);

    return service;
  }

  protected static JdbcDataQueryService createXmlService() {
    JdbcDataQueryService service = new JdbcDataQueryService();
    JdbcConnection connection = new JdbcConnection(PROPERTIES.getProperty(JDBC_QUERYSERVICE_URL),
        PROPERTIES.getProperty(JDBC_QUERYSERVICE_DRIVER));
    service.setConnection(connection);
    service.setStatement(QUERY_SQL);
    StatementParameter sp = new StatementParameter();
    sp.setQueryClass("java.lang.String");
    sp.setQueryType(StatementParameter.QueryType.xpath);
    sp.setQueryString("/root/document");
    service.addStatementParameter(sp);
    return service;
  }
  
  protected static JdbcDataQueryService createMultiService() {
    JdbcDataQueryService service = new JdbcDataQueryService();
    JdbcConnection connection = new JdbcConnection(PROPERTIES.getProperty(JDBC_QUERYSERVICE_URL),
        PROPERTIES.getProperty(JDBC_QUERYSERVICE_DRIVER));
    service.setConnection(connection);
    service.setStatement(QUERY_SQL_MULTI_RESULT);
    return service;
  }

  protected static void applyColumnTranslators(ResultSetTranslatorImp t) {
    t.addColumnTranslator(new StringColumnTranslator());
    t.addColumnTranslator(new StringColumnTranslator());
    t.addColumnTranslator(new TimestampColumnTranslator());
    t.addColumnTranslator(new IntegerColumnTranslator());
  }

  protected static AdaptrisMessage createMessage(AdapterTypeVersion atv) throws Exception {
    return createMessage(AdaptrisMessageFactory.getDefaultInstance(), atv);
  }

  protected static AdaptrisMessage createMessage(AdaptrisMessageFactory mf, AdapterTypeVersion atv) throws Exception {
    return createMessage(mf, "ISO-8859-1", atv);
  }

  protected static AdaptrisMessage createMessage(AdaptrisMessageFactory mf, String encoding, AdapterTypeVersion atv)
      throws Exception {
    return createMessage(mf, encoding, atv, false);
  }

  protected static AdaptrisMessage createMessage(AdaptrisMessageFactory mf, String encoding, AdapterTypeVersion atv,
                                                 boolean overwriteMsgId) throws Exception {
    AdaptrisMessage msg = null;
    if (encoding == null) {
      msg = mf.newMessage(XML_PAYLOAD_PREFIX + atv.getUniqueId() + XML_PAYLOAD_SUFFIX);
    }
    else {
      msg = mf.newMessage(XML_PAYLOAD_PREFIX + atv.getUniqueId() + XML_PAYLOAD_SUFFIX, encoding);
    }
    msg.addMetadata(ADAPTER_ID_KEY, atv.getUniqueId());
    if (overwriteMsgId) {
      msg.setUniqueId(atv.getUniqueId());
    }
    return msg;
  }

  protected static class AdapterTypeVersion {
    private String uniqueId;
    private String version;
    private String translatorType;
    private Date date;
    private int counter;

    AdapterTypeVersion(String id, String ver, String type, int counter) {
      setUniqueId(id);
      setVersion(ver);
      setTranslatorType(type);
      setDate(new Date());
      setCounter(counter);
    }

    public String getUniqueId() {
      return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
      this.uniqueId = uniqueId;
    }

    public String getVersion() {
      return version;
    }

    public void setVersion(String version) {
      this.version = version;
    }

    public String getTranslatorType() {
      return translatorType;
    }

    public void setTranslatorType(String translatorType) {
      this.translatorType = translatorType;
    }

    public Date getDate() {
      return date;
    }

    public void setDate(Date date) {
      this.date = date;
    }

    public int getCounter() {
      return counter;
    }

    public void setCounter(int counter) {
      this.counter = counter;
    }

  }

}
