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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import org.w3c.dom.Document;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.services.jdbc.StyledResultTranslatorImp.ColumnStyle;
import com.adaptris.core.services.jdbc.types.IntegerColumnTranslator;
import com.adaptris.core.services.jdbc.types.StringColumnTranslator;
import com.adaptris.core.services.jdbc.types.TimestampColumnTranslator;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.XmlUtils;
import com.adaptris.util.text.xml.XPath;

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
    assertNotSame(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
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
    assertNotSame(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    assertFalse(msg.containsKey(JdbcDataQueryService.class.getCanonicalName()));
    System.out.println(msg.getContent());
    XPath xp = new XPath();
    Document xmlDoc = XmlHelper.createDocument(msg, DocumentBuilderFactoryBuilder.newInstance());
    assertNull(xp.selectSingleNode(xmlDoc, "/Results/OriginalMessage"));
    assertNotNull(xp.selectSingleNode(xmlDoc, "/Results/Row"));
    assertEquals(entry.getVersion(),
        xp.selectSingleTextItem(xmlDoc, "/Results/Row/ADAPTER_VERSION"));
    assertEquals(String.valueOf(entry.getCounter()),
        xp.selectSingleTextItem(xmlDoc, "/Results/Row/COUNTER"));
    assertEquals(entry.getTranslatorType(),
        xp.selectSingleTextItem(xmlDoc, "/Results/Row/MESSAGE_TRANSLATOR_TYPE"));
    // Convert date into a Timestamp for toString purposes, as it's different from Date.toString()
    assertEquals(new Timestamp(entry.getDate().getTime()).toString(),
        xp.selectSingleTextItem(xmlDoc, "/Results/Row/INSERTED_ON"));
  }

  public void testService_XpathParam_WithTranslators() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);
    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createXmlService();
    s.setResultSetTranslator(new XmlPayloadTranslator().withColumnTranslators(
        new StringColumnTranslator(), new StringColumnTranslator(), new TimestampColumnTranslator(),
        new IntegerColumnTranslator()).withColumnNameStyle(ColumnStyle.NoStyle));
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    XPath xp = new XPath();
    Document xmlDoc = XmlHelper.createDocument(msg, DocumentBuilderFactoryBuilder.newInstance());
    assertNull(xp.selectSingleNode(xmlDoc, "/Results/OriginalMessage"));
    assertNotNull(xp.selectSingleNode(xmlDoc, "/Results/Row"));
    assertEquals(entry.getVersion(),
        xp.selectSingleTextItem(xmlDoc, "/Results/Row/ADAPTER_VERSION"));
    assertEquals(String.valueOf(entry.getCounter()),
        xp.selectSingleTextItem(xmlDoc, "/Results/Row/COUNTER"));
    assertEquals(entry.getTranslatorType(),
        xp.selectSingleTextItem(xmlDoc, "/Results/Row/MESSAGE_TRANSLATOR_TYPE"));
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    assertEquals(df.format(entry.getDate()),
        xp.selectSingleTextItem(xmlDoc, "/Results/Row/INSERTED_ON"));
  }

  // This should effectively give the same results as testService_WithTranslators...
  public void testService_XpathParam_WithAutoConvert() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);
    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createXmlService();
    s.setResultSetTranslator(new XmlPayloadTranslator()
        .withAttemptAutoConvert(true)
        .withColumnNameStyle(ColumnStyle.NoStyle));
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    XPath xp = new XPath();
    Document xmlDoc = XmlHelper.createDocument(msg, DocumentBuilderFactoryBuilder.newInstance());
    assertNull(xp.selectSingleNode(xmlDoc, "/Results/OriginalMessage"));
    assertNotNull(xp.selectSingleNode(xmlDoc, "/Results/Row"));
    assertEquals(entry.getVersion(),
        xp.selectSingleTextItem(xmlDoc, "/Results/Row/ADAPTER_VERSION"));
    assertEquals(String.valueOf(entry.getCounter()),
        xp.selectSingleTextItem(xmlDoc, "/Results/Row/COUNTER"));
    assertEquals(entry.getTranslatorType(),
        xp.selectSingleTextItem(xmlDoc, "/Results/Row/MESSAGE_TRANSLATOR_TYPE"));
    // This should have been done by a TimestampColumnTranslator...
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    assertEquals(df.format(entry.getDate()),
        xp.selectSingleTextItem(xmlDoc, "/Results/Row/INSERTED_ON"));
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

  public void testContainsRowCount() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    XmlPayloadTranslator t = new XmlPayloadTranslator();
    t.setResultCountMetadataItem(getName());
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertTrue(ADAPTER_ID_KEY + " exists", msg.containsKey(ADAPTER_ID_KEY));
    XmlUtils xu = XmlHelper.createXmlUtils(msg);
    assertNull("Xpath /Results/OriginalMessage", xu.getSingleNode("/Results/OriginalMessage"));
    assertNotNull("/Results/Row", xu.getSingleNode("/Results/Row"));
    assertTrue(msg.containsKey(getName()));
    assertEquals("1", msg.getMetadataValue(getName()));
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
