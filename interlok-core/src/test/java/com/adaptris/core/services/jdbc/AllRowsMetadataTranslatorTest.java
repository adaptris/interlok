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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;

public class AllRowsMetadataTranslatorTest extends JdbcQueryServiceCase {

  private static final String ALL_ROWS_QUERY = "SELECT adapter_version, message_translator_type FROM adapter_type_version ";
  private static final String ALL_ROWS_QUERY_ALIASES =
      "SELECT adapter_version as MY_VERSION, message_translator_type as MY_TYPE FROM adapter_type_version ";
  
  private static final String ALL_ROWS_QUERY_NAMED_PARAMS =
      "SELECT adapter_version, message_translator_type FROM adapter_type_version " + "WHERE adapter_version!=#adapterVersion";

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testJdbcDataQueryService() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    s.setStatementCreator(new ConfiguredSQLStatement(ALL_ROWS_QUERY));
    s.getStatementParameters().clear();
    AllRowsMetadataTranslator t = new AllRowsMetadataTranslator();
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    String metadataKeyColumnVersion = t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION + t.getSeparator();
    String metadataKeyColumnType = t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE + t.getSeparator();
    for (int i = 0; i < 10; i++) {
      assertTrue(metadataKeyColumnVersion + i, msg.headersContainsKey(metadataKeyColumnVersion + i));
      assertTrue(metadataKeyColumnType + i, msg.headersContainsKey(metadataKeyColumnType + i));

    }

    assertFalse(msg.headersContainsKey(JdbcDataQueryService.class.getCanonicalName()));
  }

  @Test
  public void testJdbcDataQueryService_Aliases() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    s.setStatementCreator(new ConfiguredSQLStatement(ALL_ROWS_QUERY_ALIASES));
    s.getStatementParameters().clear();
    AllRowsMetadataTranslator t = new AllRowsMetadataTranslator();
    t.setResultCountMetadataItem("TotalRows");
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    String metadataKeyColumnVersion = t.getMetadataKeyPrefix() + t.getSeparator() + "MY_VERSION" + t.getSeparator();
    String metadataKeyColumnType = t.getMetadataKeyPrefix() + t.getSeparator() + "MY_TYPE" + t.getSeparator();
    for (int i = 0; i < 10; i++) {
      assertTrue(metadataKeyColumnVersion + i, msg.headersContainsKey(metadataKeyColumnVersion + i));
      assertTrue(metadataKeyColumnType + i, msg.headersContainsKey(metadataKeyColumnType + i));

    }

    assertFalse(msg.headersContainsKey(JdbcDataQueryService.class.getCanonicalName()));
    assertTrue(msg.headersContainsKey("TotalRows"));
    assertEquals("10", msg.getMetadataValue("TotalRows"));
  }

  @Test
  public void testJdbcDataQueryServiceWithResultCount() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    s.setStatementCreator(new ConfiguredSQLStatement(ALL_ROWS_QUERY));
    s.getStatementParameters().clear();
    AllRowsMetadataTranslator t = new AllRowsMetadataTranslator();
    t.setResultCountMetadataItem("resultCount");
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);

    assertTrue(msg.headersContainsKey("resultCount"));
    assertEquals("10", msg.getMetadataValue("resultCount"));
  }

  @Test
  public void testJdbcDataQueryServiceWithNamedParams() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    s.setStatementCreator(new ConfiguredSQLStatement(ALL_ROWS_QUERY_NAMED_PARAMS));
    
    StatementParameter adapterVersion = new StatementParameter("xxx", "java.lang.String", StatementParameter.QueryType.constant);
    adapterVersion.setName("adapterVersion");
    
    s.getStatementParameters().add(adapterVersion);
    s.setParameterApplicator(new NamedParameterApplicator());
    
    AllRowsMetadataTranslator t = new AllRowsMetadataTranslator();
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    String metadataKeyColumnVersion = t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION + t.getSeparator();
    String metadataKeyColumnType = t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE + t.getSeparator();
    for (int i = 0; i < 10; i++) {
      assertTrue(metadataKeyColumnVersion + i, msg.headersContainsKey(metadataKeyColumnVersion + i));
      assertTrue(metadataKeyColumnType + i, msg.headersContainsKey(metadataKeyColumnType + i));

    }

    assertFalse(msg.headersContainsKey(JdbcDataQueryService.class.getCanonicalName()));
  }

  @Test
  public void testServiceWithStyleUpperCase() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    s.setStatementCreator(new ConfiguredSQLStatement(ALL_ROWS_QUERY));
    s.getStatementParameters().clear();
    AllRowsMetadataTranslator t = new AllRowsMetadataTranslator();
    t.setColumnNameStyle(ResultSetTranslatorImp.ColumnStyle.UpperCase);
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    String metadataKeyColumnVersion = t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION.toUpperCase() + t.getSeparator();
    String metadataKeyColumnType = t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE.toUpperCase() + t.getSeparator();
    for (int i = 0; i < 10; i++) {
      assertTrue(metadataKeyColumnVersion + i, msg.headersContainsKey(metadataKeyColumnVersion + i));
      assertTrue(metadataKeyColumnType + i, msg.headersContainsKey(metadataKeyColumnType + i));

    }

  }

  @Test
  public void testServiceWithStyleLowerCase() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    s.setStatementCreator(new ConfiguredSQLStatement(ALL_ROWS_QUERY));
    s.getStatementParameters().clear();
    AllRowsMetadataTranslator t = new AllRowsMetadataTranslator();
    t.setColumnNameStyle(ResultSetTranslatorImp.ColumnStyle.LowerCase);

    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    String metadataKeyColumnVersion = t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION.toLowerCase() + t.getSeparator();
    String metadataKeyColumnType = t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE.toLowerCase() + t.getSeparator();
    System.out.println(msg);
    for (int i = 0; i < 10; i++) {
      assertTrue(metadataKeyColumnVersion + i, msg.headersContainsKey(metadataKeyColumnVersion + i));
      assertTrue(metadataKeyColumnType + i, msg.headersContainsKey(metadataKeyColumnType + i));

    }

  }

  @Test
  public void testServiceWithStyleCapitalize() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    s.setStatementCreator(new ConfiguredSQLStatement(ALL_ROWS_QUERY));
    s.getStatementParameters().clear();
    AllRowsMetadataTranslator t = new AllRowsMetadataTranslator();
    t.setColumnNameStyle(ResultSetTranslatorImp.ColumnStyle.Capitalize);
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    String metadataKeyColumnVersion = t.getMetadataKeyPrefix() + t.getSeparator() + StringUtils.capitalize(COLUMN_VERSION) + t.getSeparator();
    String metadataKeyColumnType = t.getMetadataKeyPrefix() + t.getSeparator() + StringUtils.capitalize(COLUMN_TYPE)
        + t.getSeparator();
    for (int i = 0; i < 10; i++) {
      assertTrue(metadataKeyColumnVersion + i, msg.headersContainsKey(metadataKeyColumnVersion + i));
      assertTrue(metadataKeyColumnType + i, msg.headersContainsKey(metadataKeyColumnType + i));
    }
  }

  @Override
  protected AllRowsMetadataTranslator createTranslatorForConfig() {
    return new AllRowsMetadataTranslator();
  }

}
