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
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.NullConnection;
import com.adaptris.core.stubs.DummyMessageProducer;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.XmlUtils;

@SuppressWarnings("deprecation")
public class SplittingXmlPayloadTranslatorTest extends JdbcQueryServiceCaseXmlResults {

  private MockMessageProducer producer;
  
  public SplittingXmlPayloadTranslatorTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {
    producer = new MockMessageProducer();
  }

  public void testMetadataStatementParam() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    
    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    s.setResultSetTranslator(createPayloadTranslator());
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertTrue(msg.headersContainsKey(ADAPTER_ID_KEY));
    assertFalse(msg.headersContainsKey(JdbcDataQueryService.class.getCanonicalName()));
    AdaptrisMessage outputMessage = producer.getMessages().get(0);
    XmlUtils xu = XmlHelper.createXmlUtils(outputMessage);
    assertNull("Xpath /Results/OriginalMessage", xu.getSingleNode("/Results/OriginalMessage"));
    assertNotNull("/Results/Row missing.", xu.getSingleNode("/Results/Row"));
  }

  public void testXpathStatementParam() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createXmlService();
    s.setResultSetTranslator(createPayloadTranslator());
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertTrue(msg.headersContainsKey(ADAPTER_ID_KEY));
    assertFalse(msg.headersContainsKey(JdbcDataQueryService.class.getCanonicalName()));
    AdaptrisMessage outputMessage = producer.getMessages().get(0);
    XmlUtils xu = XmlHelper.createXmlUtils(outputMessage);
    assertNull("Xpath /Results/OriginalMessage", xu.getSingleNode("/Results/OriginalMessage"));
    assertNotNull("/Results/Row", xu.getSingleNode("/Results/Row"));
  }

  public void testXpathStatementParamWithLowerCase() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createXmlService();
    SplittingXmlPayloadTranslator t = createPayloadTranslator();
    t.setColumnNameStyle(ResultSetTranslatorImp.ColumnStyle.LowerCase);
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertTrue(msg.headersContainsKey(ADAPTER_ID_KEY));
    assertFalse(msg.headersContainsKey(JdbcDataQueryService.class.getCanonicalName()));
    AdaptrisMessage outputMessage = producer.getMessages().get(0);
    XmlUtils xu = XmlHelper.createXmlUtils(outputMessage);
    log.warn(msg.getContent());
    assertNull("Xpath /Results/OriginalMessage", xu.getSingleNode("/results/originalmessage"));
    assertNotNull("/Results/Row", xu.getSingleNode("/results/row"));
  }
  
  @Override
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
    
    AdaptrisMessage outputMessage = producer.getMessages().get(0);
    logMessage(getName(), outputMessage);
    assertEquals("UTF-8", outputMessage.getContentEncoding());
  }

  @Override
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
    
    AdaptrisMessage outputMessage = producer.getMessages().get(0);
    logMessage(getName(), outputMessage);
    assertEquals("UTF-8", outputMessage.getContentEncoding());
  }
  
  @Override
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
    
    AdaptrisMessage outputMessage = producer.getMessages().get(0);
    logMessage(getName(), outputMessage);
    try {
      XmlHelper.createXmlUtils(outputMessage, null);
      fail();
    }
    catch (CoreException e) {
    }
  }
  
  public void testMultipleResultMessages() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(11);
    AdapterTypeVersion entry = dbItems.get(0);
    
    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMultiService();
    SplittingXmlPayloadTranslator translator = createPayloadTranslator().withMaxRowsPerMessage(2);
    s.setResultSetTranslator(translator);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertTrue(msg.headersContainsKey(ADAPTER_ID_KEY));
    assertFalse(msg.headersContainsKey(JdbcDataQueryService.class.getCanonicalName()));
    
    List<AdaptrisMessage> outputMessages = producer.getMessages();
    assertEquals(6, outputMessages.size());

    int count = 0;
    for(AdaptrisMessage outputMessage: outputMessages) {
      XmlUtils xu = XmlHelper.createXmlUtils(outputMessage);
      assertNull("Xpath /Results/OriginalMessage", xu.getSingleNode("/Results/OriginalMessage"));
      assertEquals("/Results/Row", count<5 ? 2 : 1, 
          xu.getNodeList("/Results/Row").getLength());
      assertFalse(outputMessage.headersContainsKey(ADAPTER_ID_KEY));
      count++;
    }
  }

  public void testMultipleResultMessages_CopyMetadata() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(11);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMultiService();
    SplittingXmlPayloadTranslator translator =
        createPayloadTranslator().withMaxRowsPerMessage(2).withCopyMetadata(true)
            .withMessageFactory(new DefaultMessageFactory());
    s.setResultSetTranslator(translator);
    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertTrue(msg.headersContainsKey(ADAPTER_ID_KEY));
    assertFalse(msg.headersContainsKey(JdbcDataQueryService.class.getCanonicalName()));

    List<AdaptrisMessage> outputMessages = producer.getMessages();
    assertEquals(6, outputMessages.size());

    int count = 0;
    for (AdaptrisMessage outputMessage : outputMessages) {
      XmlUtils xu = XmlHelper.createXmlUtils(outputMessage);
      assertNull("Xpath /Results/OriginalMessage", xu.getSingleNode("/Results/OriginalMessage"));
      assertEquals("/Results/Row", count < 5 ? 2 : 1, xu.getNodeList("/Results/Row").getLength());
      assertTrue(outputMessage.headersContainsKey(ADAPTER_ID_KEY));
      count++;
    }
  }

  @Override
  protected SplittingXmlPayloadTranslator createTranslatorForConfig() {
    SplittingXmlPayloadTranslator t = new SplittingXmlPayloadTranslator();
    t.setProducer(new DummyMessageProducer());
    t.setStripIllegalXmlChars(true);
    t.setXmlColumnRegexp("Data_in_columns_that_match_this_regular_expression_will_be_turned_into_a_Document_Object_Before_Processing");
    return t;
  }

  @Override
  protected SplittingXmlPayloadTranslator createPayloadTranslator() {
    return new SplittingXmlPayloadTranslator()
        .withConnection(new NullConnection()).withProducer(producer);
  }

}
