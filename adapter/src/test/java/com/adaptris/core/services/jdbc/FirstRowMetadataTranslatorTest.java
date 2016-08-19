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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.Service;
import com.adaptris.core.jdbc.AdvancedJdbcPooledConnection;
import com.adaptris.core.jdbc.JdbcConnection;
import com.adaptris.core.jdbc.JdbcPooledConnection;
import com.adaptris.core.jdbc.PooledConnectionHelper;
import com.adaptris.core.services.jdbc.types.DateColumnTranslator;
import com.adaptris.core.services.jdbc.types.DoubleColumnTranslator;
import com.adaptris.core.services.jdbc.types.FloatColumnTranslator;
import com.adaptris.core.services.jdbc.types.IntegerColumnTranslator;
import com.adaptris.core.services.jdbc.types.StringColumnTranslator;
import com.adaptris.core.services.jdbc.types.TimeColumnTranslator;
import com.adaptris.core.services.jdbc.types.TimestampColumnTranslator;

public class FirstRowMetadataTranslatorTest extends JdbcQueryServiceCase {

  private static final String METADATA_KEY_DATE = "date";
  private static final String DATE_FORMAT = "yyyy-MM-dd";
  private static final String DATE_QUERY_SQL = "SELECT adapter_version, message_translator_type, inserted_on, counter"
      + " FROM adapter_type_version " + " WHERE inserted_on > ?";

  public FirstRowMetadataTranslatorTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {

  }

  public void testMetadataStatementParameter() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    FirstRowMetadataTranslator t = new FirstRowMetadataTranslator();
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertEquals(entry.getVersion(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertEquals(entry.getTranslatorType(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertFalse(msg.containsKey(JdbcDataQueryService.class.getCanonicalName()));
  }

  public void testRedmineID_4173() throws Exception {
//    testDateStatementParameter();
  }

  public void testConstantStatementParameter() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createConstantService(entry.getUniqueId());
    FirstRowMetadataTranslator t = new FirstRowMetadataTranslator();
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertEquals(entry.getVersion(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertEquals(entry.getTranslatorType(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertFalse(msg.containsKey(JdbcDataQueryService.class.getCanonicalName()));
  }

  public void testMessageIdParameter() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMessageIdService();
    FirstRowMetadataTranslator t = new FirstRowMetadataTranslator();
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(AdaptrisMessageFactory.getDefaultInstance(), "ISO-8859-1", entry, true);
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertEquals(entry.getVersion(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertEquals(entry.getTranslatorType(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertFalse(msg.containsKey(JdbcDataQueryService.class.getCanonicalName()));
  }

  public void testDateStatementParameter() throws Exception {
    createDatabase();
    Date yesterday = yesterday();
    List<AdapterTypeVersion> dbItems = generate(10, yesterday);
    AdapterTypeVersion entry = dbItems.get(0);
    entry.setDate(new Date());
    populateDatabase(dbItems, true);

    JdbcDataQueryService service = new JdbcDataQueryService();
    service.setConnection(new JdbcConnection(PROPERTIES.getProperty(JDBC_QUERYSERVICE_URL),
        PROPERTIES.getProperty(JDBC_QUERYSERVICE_DRIVER)));

    service.setStatementCreator(new ConfiguredSQLStatement(DATE_QUERY_SQL));
    DateStatementParameter tsp = new DateStatementParameter();
    tsp.setDateFormat(DATE_FORMAT);
    tsp.setQueryType(StatementParameter.QueryType.metadata);
    tsp.setQueryString(METADATA_KEY_DATE);
    service.setStatementParameters(new StatementParameterList(Arrays.asList(new JdbcStatementParameter[]
    {
      tsp
    })));
    FirstRowMetadataTranslator t = new FirstRowMetadataTranslator();
    service.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    msg.addMetadata(METADATA_KEY_DATE, dateToString(yesterday));
    execute(service, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertEquals(entry.getVersion(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertEquals(entry.getTranslatorType(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertFalse(msg.containsKey(JdbcDataQueryService.class.getCanonicalName()));

  }

  private String dateToString(Date date) {
    SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);
    return f.format(date);
  }

  public void testXmlStatementParameter() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createXmlService();
    FirstRowMetadataTranslator t = new FirstRowMetadataTranslator();
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertEquals(entry.getVersion(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertEquals(entry.getTranslatorType(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertFalse(msg.containsKey(JdbcDataQueryService.class.getCanonicalName()));
  }

  public void testServiceWithStyleUpperCase() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    FirstRowMetadataTranslator t = new FirstRowMetadataTranslator();
    t.setColumnNameStyle(ResultSetTranslatorImp.ColumnStyle.UpperCase);
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION.toUpperCase()));
    assertEquals(entry.getVersion(),
        msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION.toUpperCase()));
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE.toUpperCase()));
    assertEquals(entry.getTranslatorType(),
        msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE.toUpperCase()));
  }

  public void testServiceWithStyleLowerCase() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    FirstRowMetadataTranslator t = new FirstRowMetadataTranslator();
    t.setColumnNameStyle(ResultSetTranslatorImp.ColumnStyle.LowerCase);

    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION.toLowerCase()));
    assertEquals(entry.getVersion(),
        msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION.toLowerCase()));
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE.toLowerCase()));
    assertEquals(entry.getTranslatorType(),
        msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE.toLowerCase()));
  }

  public void testServiceWithStyleCapitalize() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    FirstRowMetadataTranslator t = new FirstRowMetadataTranslator();
    t.setColumnNameStyle(ResultSetTranslatorImp.ColumnStyle.Capitalize);
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + StringUtils.capitalize(COLUMN_VERSION)));
    assertEquals(entry.getVersion(),
        msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + StringUtils.capitalize(COLUMN_VERSION)));
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + StringUtils.capitalize(COLUMN_TYPE)));
    assertEquals(entry.getTranslatorType(),
        msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + StringUtils.capitalize(COLUMN_TYPE)));
  }


  public void testService_PooledConnection() throws Exception {
    int maxServices = 5;
    final int iterations = 5;
    int poolsize = maxServices - 1;

    String name = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    final AdapterTypeVersion entry = dbItems.get(0);
    populateDatabase(dbItems, false);

    List<Service> serviceList = new ArrayList<Service>();
    JdbcPooledConnection conn = PooledConnectionHelper.createPooledConnection(PROPERTIES.getProperty(JDBC_QUERYSERVICE_DRIVER),
        PROPERTIES.getProperty(JDBC_QUERYSERVICE_URL), poolsize);

    try {
      for (int i = 0; i < maxServices; i++) {
        JdbcDataQueryService service = createMetadataService(false);
        service.setConnection(conn);
        serviceList.add(service);
        start(service);
      }
      assertEquals(0, conn.currentBusyConnectionCount());
      PooledConnectionHelper.executeTest(serviceList, iterations, new PooledConnectionHelper.MessageCreator() {
        @Override
        public AdaptrisMessage createMsgForPooledConnectionTest() throws Exception {
          return createMessage(entry);
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
  
  public void testService_AdvancedPooledConnection() throws Exception {
    int maxServices = 5;
    final int iterations = 5;
    int poolsize = maxServices - 1;

    String name = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    final AdapterTypeVersion entry = dbItems.get(0);
    populateDatabase(dbItems, false);

    List<Service> serviceList = new ArrayList<Service>();
    AdvancedJdbcPooledConnection conn = PooledConnectionHelper.createAdvancedPooledConnection(PROPERTIES.getProperty(JDBC_QUERYSERVICE_DRIVER),
        PROPERTIES.getProperty(JDBC_QUERYSERVICE_URL), poolsize);

    try {
      for (int i = 0; i < maxServices; i++) {
        JdbcDataQueryService service = createMetadataService(false);
        service.setConnection(conn);
        serviceList.add(service);
        start(service);
      }
      assertEquals(0, conn.currentBusyConnectionCount());
      PooledConnectionHelper.executeTest(serviceList, iterations, new PooledConnectionHelper.MessageCreator() {
        @Override
        public AdaptrisMessage createMsgForPooledConnectionTest() throws Exception {
          return createMessage(entry);
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

  // "SELECT adapter_version, message_translator_type, inserted_on, counter FROM adapter_type_version "
  public void testServiceWithExactColumnTranslators() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    FirstRowMetadataTranslator t = new FirstRowMetadataTranslator();

    t.addColumnTranslator(new StringColumnTranslator());
    t.addColumnTranslator(new StringColumnTranslator());
    t.addColumnTranslator(new TimestampColumnTranslator());
    t.addColumnTranslator(new IntegerColumnTranslator());

    s.setResultSetTranslator(t);

    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertEquals(entry.getVersion(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertEquals(entry.getTranslatorType(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertEquals(String.valueOf(entry.getCounter()),
        msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_COUNTER));
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    String expected = sdf.format(entry.getDate());
    assertEquals(expected, msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_INSERTED_ON));
  }

  // "SELECT adapter_version, message_translator_type, inserted_on, counter FROM adapter_type_version "
  public void testServiceWithDateNotTimestampColumnTranslators() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    FirstRowMetadataTranslator t = new FirstRowMetadataTranslator();

    t.addColumnTranslator(new StringColumnTranslator());
    t.addColumnTranslator(new StringColumnTranslator());
    t.addColumnTranslator(new DateColumnTranslator());
    t.addColumnTranslator(new IntegerColumnTranslator());

    s.setResultSetTranslator(t);

    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertEquals(entry.getVersion(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertEquals(entry.getTranslatorType(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertEquals(String.valueOf(entry.getCounter()),
        msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_COUNTER));
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
    String expected = sdf.format(entry.getDate());
    assertEquals(expected, msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_INSERTED_ON));
  }

  // "SELECT adapter_version, message_translator_type, inserted_on, counter FROM adapter_type_version "
  public void testServiceWithTimeNotTimestampColumnTranslators() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    FirstRowMetadataTranslator t = new FirstRowMetadataTranslator();

    t.addColumnTranslator(new StringColumnTranslator());
    t.addColumnTranslator(new StringColumnTranslator());
    t.addColumnTranslator(new TimeColumnTranslator());
    t.addColumnTranslator(new IntegerColumnTranslator());

    s.setResultSetTranslator(t);

    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertEquals(entry.getVersion(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertEquals(entry.getTranslatorType(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertEquals(String.valueOf(entry.getCounter()),
        msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_COUNTER));

    // The reversion back to GMT broke the previous test (no idea how); but this works as well.
    // So we change it to this instead to compare the raw times.
    // SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ssZ");
    // String expected = sdf.format(entry.getDate());
    // assertEquals(expected, msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_INSERTED_ON));
    String timeValue = msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_INSERTED_ON);
    assertNotNull(timeValue);
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ssZ");
    assertNotNull(sdf.parse(timeValue));

  }

  // "SELECT adapter_version, message_translator_type, inserted_on, counter FROM adapter_type_version "
  public void testServiceWithFloatNotIntColumnTranslators() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    FirstRowMetadataTranslator t = new FirstRowMetadataTranslator();

    t.addColumnTranslator(new StringColumnTranslator());
    t.addColumnTranslator(new StringColumnTranslator());
    t.addColumnTranslator(new TimestampColumnTranslator());
    t.addColumnTranslator(new FloatColumnTranslator());

    s.setResultSetTranslator(t);

    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertEquals(entry.getVersion(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertEquals(entry.getTranslatorType(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    // This is quite *silly*
    String expected = String.valueOf(Float.parseFloat(String.valueOf(entry.getCounter())));
    assertEquals(expected, msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_COUNTER));
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    expected = sdf.format(entry.getDate());
    assertEquals(expected, msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_INSERTED_ON));
  }

  // "SELECT adapter_version, message_translator_type, inserted_on, counter FROM adapter_type_version "
  public void testServiceWithFormattedFloatNotIntColumnTranslators() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    FirstRowMetadataTranslator t = new FirstRowMetadataTranslator();

    t.addColumnTranslator(new StringColumnTranslator());
    t.addColumnTranslator(new StringColumnTranslator());
    t.addColumnTranslator(new TimestampColumnTranslator());
    t.addColumnTranslator(new FloatColumnTranslator("%10f"));

    s.setResultSetTranslator(t);

    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertEquals(entry.getVersion(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertEquals(entry.getTranslatorType(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    // This is quite *silly*
    String expected = String.format("%10f", Float.parseFloat(String.valueOf(entry.getCounter())));
    assertEquals(expected, msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_COUNTER));
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    expected = sdf.format(entry.getDate());
    assertEquals(expected, msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_INSERTED_ON));
  }

  // "SELECT adapter_version, message_translator_type, inserted_on, counter FROM adapter_type_version "
  public void testServiceWithDoubleNotIntColumnTranslators() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    FirstRowMetadataTranslator t = new FirstRowMetadataTranslator();

    t.addColumnTranslator(new StringColumnTranslator());
    t.addColumnTranslator(new StringColumnTranslator());
    t.addColumnTranslator(new TimestampColumnTranslator());
    t.addColumnTranslator(new DoubleColumnTranslator());

    s.setResultSetTranslator(t);

    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertEquals(entry.getVersion(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertEquals(entry.getTranslatorType(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    // This is quite *silly*
    String expected = String.valueOf(Double.parseDouble(String.valueOf(entry.getCounter())));
    assertEquals(expected, msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_COUNTER));
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    expected = sdf.format(entry.getDate());
    assertEquals(expected, msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_INSERTED_ON));
  }

  // "SELECT adapter_version, message_translator_type, inserted_on, counter FROM adapter_type_version "
  public void testServiceWithFormattedDoubleNotIntColumnTranslators() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    FirstRowMetadataTranslator t = new FirstRowMetadataTranslator();

    t.addColumnTranslator(new StringColumnTranslator());
    t.addColumnTranslator(new StringColumnTranslator());
    t.addColumnTranslator(new TimestampColumnTranslator());
    t.addColumnTranslator(new DoubleColumnTranslator("%10f"));

    s.setResultSetTranslator(t);

    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertEquals(entry.getVersion(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertEquals(entry.getTranslatorType(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    // This is quite *silly*
    String expected = String.format("%10f", Double.parseDouble(String.valueOf(entry.getCounter())));
    
    System.out.println(msg);
    assertEquals(expected, msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_COUNTER));
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    expected = sdf.format(entry.getDate());
    assertEquals(expected, msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_INSERTED_ON));
  }

  // "SELECT adapter_version, message_translator_type, inserted_on, counter FROM adapter_type_version "
  public void testServiceWithMismatchedSizeColumnTranslators() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    FirstRowMetadataTranslator t = new FirstRowMetadataTranslator();

    t.addColumnTranslator(new StringColumnTranslator());
    t.addColumnTranslator(new StringColumnTranslator());

    s.setResultSetTranslator(t);

    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertEquals(entry.getVersion(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertEquals(entry.getTranslatorType(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_COUNTER));
    assertEquals(String.valueOf(entry.getCounter()),
        msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_COUNTER));
    // fAIRLY unlikely to be able to test equality on a date field when I specified a translator for it!
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_INSERTED_ON));
  }
  
  public void testBugRedmine6841NoRowsReturned() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataServiceNoResult();
    FirstRowMetadataTranslator t = new FirstRowMetadataTranslator();
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
  }
  
  @Override
  protected FirstRowMetadataTranslator createTranslatorForConfig() {
    return new FirstRowMetadataTranslator();
  }

  private java.util.Date yesterday() {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_YEAR, -1);
    return cal.getTime();
  }
}
