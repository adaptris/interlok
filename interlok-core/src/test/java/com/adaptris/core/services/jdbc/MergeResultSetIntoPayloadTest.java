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
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.XmlUtils;
import com.adaptris.util.text.xml.InsertNode;

@SuppressWarnings("deprecation")
public class MergeResultSetIntoPayloadTest extends JdbcQueryServiceCaseXmlResults {

  public MergeResultSetIntoPayloadTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {

  }

  @Override
  public void testBug1762() throws Exception {
    // Because we are merging XML into XML, this is not a valid test.
  }

  @Override
  public void testSetEncoding() throws Exception {
    MergeResultSetIntoXmlPayload translator = new MergeResultSetIntoXmlPayload();
    assertNull(translator.getOutputMessageEncoding());
    translator.setOutputMessageEncoding("ISO-8859-1");
    assertEquals("ISO-8859-1", translator.getOutputMessageEncoding());
  }

  public void testMetadataStatementParam() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    s.setResultSetTranslator(new MergeResultSetIntoXmlPayload(new InsertNode("/root")));
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertTrue(ADAPTER_ID_KEY + " exists", msg.containsKey(ADAPTER_ID_KEY));
    assertNotSame(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    assertFalse(msg.containsKey(JdbcDataQueryService.class.getCanonicalName()));
    XmlUtils xu = XmlHelper.createXmlUtils(msg);
    log.warn(msg.getContent());
    assertNotNull("/root/document", xu.getSingleNode("/root/document"));
    assertNotNull("/root/Results/Row", xu.getSingleNode("/root/Results/Row"));
  }


  public void testContainsRowCount() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    MergeResultSetIntoXmlPayload t = new MergeResultSetIntoXmlPayload(new InsertNode("/root"));
    t.setResultCountMetadataItem(getName());
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertTrue(ADAPTER_ID_KEY + " exists", msg.containsKey(ADAPTER_ID_KEY));
    assertNotSame(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    assertFalse(msg.containsKey(JdbcDataQueryService.class.getCanonicalName()));
    XmlUtils xu = XmlHelper.createXmlUtils(msg);
    log.warn(msg.getContent());
    assertNotNull("/root/document", xu.getSingleNode("/root/document"));
    assertNotNull("/root/Results/Row", xu.getSingleNode("/root/Results/Row"));
    assertTrue(msg.containsKey(getName()));
    assertEquals("1", msg.getMetadataValue(getName()));
  }


  public void testXpathStatementParam() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createXmlService();
    s.setResultSetTranslator(new MergeResultSetIntoXmlPayload(new InsertNode("/root")));
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertTrue(ADAPTER_ID_KEY + " exists", msg.containsKey(ADAPTER_ID_KEY));
    assertNotSame(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    assertFalse(msg.containsKey(JdbcDataQueryService.class.getCanonicalName()));
    XmlUtils xu = XmlHelper.createXmlUtils(msg);
    assertNotNull("/root/document", xu.getSingleNode("/root/document"));
    assertNotNull("/root/Results/Row", xu.getSingleNode("/root/Results/Row"));
  }

  public void testXpathStatementParam_NodeDoesNotExist() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createXmlService();
    // /root/NonExistentNode, doesn't actually exist at this point.
    s.setResultSetTranslator(new MergeResultSetIntoXmlPayload(new InsertNode("/root/NonExistentNode")));
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertTrue(ADAPTER_ID_KEY + " exists", msg.containsKey(ADAPTER_ID_KEY));
    assertNotSame(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    assertFalse(msg.containsKey(JdbcDataQueryService.class.getCanonicalName()));
    XmlUtils xu = XmlHelper.createXmlUtils(msg);
    assertNotNull("/root/document doesn't exist", xu.getSingleNode("/root/document"));
    assertNotNull("/root/NonExistentNode/Results/Row doesn't exist", xu.getSingleNode("/root/NonExistentNode/Results/Row"));
  }

  public void testXpathStatementParamWithLowerCase() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createXmlService();
    MergeResultSetIntoXmlPayload t = new MergeResultSetIntoXmlPayload(new InsertNode("/root"));
    t.setColumnNameStyle(ResultSetTranslatorImp.ColumnStyle.LowerCase);
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertTrue(ADAPTER_ID_KEY + " exists", msg.containsKey(ADAPTER_ID_KEY));
    assertNotSame(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    assertFalse(msg.containsKey(JdbcDataQueryService.class.getCanonicalName()));
    XmlUtils xu = XmlHelper.createXmlUtils(msg);
    assertNotNull("/root/document", xu.getSingleNode("/root/document"));
    assertNotNull("/root/Results/Row", xu.getSingleNode("/root/results/row"));
  }

  @Override
  protected MergeResultSetIntoXmlPayload createTranslatorForConfig() {
    MergeResultSetIntoXmlPayload t = new MergeResultSetIntoXmlPayload(new InsertNode("/root"));
    t.setStripIllegalXmlChars(true);
    t.setXmlColumnRegexp("Data_in_columns_that_match_this_regular_expression_will_be_turned_into_a_Document_Object_Before_Processing");
    return t;
  }

  @Override
  protected MergeResultSetIntoXmlPayload createPayloadTranslator() {
    return new MergeResultSetIntoXmlPayload(new InsertNode("/root"));
  }

}
