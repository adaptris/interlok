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
import java.sql.SQLException;
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

public class MetadataIdentitySequenceNumberServiceTest extends SequenceNumberCase {

  private static final String JDBC_SEQUENCENUMBER_DRIVER = "jdbc.sequencenumber.driver";

  private static final String JDBC_SEQUENCENUMBER_URL = "jdbc.sequencenumber.url";

  private static final String DEFAULT_IDENTITY_METADATA_KEY = "identity";

  private static final String BASE_DIR_KEY = "SequenceNumberServiceExamples.baseDir";

  public MetadataIdentitySequenceNumberServiceTest(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  public void testMessageHasNoIdentityMetadata() throws Exception {
    createDatabase();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    MetadataIdentitySequenceNumberService service = createServiceForTests();
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException ex) {
      // pass
    }
  }

  public void test_Failure() throws Exception {
    createDatabase();
    AdaptrisMessage msg = createMessageForTests();
    MetadataIdentitySequenceNumberService service = configureForTests(new MetadataIdentitySequenceNumberService() {
      protected Connection getConnection(AdaptrisMessage msg) throws SQLException {
        throw new SQLException();
      }
    }, true);
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException ex) {
      // pass
    }
  }

  protected MetadataIdentitySequenceNumberService createService() {
    return new MetadataIdentitySequenceNumberService();
  }

  protected MetadataIdentitySequenceNumberService createServiceForTests() {
    return configureForTests(createService(), true);
  }

  private MetadataIdentitySequenceNumberService configureForTests(MetadataIdentitySequenceNumberService service,
                                                                  boolean addConnection) {
    if (addConnection) {
      service.setConnection(new JdbcConnection(PROPERTIES.getProperty(JDBC_SEQUENCENUMBER_URL), PROPERTIES
          .getProperty(JDBC_SEQUENCENUMBER_DRIVER)));
    }
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    service.setNumberFormat(DEFAULT_NUMBER_FORMAT);
    service.setIdentityMetadataKey(DEFAULT_IDENTITY_METADATA_KEY);
    return service;
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
        MetadataIdentitySequenceNumberService service = configureForTests(createService(), false);
        service.setConnection(conn);
        serviceList.add(service);
        start(service);
      }
      assertEquals(0, conn.currentBusyConnectionCount());
      PooledConnectionHelper.executeTest(serviceList, iterations, new PooledConnectionHelper.MessageCreator() {

        @Override
        public AdaptrisMessage createMsgForPooledConnectionTest() throws Exception {
          AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
          msg.addMetadata(DEFAULT_IDENTITY_METADATA_KEY, new GuidGenerator().safeUUID());
          return msg;
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
        MetadataIdentitySequenceNumberService service = configureForTests(createService(), false);
        service.setConnection(conn);
        serviceList.add(service);
        start(service);
      }
      assertEquals(0, conn.currentBusyConnectionCount());
      PooledConnectionHelper.executeTest(serviceList, iterations, new PooledConnectionHelper.MessageCreator() {

        @Override
        public AdaptrisMessage createMsgForPooledConnectionTest() throws Exception {
          AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
          msg.addMetadata(DEFAULT_IDENTITY_METADATA_KEY, new GuidGenerator().safeUUID());
          return msg;
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
  protected Object retrieveObjectForSampleConfig() {
    JdbcConnection connection = new JdbcConnection();
    connection.setConnectUrl("jdbc:mysql://localhost:3306/mydatabase");
    connection.setConnectionAttempts(2);
    connection.setConnectionRetryInterval(new TimeInterval(3L, "SECONDS"));
    MetadataIdentitySequenceNumberService service = new MetadataIdentitySequenceNumberService();
    service.setMetadataKey("sequence_no");
    service.setNumberFormat(DEFAULT_NUMBER_FORMAT);
    service.setConnection(connection);
    service.setIdentityMetadataKey(DEFAULT_IDENTITY_METADATA_KEY);
    service.setOverflowBehaviour(AbstractJdbcSequenceNumberService.OverflowBehaviour.Continue);
    return service;
  }

  @Override
  public void testBackReferences() throws Exception {
    this.testBackReferences(createServiceForTests());
  }

  @Override
  protected AdaptrisMessage createMessageForTests() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(DEFAULT_IDENTITY_METADATA_KEY, DEFAULT_ID);
    return msg;
  }
}
