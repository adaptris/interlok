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

import static com.adaptris.core.services.jdbc.AbstractJdbcSequenceNumberService.DEFAULT_INSERT_STATEMENT;
import static com.adaptris.core.services.jdbc.AbstractJdbcSequenceNumberService.DEFAULT_RESET_STATEMENT;
import static com.adaptris.core.services.jdbc.AbstractJdbcSequenceNumberService.DEFAULT_SELECT_STATEMENT;
import static com.adaptris.core.services.jdbc.AbstractJdbcSequenceNumberService.DEFAULT_UPDATE_STATEMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.jdbc.DatabaseConnection;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

@SuppressWarnings("deprecation")
public abstract class SequenceNumberCase
    extends com.adaptris.interlok.junit.scaffolding.services.JdbcServiceCase {

  protected static final String DEFAULT_NUMBER_FORMAT = "000000000";
  protected static final String DEFAULT_METADATA_KEY = "sequence_number";
  protected static final String DEFAULT_ID = "adpMsg";

  private static final String EXAMPLE_COMMENT = "<!-- The default database schme is assumed to be  \n"
      + "CREATE TABLE SEQUENCES (ID VARCHAR(255) NOT NULL, SEQ_NUMBER INT)\n"
      + "If you have a table called SEQUENCES with those two columns then you will not \n"
      + "have to change any SQL statements (i.e. they can be removed from your configuration)" + "\n-->\n";

  @Test
  public void testReplaceMetadata() {
    AbstractJdbcSequenceNumberService service = createService();
    assertNull(service.getAlwaysReplaceMetadata());
    assertTrue(service.alwaysReplaceMetadata());
    service.setAlwaysReplaceMetadata(Boolean.FALSE);
    assertEquals(Boolean.FALSE, service.getAlwaysReplaceMetadata());
    assertFalse(service.alwaysReplaceMetadata());
  }

  @Test
  public void testInsertStatement() {
    AbstractJdbcSequenceNumberService service = createService();
    assertNull(service.getInsertStatement());
    assertEquals(DEFAULT_INSERT_STATEMENT, service.insertStatement());
    service.setInsertStatement("fred");
    assertEquals("fred", service.getInsertStatement());
    assertEquals("fred", service.insertStatement());
  }

  @Test
  public void testSelectStatement() {
    AbstractJdbcSequenceNumberService service = createService();
    assertNull(service.getSelectStatement());
    assertEquals(DEFAULT_SELECT_STATEMENT, service.selectStatement());
    service.setSelectStatement("fred");
    assertEquals("fred", service.getSelectStatement());
    assertEquals("fred", service.selectStatement());

  }

  @Test
  public void testUpdateStatement() {
    AbstractJdbcSequenceNumberService service = createService();
    assertNull(service.getUpdateStatement());
    assertEquals(DEFAULT_UPDATE_STATEMENT, service.updateStatement());
    service.setUpdateStatement("fred");
    assertEquals("fred", service.getUpdateStatement());
    assertEquals("fred", service.updateStatement());

  }

  @Test
  public void testResetStatement() {
    AbstractJdbcSequenceNumberService service = createService();
    assertNull(service.getResetStatement());
    assertEquals(DEFAULT_RESET_STATEMENT, service.resetStatement());
    service.setResetStatement("fred");
    assertEquals("fred", service.getResetStatement());
    assertEquals("fred", service.resetStatement());
  }

  @Test
  public void testMetadataKey() {
    AbstractJdbcSequenceNumberService service = createService();
    assertNull(service.getMetadataKey());
    service.setMetadataKey("fred");
    assertEquals("fred", service.getMetadataKey());
  }

  @Test
  public void testNumberFormat() {
    AbstractJdbcSequenceNumberService service = createService();
    assertEquals("0", service.getNumberFormat());
    service.setNumberFormat("000");
    assertEquals("000", service.getNumberFormat());
  }

  @Test
  public void testInit() throws Exception {
    AbstractJdbcSequenceNumberService service = createService();
    try {
      LifecycleHelper.init(service);
      fail();
    }
    catch (CoreException e) {

    }
    service.setMetadataKey("fred");
    service.setNumberFormat("000");
    try {
      LifecycleHelper.init(service);
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  @Test
  public void testSequenceNumberInsert() throws Exception {
    createDatabase();
    AdaptrisMessage msg = createMessageForTests();
    AbstractJdbcSequenceNumberService service = createServiceForTests();
    service.setAlwaysReplaceMetadata(false);
    execute(service, msg);
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("000000001", msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertEquals(2, getCurrentSequenceNumber(DEFAULT_ID));
  }

  @Test
  public void testSequenceNumber_MetadataAlreadyExists_Override() throws Exception {
    createDatabase();
    populateDatabase(DEFAULT_ID, 15);
    AdaptrisMessage msg = createMessageForTests();
    msg.addMetadata(DEFAULT_METADATA_KEY, "0");
    AbstractJdbcSequenceNumberService service = createServiceForTests();
    service.setAlwaysReplaceMetadata(true);
    execute(service, msg);
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("000000015", msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertEquals(16, getCurrentSequenceNumber(DEFAULT_ID));
  }

  @Test
  public void testSequenceNumber_MetadataAlreadyExists_NoOverride() throws Exception {
    createDatabase();
    populateDatabase(DEFAULT_ID, 15);
    AdaptrisMessage msg = createMessageForTests();
    msg.addMetadata(DEFAULT_METADATA_KEY, "0");
    AbstractJdbcSequenceNumberService service = createServiceForTests();
    service.setAlwaysReplaceMetadata(false);
    execute(service, msg);
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("0", msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertEquals(15, getCurrentSequenceNumber(DEFAULT_ID));
  }

  @Test
  public void testSequenceNumberSelect() throws Exception {
    createDatabase();
    populateDatabase(DEFAULT_ID, 15);
    AdaptrisMessage msg = createMessageForTests();
    AbstractJdbcSequenceNumberService service = createServiceForTests();
    execute(service, msg);
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("000000015", msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertEquals(16, getCurrentSequenceNumber(DEFAULT_ID));
  }

  @Test
  public void testSequenceNumberInsertNoAutoCommit() throws Exception {
    createDatabase();
    populateDatabase(DEFAULT_ID, 15);
    AdaptrisMessage msg = createMessageForTests();
    AbstractJdbcSequenceNumberService service = createServiceForTests();
    service.getConnection().retrieveConnection(DatabaseConnection.class).setAutoCommit(false);
    execute(service, msg);
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("000000015", msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertEquals(16, getCurrentSequenceNumber(DEFAULT_ID));
  }

  @Test
  public void testSequenceNumberSelectNoAutoCommit() throws Exception {
    createDatabase();
    populateDatabase(DEFAULT_ID, 15);
    AdaptrisMessage msg = createMessageForTests();
    AbstractJdbcSequenceNumberService service = createServiceForTests();
    service.getConnection().retrieveConnection(DatabaseConnection.class).setAutoCommit(false);
    execute(service, msg);
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("000000015", msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertEquals(16, getCurrentSequenceNumber(DEFAULT_ID));
  }

  @Test
  public void testSequenceNumberExceedsNumberFormat_ResetToOne() throws Exception {
    createDatabase();
    populateDatabase(DEFAULT_ID, 10000);
    AdaptrisMessage msg = createMessageForTests();
    AbstractJdbcSequenceNumberService service = createServiceForTests();
    service.setNumberFormat("0000");
    service.setOverflowBehaviour(AbstractJdbcSequenceNumberService.OverflowBehaviour.ResetToOne);
    execute(service, msg);
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("0001", msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertEquals(2, getCurrentSequenceNumber(DEFAULT_ID));
  }

  @Test
  public void testSequenceNumberExceedsNumberFormat_Continue() throws Exception {
    createDatabase();
    populateDatabase(DEFAULT_ID, 10000);
    AdaptrisMessage msg = createMessageForTests();
    AbstractJdbcSequenceNumberService service = createServiceForTests();
    service.setNumberFormat("0000");
    service.setOverflowBehaviour(AbstractJdbcSequenceNumberService.OverflowBehaviour.Continue);
    execute(service, msg);
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("10000", msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertEquals(10001, getCurrentSequenceNumber(DEFAULT_ID));
  }

  @Test
  public void testSequenceNumberExceedsNumberFormat_BadConfig() throws Exception {
    createDatabase();
    populateDatabase(DEFAULT_ID, 10000);
    AdaptrisMessage msg = createMessageForTests();
    AbstractJdbcSequenceNumberService service = createServiceForTests();
    service.setNumberFormat("0000");
    service.setOverflowBehaviour(null); // should default to continue.
    execute(service, msg);
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("10000", msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertEquals(10001, getCurrentSequenceNumber(DEFAULT_ID));
  }

  @Test
  public void testAutoCreateDatabase_WithTimeout() throws Exception {
    Connection c = createConnection();
    dropDatabase(c);
    JdbcUtil.closeQuietly(c);
    AbstractJdbcSequenceNumberService service = createServiceForTests();
    service.setStatementTimeout(new TimeInterval(1L, TimeUnit.MINUTES));
    service.setCreateDatabase(true);
    AdaptrisMessage msg = createMessageForTests();
    execute(service, msg);
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("000000001", msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertEquals(2, getCurrentSequenceNumber(DEFAULT_ID));
  }

  @Test
  public void testAutoCreateDatabase() throws Exception {
    Connection c = createConnection();
    dropDatabase(c);
    JdbcUtil.closeQuietly(c);
    AbstractJdbcSequenceNumberService service = createServiceForTests();
    service.setCreateDatabase(true);
    AdaptrisMessage msg = createMessageForTests();
    execute(service, msg);
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("000000001", msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertEquals(2, getCurrentSequenceNumber(DEFAULT_ID));
  }

  @Test
  public void testAutoCreateDatabase_DatabaseExists() throws Exception {
    createDatabase();
    AbstractJdbcSequenceNumberService service = createServiceForTests();
    service.setCreateDatabase(true);
    AdaptrisMessage msg = createMessageForTests();
    execute(service, msg);
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("000000001", msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertEquals(2, getCurrentSequenceNumber(DEFAULT_ID));
  }

  @Test
  public void testSequenceNumber_ExceedsMaxConfigured_ResetToOne() throws Exception {
    createDatabase();
    populateDatabase(DEFAULT_ID, 10000);
    AdaptrisMessage msg = createMessageForTests();
    AbstractJdbcSequenceNumberService service = createServiceForTests();
    service.setMaximumSequenceNumber(9999L);
    service.setNumberFormat("00000");
    service.setOverflowBehaviour(AbstractJdbcSequenceNumberService.OverflowBehaviour.ResetToOne);
    execute(service, msg);
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("00001", msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertEquals(2, getCurrentSequenceNumber(DEFAULT_ID));
  }

  @Test
  public void testSequenceNumber_MaxConfigured_ResetToOne() throws Exception {
    createDatabase();
    populateDatabase(DEFAULT_ID, 10000);
    AdaptrisMessage msg = createMessageForTests();
    AbstractJdbcSequenceNumberService service = createServiceForTests();
    service.setMaximumSequenceNumber(50000L);
    service.setNumberFormat("00000");
    service.setOverflowBehaviour(AbstractJdbcSequenceNumberService.OverflowBehaviour.ResetToOne);
    execute(service, msg);
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("10000", msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertEquals(10001, getCurrentSequenceNumber(DEFAULT_ID));
  }

  protected void createDatabase() throws Exception {
    Connection c = createConnection();
    dropDatabase(c);
    Statement s = c.createStatement();
    s.execute("CREATE TABLE sequences (id VARCHAR(255) NOT NULL, seq_number INT)");
    JdbcUtil.closeQuietly(s);
    JdbcUtil.closeQuietly(c);
  }

  protected Connection createConnection() throws Exception {
    Class.forName(PROPERTIES.getProperty("jdbc.sequencenumber.driver"));
    Connection c = DriverManager.getConnection(PROPERTIES.getProperty("jdbc.sequencenumber.url"));
    c.setAutoCommit(true);
    return c;
  }

  protected void dropDatabase(Connection c) throws Exception {
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

  protected void populateDatabase(String id, int seqNo) throws Exception {
    Connection c = createConnection();
    PreparedStatement ps = c.prepareStatement("insert into sequences (id, seq_number) values (?, ?)");
    try {
      ps.setString(1, id);
      ps.setInt(2, seqNo);
      ps.executeUpdate();
    }
    finally {
      JdbcUtil.closeQuietly(ps);
      JdbcUtil.closeQuietly(c);
    }
  }

  protected int getCurrentSequenceNumber(String id) throws Exception {
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

  protected abstract AbstractJdbcSequenceNumberService createService();

  protected abstract AbstractJdbcSequenceNumberService createServiceForTests();

  protected abstract AdaptrisMessage createMessageForTests();

  @Override
  protected String getExampleCommentHeader(Object object) {
    return super.getExampleCommentHeader(object) + EXAMPLE_COMMENT;
  }

}
