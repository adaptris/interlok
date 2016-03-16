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

import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.jdbc.JdbcConnection;
import com.adaptris.core.stubs.DummyMessageProducer;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.XmlUtils;

@SuppressWarnings("deprecation")
public class SplittingXmlPayloadTranslatorTest extends JdbcQueryServiceCaseXmlResults {

  public SplittingXmlPayloadTranslatorTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {

  }

  public void testMetadataStatementParam() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    s.setResultSetTranslator(new SplittingXmlPayloadTranslator());
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertTrue(ADAPTER_ID_KEY + " exists", msg.containsKey(ADAPTER_ID_KEY));
    assertNotSame(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getStringPayload());
    assertFalse(msg.containsKey(JdbcDataQueryService.class.getCanonicalName()));
    XmlUtils xu = XmlHelper.createXmlUtils(msg);
    assertNull("Xpath /Results/OriginalMessage", xu.getSingleNode("/Results/OriginalMessage"));
    assertNotNull("/Results/Row", xu.getSingleNode("/Results/Row"));
  }

  public void testXpathStatementParam() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    SplittingJdbcDataQueryService s = createXmlSplittingService();
    s.setResultSetTranslator(new SplittingXmlPayloadTranslator());
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertTrue(ADAPTER_ID_KEY + " exists", msg.containsKey(ADAPTER_ID_KEY));
    assertNotSame(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getStringPayload());
    assertFalse(msg.containsKey(JdbcDataQueryService.class.getCanonicalName()));
    XmlUtils xu = XmlHelper.createXmlUtils(msg);
    assertNull("Xpath /Results/OriginalMessage", xu.getSingleNode("/Results/OriginalMessage"));
    assertNotNull("/Results/Row", xu.getSingleNode("/Results/Row"));
  }

  public void testXpathStatementParamWithLowerCase() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    SplittingJdbcDataQueryService s = createXmlSplittingService();
    SplittingXmlPayloadTranslator t = new SplittingXmlPayloadTranslator();
    t.setColumnNameStyle(ResultSetTranslatorImp.ColumnStyle.LowerCase);
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertTrue(ADAPTER_ID_KEY + " exists", msg.containsKey(ADAPTER_ID_KEY));
    assertNotSame(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getStringPayload());
    assertFalse(msg.containsKey(JdbcDataQueryService.class.getCanonicalName()));
    XmlUtils xu = XmlHelper.createXmlUtils(msg);
    log.warn(msg.getStringPayload());
    assertNull("Xpath /Results/OriginalMessage", xu.getSingleNode("/results/originalmessage"));
    assertNotNull("/Results/Row", xu.getSingleNode("/results/row"));
  }
  
  protected static SplittingJdbcDataQueryService createMetadataSplittingService() {
    return createMetadataSplittingService(true);
  }
  
  protected static SplittingJdbcDataQueryService createMetadataSplittingService(boolean createConnection) {
    SplittingJdbcDataQueryService service = new SplittingJdbcDataQueryService();
    if (createConnection) {
      JdbcConnection connection = new JdbcConnection(PROPERTIES.getProperty(JDBC_QUERYSERVICE_URL),
          PROPERTIES.getProperty(JDBC_QUERYSERVICE_DRIVER));
      service.setConnection(connection);
    }
    service.setStatement(QUERY_SQL);
    StatementParameter sp = new StatementParameter();
    sp.setQueryClass("java.lang.String");
    sp.setQueryType(StatementParameter.QueryType.metadata);
    sp.setQueryString(ADAPTER_ID_KEY);
    service.addStatementParameter(sp);

    return service;
  }
  
  protected static SplittingJdbcDataQueryService createXmlSplittingService() {
    SplittingJdbcDataQueryService service = new SplittingJdbcDataQueryService();
    JdbcConnection connection = new JdbcConnection(PROPERTIES.getProperty(JDBC_QUERYSERVICE_URL),
        PROPERTIES.getProperty(JDBC_QUERYSERVICE_DRIVER));
    service.setProducer(new DummyMessageProducer());
    service.setConnection(connection);
    service.setStatement(QUERY_SQL);
    StatementParameter sp = new StatementParameter();
    sp.setQueryClass("java.lang.String");
    sp.setQueryType(StatementParameter.QueryType.xpath);
    sp.setQueryString("/root/document");
    service.addStatementParameter(sp);
    return service;
  }

  @Override
  protected SplittingXmlPayloadTranslator createTranslatorForConfig() {
    SplittingXmlPayloadTranslator t = new SplittingXmlPayloadTranslator();
    t.setStripIllegalXmlChars(true);
    t.setXmlColumnRegexp("Data_in_columns_that_match_this_regular_expression_will_be_turned_into_a_Document_Object_Before_Processing");
    t.setMaxRowsPerMessage(10);
    return t;
  }

  @Override
  protected SplittingXmlPayloadTranslator createPayloadTranslator() {
    return new SplittingXmlPayloadTranslator();
  }

}
