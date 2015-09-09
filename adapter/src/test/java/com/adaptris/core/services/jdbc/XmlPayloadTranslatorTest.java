package com.adaptris.core.services.jdbc;

import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.XmlUtils;

@SuppressWarnings("deprecation")
public class XmlPayloadTranslatorTest extends JdbcQueryServiceCaseXmlResults {

  public XmlPayloadTranslatorTest(String arg0) {
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
    s.setResultSetTranslator(new XmlPayloadTranslator());
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
    JdbcDataQueryService s = createXmlService();
    s.setResultSetTranslator(new XmlPayloadTranslator());
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertTrue(ADAPTER_ID_KEY + " exists", msg.containsKey(ADAPTER_ID_KEY));
    assertNotSame(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getStringPayload());
    assertFalse(msg.containsKey(JdbcDataQueryService.class.getCanonicalName()));
    XmlUtils xu = XmlHelper.createXmlUtils(msg);
    assertNull("Xpath /Results/OriginalMessage", xu.getSingleNode("/Results/OriginalMessage"));
    assertNotNull("/Results/Row", xu.getSingleNode("/Results/Row"));
  }

  public void testPreserveOriginal() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    XmlPayloadTranslator t = new XmlPayloadTranslator();
    t.setPreserveOriginalMessage(true);
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertTrue(ADAPTER_ID_KEY + " exists", msg.containsKey(ADAPTER_ID_KEY));
    XmlUtils xu = XmlHelper.createXmlUtils(msg);
    assertNotNull("Xpath /Results/OriginalMessage", xu.getSingleNode("/Results/OriginalMessage"));
    assertNotNull("/Results/Row", xu.getSingleNode("/Results/Row"));
  }


  public void testXpathStatementParamWithLowerCase() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createXmlService();
    XmlPayloadTranslator t = new XmlPayloadTranslator();
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

  @Override
  protected XmlPayloadTranslator createTranslatorForConfig() {
    XmlPayloadTranslator t = new XmlPayloadTranslator();
    t.setPreserveOriginalMessage(Boolean.TRUE);
    t.setStripIllegalXmlChars(true);
    t.setXmlColumnRegexp("Data_in_columns_that_match_this_regular_expression_will_be_turned_into_a_Document_Object_Before_Processing");
    return t;
  }

  @Override
  protected XmlPayloadTranslator createPayloadTranslator() {
    return new XmlPayloadTranslator();
  }

}
