package com.adaptris.core.services.jdbc;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.adaptris.core.AdaptrisMessage;

public class AllRowsMetadataTranslatorTest extends JdbcQueryServiceCase {

  private static final String ALL_ROWS_QUERY = "SELECT adapter_version, message_translator_type " + "FROM adapter_type_version ";
  
  private static final String ALL_ROWS_QUERY_NAMED_PARAMS = "SELECT adapter_version, message_translator_type " + "FROM adapter_type_version " + "WHERE adapter_version!=#adapterVersion";

  public AllRowsMetadataTranslatorTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {

  }

  public void testJdbcDataQueryService() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    s.setStatement(ALL_ROWS_QUERY);
    s.getStatementParameters().clear();
    AllRowsMetadataTranslator t = new AllRowsMetadataTranslator();
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getStringPayload());
    String metadataKeyColumnVersion = t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION + t.getSeparator();
    String metadataKeyColumnType = t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE + t.getSeparator();
    for (int i = 0; i < 10; i++) {
      assertTrue(metadataKeyColumnVersion + i, msg.containsKey(metadataKeyColumnVersion + i));
      assertTrue(metadataKeyColumnType + i, msg.containsKey(metadataKeyColumnType + i));

    }

    assertFalse(msg.containsKey(JdbcDataQueryService.class.getCanonicalName()));
  }
  
  public void testJdbcDataQueryServiceWithNamedParams() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    s.setStatement(ALL_ROWS_QUERY_NAMED_PARAMS);
    
    StatementParameter adapterVersion = new StatementParameter("xxx", "java.lang.String", StatementParameter.QueryType.constant);
    adapterVersion.setName("adapterVersion");
    
    s.getStatementParameters().add(adapterVersion);
    s.setParameterApplicator(new NamedParameterApplicator());
    
    AllRowsMetadataTranslator t = new AllRowsMetadataTranslator();
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getStringPayload());
    String metadataKeyColumnVersion = t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION + t.getSeparator();
    String metadataKeyColumnType = t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE + t.getSeparator();
    for (int i = 0; i < 10; i++) {
      assertTrue(metadataKeyColumnVersion + i, msg.containsKey(metadataKeyColumnVersion + i));
      assertTrue(metadataKeyColumnType + i, msg.containsKey(metadataKeyColumnType + i));

    }

    assertFalse(msg.containsKey(JdbcDataQueryService.class.getCanonicalName()));
  }

  public void testServiceWithStyleUpperCase() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    s.setStatement(ALL_ROWS_QUERY);
    s.getStatementParameters().clear();
    AllRowsMetadataTranslator t = new AllRowsMetadataTranslator();
    t.setColumnNameStyle(ResultSetTranslatorImp.ColumnStyle.UpperCase);
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getStringPayload());
    String metadataKeyColumnVersion = t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION.toUpperCase() + t.getSeparator();
    String metadataKeyColumnType = t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE.toUpperCase() + t.getSeparator();
    for (int i = 0; i < 10; i++) {
      assertTrue(metadataKeyColumnVersion + i, msg.containsKey(metadataKeyColumnVersion + i));
      assertTrue(metadataKeyColumnType + i, msg.containsKey(metadataKeyColumnType + i));

    }

  }

  public void testServiceWithStyleLowerCase() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    s.setStatement(ALL_ROWS_QUERY);
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
      assertTrue(metadataKeyColumnVersion + i, msg.containsKey(metadataKeyColumnVersion + i));
      assertTrue(metadataKeyColumnType + i, msg.containsKey(metadataKeyColumnType + i));

    }

  }

  public void testServiceWithStyleCapitalize() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    s.setStatement(ALL_ROWS_QUERY);
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
      assertTrue(metadataKeyColumnVersion + i, msg.containsKey(metadataKeyColumnVersion + i));
      assertTrue(metadataKeyColumnType + i, msg.containsKey(metadataKeyColumnType + i));
    }
  }

  @Override
  protected AllRowsMetadataTranslator createTranslatorForConfig() {
    return new AllRowsMetadataTranslator();
  }

}
