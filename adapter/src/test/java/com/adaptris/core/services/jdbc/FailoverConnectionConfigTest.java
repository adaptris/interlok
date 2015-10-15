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

import com.adaptris.core.jdbc.FailoverJdbcConnection;
import com.adaptris.util.TimeInterval;

public class FailoverConnectionConfigTest extends JdbcServiceExample {
  private static final String METADATA_VALUE = "Any Old Value";
  private static final String METADATA_KEY = "jdbcDataCaptureServiceTest";

  public FailoverConnectionConfigTest(String arg0) {
    super(arg0);
  }

  // do not run the state checks in ServiceCase.
  protected boolean doStateTests() {
    return false;
  }
  
  @Override
  protected Object retrieveObjectForSampleConfig() {
    JdbcDataCaptureService service = new JdbcDataCaptureService();
    FailoverJdbcConnection connection = new FailoverJdbcConnection();
    connection.addConnectUrl("jdbc:mysql://localhost:3306/mydatabase");
    connection.addConnectUrl("jdbc:mysql://127.0.0.1:3306/mydatabase");
    connection.addConnectUrl("jdbc:mysql://some.other.machine:3306/mydatabase");
    connection.setConnectionAttempts(2);
    connection.setConnectionRetryInterval(new TimeInterval(3L, "SECONDS"));
    service.setConnection(connection);
    service.setStatement("insert into mytable (segment_id) values (?)");
    DateStatementParameter dp = new DateStatementParameter();
    dp.setQueryType(StatementParameter.QueryType.xpath);
    dp.setQueryString("Segment[@id='PO1']");
    service.addStatementParameter(dp);
    return service;
  }

  @Override
  protected String createBaseFileName(Object o) {
    return "com.adaptris.core.services.jdbc.JdbcService-" + FailoverJdbcConnection.class.getSimpleName();

  }

  @Override
  protected String getExampleCommentHeader(Object object) {
    return super.getExampleCommentHeader(object) + "\n<!-- \n"
        + "This example simply shows a JdbcDataCaptureService with a 'FailoverJdbcConnection'"
        + "\nwhich allows you to failover to one or more other databases if the first connection" + "\nfails" + "\n-->\n";
  }

  @Override
  public void testBackReferences() throws Exception {
    JdbcDataCaptureService service = new JdbcDataCaptureService();
    FailoverJdbcConnection connection = new FailoverJdbcConnection();
    connection.addConnectUrl("jdbc:mysql://localhost:3306/mydatabase");
    connection.addConnectUrl("jdbc:mysql://127.0.0.1:3306/mydatabase");
    connection.addConnectUrl("jdbc:mysql://some.other.machine:3306/mydatabase");
    service.setConnection(connection);
    this.testBackReferences(service);

  }
}
