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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairBag;
import com.adaptris.util.KeyValuePairSet;

public class JdbcMapUpsertTest extends JdbcMapInsertCase {

  protected static final String UTC_0 = "1970-01-01";
  protected static final String SMITH = "smith";
  protected static final String ID_ELEMENT_VALUE = "firstname";
  protected static final String ALICE = "alice";
  protected static final String DOB = "2017-01-01";

  protected static final String INSERT_STMT = String.format("INSERT INTO %s (firstname, lastname, dob) VALUES ('%s', '%s' ,'%s')",
      TABLE_NAME, ALICE, SMITH, UTC_0);

  protected static final String INSERT_QUOTED = String
      .format("INSERT INTO %s (\"firstname\", \"lastname\", \"dob\") VALUES ('%s', '%s' ,'%s')",
      TABLE_NAME, ALICE, SMITH, UTC_0);

  protected static final String SELECT_STMT = "SELECT * FROM %s WHERE firstname='%s'";
  protected static final String SELECT_QUOTED = "SELECT * FROM %s WHERE \"firstname\"='%s'";
  protected static final String CREATE_ALL_BASIC_TYPES = String.format("CREATE TABLE %s (firstname VARCHAR(128) NOT NULL,"
      + "lastname VARCHAR(128) NOT NULL,"
      + "dob DATE,"
      + "integerColumn INTEGER,"
      + "longColumn BIGINT,"
      + "booleanColumn BOOLEAN,"
      + "bigIntegerColumn BIGINT,"
      + "bigDecimalColumn DECIMAL,"
      + "floatColumn FLOAT,"
      + "doubleColumn DOUBLE,"
      + "timestampColumn TIMESTAMP,"
      + "timeColumn TIME)", TABLE_NAME);

  protected static final String CONTENT_ALL_TYPES =
      "firstname=alice\n" +
      "lastname=smith\n" +
      "dob=2017-01-01\n" +
      "integerColumn=1\n" +
      "longColumn=1\n" +
      "booleanColumn=true\n" +
      "bigIntegerColumn=1\n" +
      "bigDecimalColumn=1.0\n" +
      "floatColumn=1.0\n" +
      "doubleColumn=1.0\n" +
      "timestampColumn=2017-01-01 00:01:00\n" +
      "timeColumn=00:01:00\n";

  protected static final String INSERT_STMT_ALL_TYPES = String.format("INSERT INTO %s "
      + "(firstname, lastname, dob, integerColumn, longColumn, booleanColumn, bigIntegerColumn, bigDecimalColumn, "
      + "floatColumn, doubleColumn, timestampColumn, timeColumn) "
      + "VALUES ('%s', '%s' , '%s', 1, 1, false, 1, 1.0, 1.0, 1.0, '1970-01-01 00:00:00', '00:00:00')",
      TABLE_NAME, ALICE, SMITH, UTC_0);

  @Test
  public void testDatabaseId() {
    JdbcMapUpsert upsert = createService();
    assertEquals(JdbcMapUpsert.DEFAULT_ID_FIELD, upsert.idField());
    assertEquals("hello", upsert.withId("hello").idField());
  }

  @Test
  public void testService_Insert() throws Exception {
    createDatabase();
    UpsertProperties service = configureForTests(createService()).withRowsAffectedMetadataKey("rowsInserted");
    service.setIdField(ID_ELEMENT_VALUE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CONTENT);
    ExampleServiceCase.execute(service, msg);
    doAssert(1);
    assertTrue(msg.headersContainsKey("rowsInserted"));
    assertEquals("1", msg.getMetadataValue("rowsInserted"));
  }

  @Test
  public void testService_Insert_WithBookend() throws Exception {
    createDatabase(CREATE_QUOTED);
    UpsertProperties service = configureForTests(createService());
    service.withColumnBookend('"');
    service.setIdField(ID_ELEMENT_VALUE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CONTENT);
    ExampleServiceCase.execute(service, msg);
    doAssert(1);
  }

  @Test
  public void testService_InvalidColumns() throws Exception {
    createDatabase();
    JdbcMapUpsert service = configureForTests(createService()).withId(ID_ELEMENT_VALUE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(INVALID_COLUMN);
    try {
      ExampleServiceCase.execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  @Test
  public void testService_Update() throws Exception {
    createDatabase();
    populateDatabase();
    UpsertProperties service = configureForTests(createService()).withRowsAffectedMetadataKey("rowsUpdated");
    service.setIdField(ID_ELEMENT_VALUE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CONTENT);
    ExampleServiceCase.execute(service, msg);
    assertTrue(msg.headersContainsKey("rowsUpdated"));
    assertEquals("1", msg.getMetadataValue("rowsUpdated"));
    doAssert(1);
    checkDob(ALICE, DOB);
  }

  @Test
  public void testService_Update_Bookend() throws Exception {
    createDatabase(CREATE_QUOTED);
    populateDatabase(INSERT_QUOTED);
    UpsertProperties service = configureForTests(createService());
    service.withColumnBookend('"');
    service.setIdField(ID_ELEMENT_VALUE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CONTENT);
    ExampleServiceCase.execute(service, msg);
    doAssert(1);
    checkDob(SELECT_QUOTED, ALICE, DOB);
  }

  @Test
  public void testService_Update_AllColumns() throws Exception {
    createDatabase(CREATE_ALL_BASIC_TYPES);
    populateDatabase(INSERT_STMT_ALL_TYPES);
    UpsertProperties service = configureForTests(createService());
    KeyValuePairSet mappings = new KeyValuePairSet();
    mappings.add(new KeyValuePair("firstname", JdbcMapInsert.BasicType.String.name()));
    mappings.add(new KeyValuePair("lastname", JdbcMapInsert.BasicType.String.name()));
    mappings.add(new KeyValuePair("dob", JdbcMapInsert.BasicType.Date.name()));
    mappings.add(new KeyValuePair("integerColumn", JdbcMapInsert.BasicType.Integer.name()));
    mappings.add(new KeyValuePair("longColumn", JdbcMapInsert.BasicType.Long.name()));
    mappings.add(new KeyValuePair("booleanColumn", JdbcMapInsert.BasicType.Boolean.name()));
    mappings.add(new KeyValuePair("bigIntegerColumn", JdbcMapInsert.BasicType.BigInteger.name()));
    mappings.add(new KeyValuePair("bigDecimalColumn", JdbcMapInsert.BasicType.BigDecimal.name()));
    mappings.add(new KeyValuePair("floatColumn", JdbcMapInsert.BasicType.Float.name()));
    mappings.add(new KeyValuePair("doubleColumn", JdbcMapInsert.BasicType.Double.name()));
    mappings.add(new KeyValuePair("timestampColumn", JdbcMapInsert.BasicType.Timestamp.name()));
    mappings.add(new KeyValuePair("timeColumn", JdbcMapInsert.BasicType.Time.name()));
    service.setFieldMappings(mappings);
    service.setIdField(ID_ELEMENT_VALUE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CONTENT_ALL_TYPES);
    ExampleServiceCase.execute(service, msg);
    doAssert(1);
    checkDob(ALICE, DOB);
    try (Connection c = createConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery(String.format(SELECT_STMT, TABLE_NAME, ALICE))) {
      if (rs.next()) {
        assertEquals(true, rs.getBoolean("booleanColumn"));
      }
      else {
        fail("No Match for firstname: " + ALICE);
      }
    }
  }

  @Override
  protected UpsertProperties createService() {
    return new UpsertProperties();
  }

  protected static void populateDatabase() throws Exception {
    populateDatabase(INSERT_STMT);
  }

  protected static void populateDatabase(String insertStmt) throws Exception {
    try (Connection c = createConnection(); Statement s = c.createStatement()) {
      s.execute(insertStmt);
    }
  }

  protected static void checkDob(String firstname, String dob) throws Exception {
    checkDob(SELECT_STMT, firstname, dob);
  }

  protected static void checkDob(String selectStmt, String firstname, String dob) throws Exception {
    try (Connection c = createConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(String.format(selectStmt, TABLE_NAME, firstname))){
      if (rs.next()) {
        assertEquals(dob, rs.getString("dob"));
      }
      else {
        fail("No Match for firstname: " + firstname);
      }
    }
  }

  protected class UpsertProperties extends JdbcMapUpsert {

    @Override
    public void doService(AdaptrisMessage msg) throws ServiceException {
      Connection conn = null;
      try {
        conn = getConnection(msg);
        addUpdatedMetadata(handleUpsert(table(msg), conn, mapify(msg)), msg);
        JdbcUtil.commit(conn, msg);
      }
      catch (Exception e) {
        JdbcUtil.rollback(conn, msg);
        throw ExceptionHelper.wrapServiceException(e);
      }
      finally {
        JdbcUtil.closeQuietly(conn);
      }
    }

    protected Map<String, String> mapify(AdaptrisMessage msg) throws Exception {
      Properties result = new Properties();
      try (InputStream in = msg.getInputStream()) {
        result.load(in);
      }
      return KeyValuePairBag.asMap(new KeyValuePairSet(result));
    }
  }
}
