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
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.TimeInterval;

@SuppressWarnings("deprecation")
public class StaticIdentitySequenceNumberServiceTest extends SequenceNumberCase {

  private static final String JDBC_SEQUENCENUMBER_DRIVER = "jdbc.sequencenumber.driver";
  private static final String JDBC_SEQUENCENUMBER_URL = "jdbc.sequencenumber.url";
  private static final String BASE_DIR_KEY = "SequenceNumberServiceExamples.baseDir";

  private static final String NULL_RESET_STATEMENT = "UPDATE SEQUENCES SET SEQ_NUMBER = ? WHERE ID = 'abc'";
  private static final String NULL_UPDATE_STATEMENT = "UPDATE SEQUENCES SET SEQ_NUMBER = SEQ_NUMBER+1 WHERE ID='abc'";
  private static final String NULL_INSERT_STATEMENT = "INSERT INTO SEQUENCES (ID, SEQ_NUMBER) VALUES ('abc', 2)";
  private static final String NULL_SELECT_STATEMENT = "SELECT SEQ_NUMBER from SEQUENCES where ID='abc'";

  public StaticIdentitySequenceNumberServiceTest(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  protected StaticIdentitySequenceNumberService createService() {
    return new StaticIdentitySequenceNumberService();
  }

  protected StaticIdentitySequenceNumberService createServiceForTests() {
    return createServiceForTests(true);
  }

  private StaticIdentitySequenceNumberService createServiceForTests(boolean createConnection) {
    StaticIdentitySequenceNumberService service = new StaticIdentitySequenceNumberService();
    if (createConnection) {
      service.setConnection(new JdbcConnection(PROPERTIES.getProperty(JDBC_SEQUENCENUMBER_URL), PROPERTIES
        .getProperty(JDBC_SEQUENCENUMBER_DRIVER)));
    }
    service.setIdentity(DEFAULT_ID);
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    service.setNumberFormat(DEFAULT_NUMBER_FORMAT);
    return service;
  }

  public void testService_NullIdentity_Overflows() throws Exception {

    try {
      StaticIdentitySequenceNumberService service = createServiceForTests();

      service.setResetStatement(NULL_RESET_STATEMENT);
      service.setInsertStatement(NULL_INSERT_STATEMENT);
      service.setUpdateStatement(NULL_UPDATE_STATEMENT);
      service.setSelectStatement(NULL_SELECT_STATEMENT);
      service.setIdentity(null);

      createDatabase();
      populateDatabase("abc", 10000);
      AdaptrisMessage msg = createMessageForTests();
      service.setNumberFormat("0000");
      service.setOverflowBehaviour(AbstractJdbcSequenceNumberService.OverflowBehaviour.Continue);
      execute(service, msg);
      assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
      assertEquals("10000", msg.getMetadataValue(DEFAULT_METADATA_KEY));
      assertEquals(10001, getCurrentSequenceNumber("abc"));

    }
    finally {

    }
  }

  public void testService_NullIdentity_Insert() throws Exception {
    try {
      createDatabase();
      AdaptrisMessage msg = createMessageForTests();
      StaticIdentitySequenceNumberService service = createServiceForTests();
      service.setResetStatement(NULL_RESET_STATEMENT);
      service.setInsertStatement(NULL_INSERT_STATEMENT);
      service.setUpdateStatement(NULL_UPDATE_STATEMENT);
      service.setSelectStatement(NULL_SELECT_STATEMENT);
      service.setIdentity(null);
      service.setAlwaysReplaceMetadata(true);
      execute(service, msg);
      assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
      assertEquals("000000001", msg.getMetadataValue(DEFAULT_METADATA_KEY));
      assertEquals(2, getCurrentSequenceNumber("abc"));
    }
    finally {

    }
  }

  public void testService_NullIdentity_SelectUpdate() throws Exception {
    try {
      StaticIdentitySequenceNumberService service = createServiceForTests();

      service.setIdentity(null);
      service.setResetStatement(NULL_RESET_STATEMENT);
      service.setInsertStatement(NULL_INSERT_STATEMENT);
      service.setUpdateStatement(NULL_UPDATE_STATEMENT);
      service.setSelectStatement(NULL_SELECT_STATEMENT);

      createDatabase();
      populateDatabase("abc", 15);
      AdaptrisMessage msg = createMessageForTests();
      execute(service, msg);
      assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
      assertEquals("000000015", msg.getMetadataValue(DEFAULT_METADATA_KEY));
      assertEquals(16, getCurrentSequenceNumber("abc"));
    }
    finally {

    }
  }

  public void testService_NullIdentity_DefaultStatements() throws Exception {
    try {
      createDatabase();
      AdaptrisMessage msg = createMessageForTests();
      StaticIdentitySequenceNumberService service = createServiceForTests();
      service.setIdentity(null);
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {
      // expected as we call SELECT SEQ_NUMBER from SEQUENCES where ID=? with a null parameter.
    }
    finally {

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
    JdbcPooledConnection conn = PooledConnectionHelper.createPooledConnection(PROPERTIES.getProperty(JDBC_SEQUENCENUMBER_DRIVER),
        PROPERTIES.getProperty(JDBC_SEQUENCENUMBER_URL), poolsize);

    try {
      for (int i = 0; i < maxServices; i++) {
        StaticIdentitySequenceNumberService service = createServiceForTests(false);
        service.setConnection(conn);
        service.setIdentity(new GuidGenerator().safeUUID());
        serviceList.add(service);
        start(service);
      }
      PooledConnectionHelper.executeTest(serviceList, iterations, new PooledConnectionHelper.MessageCreator() {

        @Override
        public AdaptrisMessage createMsgForPooledConnectionTest() throws Exception {
          return createMessageForTests();
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

    createDatabase();
    List<Service> serviceList = new ArrayList<Service>();
    String name = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    AdvancedJdbcPooledConnection conn = PooledConnectionHelper.createAdvancedPooledConnection(PROPERTIES.getProperty(JDBC_SEQUENCENUMBER_DRIVER),
        PROPERTIES.getProperty(JDBC_SEQUENCENUMBER_URL), poolsize);

    try {
      for (int i = 0; i < maxServices; i++) {
        StaticIdentitySequenceNumberService service = createServiceForTests(false);
        service.setConnection(conn);
        service.setIdentity(new GuidGenerator().safeUUID());
        serviceList.add(service);
        start(service);
      }
      PooledConnectionHelper.executeTest(serviceList, iterations, new PooledConnectionHelper.MessageCreator() {

        @Override
        public AdaptrisMessage createMsgForPooledConnectionTest() throws Exception {
          return createMessageForTests();
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

  @Override
  protected StaticIdentitySequenceNumberService retrieveObjectForSampleConfig() {
    JdbcConnection connection = new JdbcConnection();
    connection.setConnectUrl("jdbc:mysql://localhost:3306/mydatabase");
    connection.setConnectionAttempts(2);
    connection.setConnectionRetryInterval(new TimeInterval(3L, "SECONDS"));
    StaticIdentitySequenceNumberService service = new StaticIdentitySequenceNumberService();
    service.setIdentity("adaptrismsg");
    service.setMetadataKey("sequence_no");
    service.setNumberFormat(DEFAULT_NUMBER_FORMAT);
    service.setConnection(connection);
    service.setOverflowBehaviour(AbstractJdbcSequenceNumberService.OverflowBehaviour.Continue);
    return service;
  }

  @Override
  protected StaticIdentitySequenceNumberService retrieveObjectForCastorRoundTrip() {
    return createServiceForTests(true);
  }

  @Override
  public void testBackReferences() throws Exception {
    this.testBackReferences(createServiceForTests());
  }

  @Override
  protected AdaptrisMessage createMessageForTests() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    return msg;
  }
}
