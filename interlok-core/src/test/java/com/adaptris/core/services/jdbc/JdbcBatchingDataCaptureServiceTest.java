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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.jdbc.AdvancedJdbcPooledConnection;
import com.adaptris.core.jdbc.JdbcConnection;
import com.adaptris.core.jdbc.JdbcPooledConnection;
import com.adaptris.core.jdbc.PooledConnectionHelper;

public class JdbcBatchingDataCaptureServiceTest extends JdbcDataCaptureServiceCase {

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Override
  protected JdbcBatchingDataCaptureService newService() {
    JdbcBatchingDataCaptureService service = new JdbcBatchingDataCaptureService();
    service.setBatchWindow(1);
    return service;
  }

  @Test
  public void testBackReferences() throws Exception {
    this.testBackReferences(new JdbcBatchingDataCaptureService("INSERT INTO MYTABLE ('ABC');"));
  }

  @Test
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

  @Test
  public void testService_IterationsLessThanBatch() throws Exception {
    createDatabase();
    JdbcBatchingDataCaptureService service = (JdbcBatchingDataCaptureService) createBasicService();
    service.setRowsUpdatedMetadataKey("rowsUpdatedKey");
    service.setBatchWindow(10);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_DOCUMENT);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    execute(service, msg);
    assertEquals("1", msg.getMetadataValue("rowsUpdatedKey"));
    execute(service, msg);
    assertEquals("1", msg.getMetadataValue("rowsUpdatedKey"));
    doBasicCaptureAsserts(2);
  }

  @Test
  public void testRowsUpdated() throws Exception {
    try {
      int[] results = {1, Statement.EXECUTE_FAILED, 2};
      JdbcBatchingDataCaptureService.rowsUpdated(results);
      fail();
    } catch (SQLException expected) {

    }
    int[] results = {1, Statement.SUCCESS_NO_INFO, 2};
    assertEquals(3L, JdbcBatchingDataCaptureService.rowsUpdated(results));
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
