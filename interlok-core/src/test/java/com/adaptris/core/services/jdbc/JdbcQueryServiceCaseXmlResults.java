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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.services.LoggingServiceImpl.LoggingLevel;
import com.adaptris.core.services.jdbc.StyledResultTranslatorImp.ColumnStyle;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

@SuppressWarnings("deprecation")
public abstract class JdbcQueryServiceCaseXmlResults extends JdbcQueryServiceCase {

  private static final String QUERY_SQL_ILLEGAL_COLUMN = "SELECT adapter_version AS \"adapter version\", message_translator_type, inserted_on, counter"
      + " FROM adapter_type_version " + " WHERE adapter_unique_id = ?";

  private static final String QUERY_SQL_ILLEGAL_BLANK_COLUMN = "SELECT adapter_version AS \" \", message_translator_type, inserted_on, counter"
      + " FROM adapter_type_version " + " WHERE adapter_unique_id = ?";

  protected abstract XmlPayloadTranslatorImpl createPayloadTranslator();

  @Test
  public void testSetEncoding() throws Exception {
    XmlPayloadTranslatorImpl translator = createPayloadTranslator();
    assertNull(translator.getOutputMessageEncoding());
    translator.setOutputMessageEncoding("ISO-8859-1");
    assertEquals("ISO-8859-1", translator.getOutputMessageEncoding());
  }

  @Test
  public void testSetNamespaceContext() throws Exception {
    JdbcDataQueryService service = new JdbcDataQueryService();
    assertNull(service.getNamespaceContext());
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.add(new KeyValuePair("hello", "world"));
    service.setNamespaceContext(kvps);
    assertEquals(kvps, service.getNamespaceContext());
    service.setNamespaceContext(null);
    assertNull(service.getNamespaceContext());
  }

  @Test
  public void testDoService_WithEncoding() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);
    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createXmlService();
    XmlPayloadTranslatorImpl translator = createPayloadTranslator();
    translator.setOutputMessageEncoding("UTF-8");
    s.setResultSetTranslator(translator);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    logMessage(getName(), msg);
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  @Test
  public void testDoService_WithResultCount() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);
    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createXmlService();
    XmlPayloadTranslatorImpl translator = createPayloadTranslator();
    translator.setOutputMessageEncoding("UTF-8");
    translator.setResultCountMetadataItem("resultCount");
    s.setResultSetTranslator(translator);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    logMessage(getName(), msg);
    assertTrue(msg.headersContainsKey("resultCount"));
    assertEquals("1", msg.getMetadataValue("resultCount"));
  }

  @Test
  public void testDoService_WithEncodingUnspecified() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);
    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createXmlService();
    XmlPayloadTranslatorImpl translator = createPayloadTranslator();
    s.setResultSetTranslator(translator);
    AdaptrisMessage msg = createMessage(AdaptrisMessageFactory.getDefaultInstance(), null, entry);
    execute(s, msg);
    logMessage(getName(), msg);

    assertEquals("UTF-8", msg.getContentEncoding());
  }

  @Test
  public void testDoService_WithEncodingFromMsgFactory() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);
    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createXmlService();
    XmlPayloadTranslatorImpl translator = createPayloadTranslator();
    s.setResultSetTranslator(translator);
    DefaultMessageFactory mf = new DefaultMessageFactory();
    mf.setDefaultCharEncoding("UTF-8");
    AdaptrisMessage msg = createMessage(mf, null, entry);
    execute(s, msg);
    logMessage(getName(), msg);
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  protected void logMessage(String prefix, AdaptrisMessage msg) throws Exception {
    execute( new LogMessageService(LoggingLevel.TRACE, prefix), msg);
  }

  @Test
  public void testDoService_XmlColumnPrefix() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generateWithXmlColumn(10);
    AdapterTypeVersion entry = dbItems.get(0);
    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createXmlService();
    XmlPayloadTranslatorImpl translator = createPayloadTranslator();
    translator.setXmlColumnPrefix("ADAPTER");
    translator.setOutputMessageEncoding("UTF-8");
    s.setResultSetTranslator(translator);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    logMessage(getName(), msg);
  }

  @Test
  public void testDoService_XmlColumnRegexp() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generateWithXmlColumn(10);
    AdapterTypeVersion entry = dbItems.get(0);
    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createXmlService();
    XmlPayloadTranslatorImpl translator = createPayloadTranslator();
    translator.setXmlColumnRegexp("ADAPTER.*");
    translator.setOutputMessageEncoding("UTF-8");
    s.setResultSetTranslator(translator);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    logMessage(getName(), msg);
  }

  @Test
  public void testDoService_XmlColumn_NotXml() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);
    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createXmlService();
    XmlPayloadTranslatorImpl translator = createPayloadTranslator();
    translator.setXmlColumnRegexp("ADAPTER.*");
    translator.setOutputMessageEncoding("UTF-8");
    s.setResultSetTranslator(translator);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    logMessage(getName(), msg);
  }

  @Test
  public void testDoService_XmlColumn_NotXml_DisplayErrors() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);
    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createXmlService();
    XmlPayloadTranslatorImpl translator = createPayloadTranslator();
    translator.setDisplayColumnErrors(true);
    translator.setXmlColumnRegexp("ADAPTER.*");
    translator.setOutputMessageEncoding("UTF-8");
    s.setResultSetTranslator(translator);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    logMessage(getName(), msg);
  }

  @Test
  public void testDoService_CDATA() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);
    populateDatabase(dbItems, true, true);
    JdbcDataQueryService s = createXmlService();
    XmlPayloadTranslatorImpl translator = createPayloadTranslator();
    translator.setCdataColumnRegexp(".*ADAPTER.*");
    translator.setOutputMessageEncoding("UTF-8");
    s.setResultSetTranslator(translator);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    logMessage(getName(), msg);
    System.err.println(msg.getContent());
    assertTrue(msg.getContent().contains("<![CDATA["));
  }

  @Test
  public void testDoService_IllegalXmlCharacters() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generateWithIllegalXmlChars(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createXmlService();
    XmlPayloadTranslatorImpl translator = createPayloadTranslator();
    translator.setStripIllegalXmlChars(false);
    s.setResultSetTranslator(translator);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    logMessage(getName(), msg);
    try {
      XmlHelper.createXmlUtils(msg, null);
      fail();
    }
    catch (CoreException e) {
    }
  }

  @Test
  public void testDoService_IllegalXmlCharacters_Stripped() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generateWithIllegalXmlChars(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createXmlService();
    XmlPayloadTranslatorImpl translator = createPayloadTranslator();
    translator.setStripIllegalXmlChars(true);
    s.setResultSetTranslator(translator);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    logMessage(getName(), msg);
    // Illegals should have been stripped, so this should be OK for a given range of "OK"
    XmlHelper.createXmlUtils(msg, null);
  }

  @Test
  public void testDoService_IllegalElementNames_ColumnHasSpaces() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);
    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createXmlService();
    XmlPayloadTranslatorImpl translator = createPayloadTranslator();
    translator.setColumnNameStyle(ColumnStyle.UpperCase);
    s.setResultSetTranslator(translator);
    s.setStatementCreator(new ConfiguredSQLStatement(QUERY_SQL_ILLEGAL_COLUMN));
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    logMessage(getName(), msg);
    // Illegals should have been stripped, so this should be OK for a given range of "OK"
    XmlHelper.createXmlUtils(msg, null);
  }

  @Test
  public void testDoService_IllegalElementNames_BlankElementName() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);
    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createXmlService();
    XmlPayloadTranslatorImpl translator = createPayloadTranslator();
    translator.setStripIllegalXmlChars(true);
    translator.setColumnNameStyle(ColumnStyle.UpperCase);
    s.setResultSetTranslator(translator);
    s.setStatementCreator(new ConfiguredSQLStatement(QUERY_SQL_ILLEGAL_BLANK_COLUMN));
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    logMessage(getName(), msg);
    // Illegals should have been stripped, so this should be OK for a given range of "OK"
    XmlHelper.createXmlUtils(msg, null);
  }

  protected static List<AdapterTypeVersion> generateWithXmlColumn(int max) throws Exception {
    GuidGenerator guid = new GuidGenerator();
    String tagStart = "<xml-tag>";
    String tagEnd = "</xml-tag>";
    List<AdapterTypeVersion> result = new ArrayList<AdapterTypeVersion>(max);
    new String(new byte[]
    {
      0x02
    });
    for (int i = 0; i < max; i++) {
      AdapterTypeVersion atv = new AdapterTypeVersion(guid.safeUUID(), tagStart + guid.safeUUID() + tagEnd, guid.safeUUID(), i);
      atv.setDate(new Date());
      result.add(atv);
    }
    return result;
  }

  protected static List<AdapterTypeVersion> generateWithIllegalXmlChars(int max) throws Exception {
    GuidGenerator guid = new GuidGenerator();
    List<AdapterTypeVersion> result = new ArrayList<AdapterTypeVersion>(max);
    String illegal = new String(new byte[]
    {
      0x02
    });
    for (int i = 0; i < max; i++) {
      AdapterTypeVersion atv = new AdapterTypeVersion(guid.safeUUID(), illegal + "_" + guid.safeUUID(), guid.safeUUID(), i);
      atv.setDate(new Date());
      result.add(atv);
    }
    return result;
  }

}
