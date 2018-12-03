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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.jdbc.AdvancedJdbcPooledConnection;
import com.adaptris.core.jdbc.JdbcConnection;
import com.adaptris.core.jdbc.JdbcPooledConnection;
import com.adaptris.core.jdbc.PooledConnectionHelper;

public class JdbcBatchingDataCaptureServiceTest extends JdbcDataCaptureServiceCase {

  public JdbcBatchingDataCaptureServiceTest(String arg0) {
    super(arg0);
  }

  @Override
  protected JdbcBatchingDataCaptureService newService() {
    JdbcBatchingDataCaptureService service = new JdbcBatchingDataCaptureService();
    service.setBatchWindow(1);
    return service;
  }

  @Override
  public void testBackReferences() throws Exception {
    this.testBackReferences(new JdbcBatchingDataCaptureService("INSERT INTO MYTABLE ('ABC');"));
  }
  public void testBatchWindow() {
    JdbcBatchingDataCaptureService service = new JdbcBatchingDataCaptureService();
    assertNull(service.getBatchWindow());
    assertEquals(JdbcBatchingDataCaptureService.DEFAULT_BATCH_WINDOW, service.batchWindow());
    service.setBatchWindow(10);
    assertEquals(Integer.valueOf(10), service.getBatchWindow());
    assertEquals(10, service.batchWindow());
    service.setBatchWindow(null);
    assertNull(service.getBatchWindow());
    assertEquals(JdbcBatchingDataCaptureService.DEFAULT_BATCH_WINDOW, service.batchWindow());
  }

  public void testService_IterationsLessThanBatch() throws Exception {
    createDatabase();
    JdbcBatchingDataCaptureService service = (JdbcBatchingDataCaptureService) createBasicService();
    service.setBatchWindow(10);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_DOCUMENT);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    execute(service, msg);
    execute(service, msg);
    doBasicCaptureAsserts(2);
  }

  @Override
  protected JdbcConnection createJdbcConnection() {
    JdbcConnection connection = super.createJdbcConnection();
    connection.setAutoCommit(false);
    return connection;
  }

  @Override
  protected AdvancedJdbcPooledConnection createAdvancedPooledJdbcConnection(int poolsize) {
    AdvancedJdbcPooledConnection conn = PooledConnectionHelper.createAdvancedPooledConnection(
        PROPERTIES.getProperty(JDBC_CAPTURE_SERVICE_DRIVER),
        PROPERTIES.getProperty(JDBC_CAPTURE_SERVICE_URL), poolsize);
    conn.setAutoCommit(false);
    return conn;
  }

  @Override
  protected JdbcPooledConnection createPooledJdbcConnection(int poolsize) {
    JdbcPooledConnection conn = PooledConnectionHelper.createPooledConnection(PROPERTIES.getProperty(JDBC_CAPTURE_SERVICE_DRIVER),
        PROPERTIES.getProperty(JDBC_CAPTURE_SERVICE_URL), poolsize);
    conn.setAutoCommit(false);
    return conn;
  }
}
