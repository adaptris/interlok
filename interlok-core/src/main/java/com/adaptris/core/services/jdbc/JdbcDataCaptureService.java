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

import javax.xml.namespace.NamespaceContext;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.jdbc.DatabaseConnection;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.util.text.xml.XPath;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Capture Data from a AdaptrisMessage and store it in a JDBC-compliant database.
 * <p>
 * While not deprecated; you are encouraged to use {@link JdbcBatchingDataCaptureService} instead for performance reasons. Set the
 * {@link JdbcBatchingDataCaptureService#setBatchWindow(Integer)} to {@code 1} to have functionally equivalent behaviour to this
 * class.
 * </p>
 * <p>
 * If the {@code DocumentBuilderFactoryBuilder} has been explicitly set to be not namespace aware and the document does in fact
 * contain namespaces, then Saxon can cause merry havoc in the sense that {@code //NonNamespaceXpath} doesn't work if the document
 * has namespaces in it. We have included a shim so that behaviour can be toggled based on what you have configured.
 * </p>
 *
 * @see XPath#newXPathInstance(DocumentBuilderFactoryBuilder, NamespaceContext)
 * @config jdbc-data-capture-service
 * @author sellidge
 */
@XStreamAlias("jdbc-data-capture-service")
@AdapterComponent
@ComponentProfile(summary = "Capture data from the message and store it in a database", tag = "service,jdbc",
recommended = {DatabaseConnection.class})
@DisplayOrder(
    order = {"connection", "statement", "iterationXpath", "iterates", "rowsUpdatedMetadataKey",
        "statementParameters", "parameterApplicator", "xmlDocumentFactoryConfig",
        "namespaceContext", "saveReturnedKeys", "saveReturnedKeysColumn", "saveReturnedKeysTable"})
public class JdbcDataCaptureService extends JdbcIteratingDataCaptureServiceImpl {

  public JdbcDataCaptureService() {
    super();
  }


  public JdbcDataCaptureService(String statement) {
    this();
    setStatement(statement);
  }

  @Override
  protected long executeUpdate(PreparedStatement insert) throws SQLException {
    return insert.executeUpdate();
  }

  @Override
  protected long finishUpdate(PreparedStatement insert) throws SQLException {
    return 0;
  }
}
