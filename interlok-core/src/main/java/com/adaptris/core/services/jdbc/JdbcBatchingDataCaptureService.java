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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.namespace.NamespaceContext;

import org.apache.commons.lang.ArrayUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.jdbc.DatabaseConnection;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.util.text.xml.XPath;
import com.mysql.jdbc.Statement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Capture Data from a AdaptrisMessage and store it in a JDBC-compliant database.
 * <p>
 * There is probably little or no point in having batches if your underlying database connection has {@code auto-commit=true}. No
 * checks on the underling {@link java.sql.Connection} so if {@link java.sql.DatabaseMetaData#supportsBatchUpdates()} is likely to
 * return false, then results may be undefined.
 * </p>
 * <p>
 * With a {@link #setBatchWindow(Integer)} of 1, then it will be functionally equivalent to {@link JdbcDataCaptureService}.
 * </p>
 * <p>
 * If the {@code DocumentBuilderFactoryBuilder} has been explicitly set to be not namespace aware and the document does in fact
 * contain namespaces, then Saxon can cause merry havoc in the sense that {@code //NonNamespaceXpath} doesn't work if the document
 * has namespaces in it. We have included a shim so that behaviour can be toggled based on what you have configured.
 * </p>
 * 
 * @see XPath#newXPathInstance(DocumentBuilderFactoryBuilder, NamespaceContext)
 * @see JdbcDataCaptureService
 * @config jdbc-batching-data-capture-service
 */
@XStreamAlias("jdbc-batching-data-capture-service")
@AdapterComponent
@ComponentProfile(summary = "Capture data from the message and store it in a database", tag = "service,jdbc",
    recommended = {DatabaseConnection.class})
@DisplayOrder(order =
{
    "connection", "statement", "batchWindow", "iterationXpath", "iterates", "statementParameters", "parameterApplicator",
    "xmlDocumentFactoryConfig", "namespaceContext", "saveReturnedKeys", "saveReturnedKeysColumn", "saveReturnedKeysTable"})
public class JdbcBatchingDataCaptureService extends JdbcIteratingDataCaptureServiceImpl {

  private static final InheritableThreadLocal<AtomicInteger> counter = new InheritableThreadLocal<AtomicInteger>() {
    @Override
    protected synchronized AtomicInteger initialValue() {
      return new AtomicInteger();
    }
  };

  public static final int DEFAULT_BATCH_WINDOW = 1024;

  @InputFieldDefault(value = "1024")
  private Integer batchWindow = null;

  public JdbcBatchingDataCaptureService() {
    super();
  }

  public JdbcBatchingDataCaptureService(String statement) {
    this();
    setStatement(statement);
  }

  @Override
  protected void executeUpdate(PreparedStatement insert) throws SQLException {
    int count = counter.get().incrementAndGet();
    insert.addBatch();
    if (count % batchWindow() == 0) {
      log.trace("BatchWindow reached, executeBatch()");
      executeBatch(insert);
    }
  }

  @Override
  protected void finishUpdate(PreparedStatement insert) throws SQLException {
    executeBatch(insert);
    counter.set(new AtomicInteger());
  }

  private void executeBatch(PreparedStatement insert) throws SQLException {
    int[] rc = insert.executeBatch();
    List<Integer> result = Arrays.asList(ArrayUtils.toObject(rc));
    if (result.contains(Statement.EXECUTE_FAILED)) {
      throw new SQLException("Batch Execution Failed.");
    }
  }

  /**
   * @return the batchWindow
   */
  public Integer getBatchWindow() {
    return batchWindow;
  }

  /**
   * Set the batch window for operations.
   * 
   * @param i the batchWindow to set; default is {@value #DEFAULT_BATCH_WINDOW} if not specified.
   */
  public void setBatchWindow(Integer i) {
    this.batchWindow = i;
  }

  int batchWindow() {
    return getBatchWindow() != null ? getBatchWindow().intValue() : DEFAULT_BATCH_WINDOW;
  }

}
