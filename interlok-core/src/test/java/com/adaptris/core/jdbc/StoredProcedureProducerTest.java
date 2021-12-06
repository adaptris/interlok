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

package com.adaptris.core.jdbc;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.lang3.BooleanUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.services.jdbc.AllRowsMetadataTranslator;
import com.adaptris.core.services.jdbc.FirstRowMetadataTranslator;
import com.adaptris.core.services.jdbc.JdbcServiceList;
import com.adaptris.core.services.jdbc.XmlPayloadTranslator;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.junit.scaffolding.ExampleProducerCase;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.adaptris.jdbc.ExecuteQueryCallableStatementExecutor;
import com.adaptris.jdbc.MysqlStatementCreator;
import com.adaptris.jdbc.ParameterValueType;
import com.adaptris.jdbc.SqlServerStatementCreator;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.XmlUtils;
import com.adaptris.util.text.xml.XPath;

public class StoredProcedureProducerTest extends ExampleProducerCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   *
   */
  public static final String BASE_DIR_KEY = "JdbcProducerExamples.baseDir";
  public static final String JDBC_STOREDPROC_TESTS_ENABLED = "jdbc.storedproc.tests.enabled";
  private static final String JDBC_DRIVER = "jdbc.storedproc.driver";
  private static final String JDBC_URL = "jdbc.storedproc.url";
  private static final String JDBC_USER = "jdbc.storedproc.username";
  private static final String JDBC_PASSWORD = "jdbc.storedproc.password";
  private static final String JDBC_VENDOR = "jdbc.storedproc.vendor";
  private static final String XML_PAYLOAD = "<head>" + "<body>" + "<element1>" + "Sold" + "</element1>" + "<element2>"
      + "SomeValue" + "</element2>" + "</body>" + "</head>";

  public StoredProcedureProducerTest() {
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }


  @Before
  public void setUp() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
  }

  private static boolean areTestsEnabled() {
    return BooleanUtils.toBoolean(PROPERTIES.getProperty(JDBC_STOREDPROC_TESTS_ENABLED, "false"));
  }

  private StandaloneProducer configureForTests(JdbcStoredProcedureProducer p, boolean addConnection) {
    if (PROPERTIES.getProperty(JDBC_VENDOR).equals("mysql")) p.setStatementCreator(new MysqlStatementCreator());
    else if (PROPERTIES.getProperty(JDBC_VENDOR).equals("sqlserver")) p.setStatementCreator(new SqlServerStatementCreator());
    else
      fail("Vendor for JDBC tests unknown: " + PROPERTIES.getProperty(JDBC_VENDOR));

    if (addConnection) {
      JdbcConnection jdbcConnection = new JdbcConnection(PROPERTIES.getProperty(JDBC_URL), PROPERTIES.getProperty(JDBC_DRIVER));
      jdbcConnection.setUsername(PROPERTIES.getProperty(JDBC_USER));
      jdbcConnection.setPassword(PROPERTIES.getProperty(JDBC_PASSWORD));
      return new StandaloneProducer(jdbcConnection, p);
    }
    return new StandaloneProducer(p);
  }

  private AdaptrisMessage createMessage() {
    return DefaultMessageFactory.getDefaultInstance().newMessage();
  }

  private AdaptrisMessage createMessage(String txt) {
    return DefaultMessageFactory.getDefaultInstance().newMessage(txt);
  }

  @Test
  public void testOneMetadataParamIn() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();
      spp.setProcedureName(("one_in"));

      JdbcMetadataParameter inParameter = new JdbcMetadataParameter();
      inParameter.setMetadataKey("xType");
      inParameter.setName("xType");
      inParameter.setType(ParameterValueType.VARCHAR);
      AdaptrisMessage message = createMessage();
      message.addMetadata("xType", "Sold");

      InParameters inParameters = new InParameters();
      inParameters.add(inParameter);

      spp.setInParameters(inParameters);

      assertEquals(1, message.getMetadata().size());
      ExampleServiceCase.execute(configureForTests(spp, true), message);
      assertEquals(1, message.getMetadata().size());
    }
  }

  @Test
  public void testOneMetadataParamInOut() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("one_inout"));

      JdbcMetadataParameter inParameter = new JdbcMetadataParameter();
      inParameter.setMetadataKey("xSomeAmount");
      inParameter.setName("xSomeAmount");
      inParameter.setType(ParameterValueType.INTEGER);
      AdaptrisMessage message = createMessage();

      message.addMetadata("xSomeAmount", "100");

      InOutParameters inOutParameters = new InOutParameters();
      inOutParameters.add(inParameter);

      spp.setInOutParameters(inOutParameters);

      assertEquals(1, message.getMetadata().size());
      assertEquals("100", message.getMetadataValue("xSomeAmount"));
      ExampleServiceCase.execute(configureForTests(spp, true), message);
      assertEquals(1, message.getMetadata().size());
      assertEquals("105", message.getMetadataValue("xSomeAmount"));
    }
  }

  @Test
  public void testMultipleExecutions() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("one_in_one_out"));
      AdaptrisMessage message = createMessage(XML_PAYLOAD);
      AdaptrisMessage message2 = createMessage(XML_PAYLOAD);
      JdbcXPathParameter inParameter = new JdbcXPathParameter();
      inParameter.setXpath("/head/body/element1");
      inParameter.setName("xType");
      inParameter.setType(ParameterValueType.VARCHAR);

      InParameters inParameters = new InParameters();
      inParameters.add(inParameter);

      JdbcMetadataParameter outParameter = new JdbcMetadataParameter();
      outParameter.setMetadataKey("result");
      outParameter.setName("transferCount");
      outParameter.setType(ParameterValueType.INTEGER);

      OutParameters outParameters = new OutParameters();
      outParameters.add(outParameter);

      spp.setInParameters(inParameters);
      spp.setOutParameters(outParameters);
      StandaloneProducer producer = configureForTests(spp, true);
      try {
        // INTERLOK-3329 For coverage so the prepare() warning is executed 2x
        LifecycleHelper.prepare(producer);
        start(producer);
        assertEquals(0, message.getMetadata().size());
        producer.doService(message);
        assertEquals(1, message.getMetadata().size());
        assertEquals("15", message.getMetadataValue("result"));

        assertEquals(0, message2.getMetadata().size());
        producer.doService(message2);
        assertEquals(1, message2.getMetadata().size());
        assertEquals("15", message2.getMetadataValue("result"));
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testConnectionInObjectMetadata() throws Exception {
    JdbcServiceList serviceList = new JdbcServiceList();
    try {
      if (areTestsEnabled()) {
        JdbcConnection jdbcConnection = new JdbcConnection(PROPERTIES.getProperty(JDBC_URL), PROPERTIES.getProperty(JDBC_DRIVER));
        jdbcConnection.setUsername(PROPERTIES.getProperty(JDBC_USER));
        jdbcConnection.setPassword(PROPERTIES.getProperty(JDBC_PASSWORD));
        serviceList.setConnection(jdbcConnection);
        JdbcStoredProcedureProducer myStoredProc = new JdbcStoredProcedureProducer();
        if (PROPERTIES.getProperty(JDBC_VENDOR).equals("mysql")) myStoredProc.setStatementCreator(new MysqlStatementCreator());
        else if (PROPERTIES.getProperty(JDBC_VENDOR).equals("sqlserver"))
          myStoredProc.setStatementCreator(new SqlServerStatementCreator());
        myStoredProc.setProcedureName(("one_in_one_out"));

        serviceList.add(new StandaloneProducer(myStoredProc));
        start(serviceList);
        AdaptrisMessage message = createMessage(XML_PAYLOAD);
        AdaptrisMessage message2 = createMessage(XML_PAYLOAD);

        JdbcXPathParameter inParameter = new JdbcXPathParameter();
        inParameter.setXpath("/head/body/element1");
        inParameter.setName("xType");
        inParameter.setType(ParameterValueType.VARCHAR);

        InParameters inParameters = new InParameters();
        inParameters.add(inParameter);

        JdbcMetadataParameter outParameter = new JdbcMetadataParameter();
        outParameter.setMetadataKey("result");
        outParameter.setName("transferCount");
        outParameter.setType(ParameterValueType.INTEGER);

        OutParameters outParameters = new OutParameters();
        outParameters.add(outParameter);

        myStoredProc.setInParameters(inParameters);
        myStoredProc.setOutParameters(outParameters);

        assertEquals(0, message.getMetadata().size());
        serviceList.doService(message);
        assertEquals("15", message.getMetadataValue("result"));

        assertEquals(0, message2.getMetadata().size());
        serviceList.doService(message2);
        assertEquals("15", message2.getMetadataValue("result"));
      }
    }
    finally {
      stop(serviceList);
    }
  }

  @Test
  public void testOneConstantParamInOneMetadataOut() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("one_in_one_out"));

      JdbcConstantParameter inParameter = new JdbcConstantParameter();
      inParameter.setConstant("Sold");
      inParameter.setName("xType");
      inParameter.setType(ParameterValueType.VARCHAR);

      InParameters inParameters = new InParameters();
      inParameters.add(inParameter);

      JdbcMetadataParameter outParameter = new JdbcMetadataParameter();
      outParameter.setMetadataKey("result");
      outParameter.setName("transferCount");
      outParameter.setType(ParameterValueType.INTEGER);

      OutParameters outParameters = new OutParameters();
      outParameters.add(outParameter);

      spp.setInParameters(inParameters);
      spp.setOutParameters(outParameters);
      AdaptrisMessage message = createMessage();

      assertEquals(0, message.getMetadata().size());
      StandaloneProducer producer = configureForTests(spp, true);
      try {
        start(producer);
        producer.doService(message);
        assertEquals(1, message.getMetadata().size());
        assertEquals("15", message.getMetadataValue("result"));
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testOneXpathParamInOneMetadataOut() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("one_in_one_out"));

      AdaptrisMessage message = createMessage(XML_PAYLOAD);

      JdbcXPathParameter inParameter = new JdbcXPathParameter();
      inParameter.setXpath("/head/body/element1");
      inParameter.setName("xType");
      inParameter.setType(ParameterValueType.VARCHAR);

      InParameters inParameters = new InParameters();
      inParameters.add(inParameter);

      JdbcMetadataParameter outParameter = new JdbcMetadataParameter();
      outParameter.setMetadataKey("result");
      outParameter.setName("transferCount");
      outParameter.setType(ParameterValueType.INTEGER);

      OutParameters outParameters = new OutParameters();
      outParameters.add(outParameter);

      spp.setInParameters(inParameters);
      spp.setOutParameters(outParameters);

      assertEquals(0, message.getMetadata().size());
      StandaloneProducer producer = configureForTests(spp, true);
      try {
        start(producer);
        producer.doService(message);
        assertEquals(1, message.getMetadata().size());
        assertEquals("15", message.getMetadataValue("result"));
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testOneConstantParamInOut() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("one_inout"));

      JdbcConstantParameter inParameter = new JdbcConstantParameter();
      inParameter.setName("xSomeAmount");
      inParameter.setType(ParameterValueType.VARCHAR);
      inParameter.setConstant("100");

      InOutParameters inOutParameters = new InOutParameters();
      inOutParameters.add(inParameter);

      spp.setInOutParameters(inOutParameters);
      StandaloneProducer producer = configureForTests(spp, true);
      AdaptrisMessage message = createMessage();

      try {
        start(producer);
        producer.doService(message);
        fail("Cannot use a constant for InOut parameters");
      }
      catch (ServiceException ex) {
        // pass, expected
      }
      finally {
        stop(producer);
      }

    }
  }

  @Test
  public void testOneConstantParamOut() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("one_out"));

      JdbcConstantParameter inParameter = new JdbcConstantParameter();
      inParameter.setName("xSomeAmount");
      inParameter.setType(ParameterValueType.VARCHAR);
      inParameter.setConstant("100");

      OutParameters outParameters = new OutParameters();
      outParameters.add(inParameter);

      spp.setOutParameters(outParameters);

      StandaloneProducer producer = configureForTests(spp, true);
      AdaptrisMessage message = createMessage();

      try {
        start(producer);
        producer.doService(message);
        fail("Cannot use a constant for Out parameters");
      }
      catch (ServiceException ex) {
        // pass, expected
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testOneMetadataParamOutAndOneResultSet() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();
      spp.setProcedureName(("one_resultset_one_out"));
      spp.setResultSetTranslator(new FirstRowMetadataTranslator());

      JdbcMetadataParameter outParameter = new JdbcMetadataParameter();
      outParameter.setMetadataKey("completed");
      outParameter.setName("completed");
      outParameter.setType(ParameterValueType.INTEGER);

      OutParameters outParameters = new OutParameters();
      outParameters.add(outParameter);

      spp.setOutParameters(outParameters);
      AdaptrisMessage message = createMessage();

      assertEquals(0, message.getMetadata().size());
      StandaloneProducer producer = configureForTests(spp, true);
      try {
        start(producer);
        producer.doService(message);
        assertEquals(4, message.getMetadata().size());
        assertEquals("1", message.getMetadataValue("completed"));
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testOneXPathParamInOut() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("one_inout"));

      JdbcXPathParameter inParameter = new JdbcXPathParameter();
      inParameter.setName("xSomeAmount");
      inParameter.setType(ParameterValueType.VARCHAR);
      inParameter.setXpath("100");

      InOutParameters inOutParameters = new InOutParameters();
      inOutParameters.add(inParameter);

      spp.setInOutParameters(inOutParameters);

      StandaloneProducer producer = configureForTests(spp, true);
      AdaptrisMessage message = createMessage();

      try {
        start(producer);
        producer.doService(message);
        fail("Cannot use a constant for InOut parameters");
      }
      catch (ServiceException ex) {
        // pass, expected
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testOneXPathParamOut() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("one_out"));

      JdbcXPathParameter inParameter = new JdbcXPathParameter();
      inParameter.setName("xSomeAmount");
      inParameter.setType(ParameterValueType.VARCHAR);
      inParameter.setXpath("100");

      OutParameters outParameters = new OutParameters();
      outParameters.add(inParameter);

      spp.setOutParameters(outParameters);

      StandaloneProducer producer = configureForTests(spp, true);
      AdaptrisMessage message = createMessage();

      try {
        start(producer);
        producer.doService(message);
        fail("Cannot use a constant for Out parameters");
      }
      catch (ServiceException ex) {
        // pass, expected
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testOneMetadataParamInButDoesntExist() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("one_in"));

      JdbcMetadataParameter inParameter = new JdbcMetadataParameter();
      inParameter.setMetadataKey("xType");
      inParameter.setName("xType");
      inParameter.setType(ParameterValueType.VARCHAR);

      InParameters inParameters = new InParameters();
      inParameters.add(inParameter);

      spp.setInParameters(inParameters);

      StandaloneProducer producer = configureForTests(spp, true);
      AdaptrisMessage message = createMessage();

      try {
        start(producer);
        producer.doService(message);
        fail("Should have thrown ProduceException, because the metadata does not exist in the message.");
      }
      catch (ServiceException ex) {
        // pass, expected
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testOneMetadataParamOut() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();
      spp.setProcedureName(("one_out"));

      JdbcMetadataParameter outParameter = new JdbcMetadataParameter();
      outParameter.setMetadataKey("transferCount");
      outParameter.setName("transferCount");
      outParameter.setType(ParameterValueType.INTEGER);

      OutParameters outParameters = new OutParameters();
      outParameters.add(outParameter);

      spp.setOutParameters(outParameters);
      AdaptrisMessage message = createMessage();

      assertEquals(0, message.getMetadata().size());
      StandaloneProducer producer = configureForTests(spp, true);
      try {
        start(producer);
        producer.doService(message);
        assertEquals(1, message.getMetadata().size());
        assertEquals("31", message.getMetadataValue("transferCount"));
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testManyMetadataParametersOut() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("many_out"));

      JdbcMetadataParameter outParameter1 = new JdbcMetadataParameter();
      outParameter1.setMetadataKey("numberSold");
      outParameter1.setName("numberSold");
      outParameter1.setType(ParameterValueType.INTEGER);
      JdbcMetadataParameter outParameter2 = new JdbcMetadataParameter();
      outParameter2.setMetadataKey("numberPurchased");
      outParameter2.setName("numberPurchased");
      outParameter2.setType(ParameterValueType.INTEGER);
      JdbcMetadataParameter outParameter3 = new JdbcMetadataParameter();
      outParameter3.setMetadataKey("totalTransfers");
      outParameter3.setName("totalTransfers");
      outParameter3.setType(ParameterValueType.INTEGER);

      OutParameters outParameters = new OutParameters();
      outParameters.add(outParameter1);
      outParameters.add(outParameter2);
      outParameters.add(outParameter3);

      spp.setOutParameters(outParameters);
      AdaptrisMessage message = createMessage();

      assertEquals(0, message.getMetadata().size());
      StandaloneProducer producer = configureForTests(spp, true);
      try {
        start(producer);
        producer.doService(message);
        assertEquals(3, message.getMetadata().size());

        assertEquals("15", message.getMetadataValue("numberSold"));
        assertEquals("15", message.getMetadataValue("numberPurchased"));
        assertEquals("31", message.getMetadataValue("totalTransfers"));
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testOneObjectMetadataParamIn() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("one_in"));

      JdbcObjectMetadataParameter inParameter = new JdbcObjectMetadataParameter();
      inParameter.setMetadataKey("xType");
      inParameter.setName("xType");
      inParameter.setType(ParameterValueType.VARCHAR);
      AdaptrisMessage message = createMessage();

      message.addObjectHeader("xType", "Sold");

      InParameters inParameters = new InParameters();
      inParameters.add(inParameter);

      spp.setInParameters(inParameters);

      Map<Object, Object> objectMetadata = message.getObjectHeaders();

      assertEquals(1, objectMetadata.size());
      StandaloneProducer producer = configureForTests(spp, true);
      try {
        start(producer);
        producer.doService(message);

        objectMetadata = message.getObjectHeaders();
        assertEquals(1, objectMetadata.size());
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testOneObjectMetadataParamInOut() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();
      spp.setProcedureName(("one_inout"));

      JdbcObjectMetadataParameter inOutParameter = new JdbcObjectMetadataParameter();
      inOutParameter.setMetadataKey("xSomeAmount");
      inOutParameter.setName("xSomeAmount");
      inOutParameter.setType(ParameterValueType.INTEGER);
      AdaptrisMessage message = createMessage();

      message.addObjectHeader("xSomeAmount", 100);

      InOutParameters inOutParameters = new InOutParameters();
      inOutParameters.add(inOutParameter);

      spp.setInOutParameters(inOutParameters);

      Map<Object, Object> objectMetadata = message.getObjectHeaders();

      assertEquals(1, objectMetadata.size());
      assertTrue("100".equals(objectMetadata.get("xSomeAmount").toString()));
      StandaloneProducer producer = configureForTests(spp, true);
      try {
        start(producer);
        producer.doService(message);
        assertEquals(1, objectMetadata.size());
        assertTrue("105".equals(objectMetadata.get("xSomeAmount").toString()));
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testOneObjectMetadataParamInButDoesntExist() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("one_in"));

      JdbcObjectMetadataParameter inParameter = new JdbcObjectMetadataParameter();
      inParameter.setMetadataKey("xType");
      inParameter.setName("xType");
      inParameter.setType(ParameterValueType.VARCHAR);

      InParameters inParameters = new InParameters();
      inParameters.add(inParameter);

      spp.setInParameters(inParameters);
      AdaptrisMessage message = createMessage();

      StandaloneProducer producer = configureForTests(spp, true);
      try {
        start(producer);
        producer.doService(message);
        fail("Should have thrown ProduceException, because the metadata does not exist in the message.");
      }
      catch (ServiceException ex) {
        // pass, expected
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testOneObjectMetadataParamOut() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("one_out"));

      JdbcObjectMetadataParameter outParameter = new JdbcObjectMetadataParameter();
      outParameter.setMetadataKey("transferCount");
      outParameter.setName("transferCount");
      outParameter.setType(ParameterValueType.INTEGER);

      OutParameters outParameters = new OutParameters();
      outParameters.add(outParameter);

      spp.setOutParameters(outParameters);
      AdaptrisMessage message = createMessage();

      Map<Object, Object> objectMetadata = message.getObjectHeaders();

      assertEquals(0, objectMetadata.size());
      StandaloneProducer producer = configureForTests(spp, true);
      try {
        start(producer);
        producer.doService(message);

        objectMetadata = message.getObjectHeaders();

        assertEquals(1, objectMetadata.size());
        assertTrue("31".equals(objectMetadata.get("transferCount").toString()));
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testManyObjectMetadataParametersOut() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("many_out"));

      JdbcObjectMetadataParameter outParameter1 = new JdbcObjectMetadataParameter();
      outParameter1.setMetadataKey("numberSold");
      outParameter1.setName("numberSold");
      outParameter1.setType(ParameterValueType.INTEGER);

      JdbcObjectMetadataParameter outParameter2 = new JdbcObjectMetadataParameter();
      outParameter2.setMetadataKey("numberPurchased");
      outParameter2.setName("numberPurchased");
      outParameter2.setType(ParameterValueType.INTEGER);

      JdbcObjectMetadataParameter outParameter3 = new JdbcObjectMetadataParameter();
      outParameter3.setMetadataKey("totalTransfers");
      outParameter3.setName("totalTransfers");
      outParameter3.setType(ParameterValueType.INTEGER);

      OutParameters outParameters = new OutParameters();
      outParameters.add(outParameter1);
      outParameters.add(outParameter2);
      outParameters.add(outParameter3);

      spp.setOutParameters(outParameters);
      AdaptrisMessage message = createMessage();

      Map<Object, Object> objectMetadata = message.getObjectHeaders();

      assertEquals(0, objectMetadata.size());
      StandaloneProducer producer = configureForTests(spp, true);
      try {
        start(producer);
        producer.doService(message);

        objectMetadata = message.getObjectHeaders();

        assertEquals(3, objectMetadata.size());

        assertTrue("15".equals(objectMetadata.get("numberSold").toString()));
        assertTrue("15".equals(objectMetadata.get("numberPurchased").toString()));
        assertTrue("31".equals(objectMetadata.get("totalTransfers").toString()));
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testOneMetadataInOneOutOneInOut() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("one_inout_one_in_one_out"));

      JdbcMetadataParameter inParameter = new JdbcMetadataParameter();
      inParameter.setMetadataKey("managersName");
      inParameter.setName("managersName");
      inParameter.setType(ParameterValueType.VARCHAR);

      JdbcMetadataParameter outParameter = new JdbcMetadataParameter();
      outParameter.setMetadataKey("playersName");
      outParameter.setName("playersName");
      outParameter.setType(ParameterValueType.VARCHAR);

      JdbcMetadataParameter inOutParameter = new JdbcMetadataParameter();
      inOutParameter.setMetadataKey("xAmount");
      inOutParameter.setName("xAmount");
      inOutParameter.setType(ParameterValueType.INTEGER);
      AdaptrisMessage message = createMessage();

      message.addMetadata("managersName", "Rafael Benitez");
      message.addMetadata("xAmount", "8000000");

      InParameters inParameters = new InParameters();
      inParameters.add(inParameter);

      OutParameters outParameters = new OutParameters();
      outParameters.add(outParameter);

      InOutParameters inOutParameters = new InOutParameters();
      inOutParameters.add(inOutParameter);

      spp.setInParameters(inParameters);
      spp.setOutParameters(outParameters);
      spp.setInOutParameters(inOutParameters);

      assertEquals(2, message.getMetadata().size());
      StandaloneProducer producer = configureForTests(spp, true);
      try {
        start(producer);
        producer.doService(message);
        assertEquals(3, message.getMetadata().size());

        assertEquals("7000000", message.getMetadataValue("xAmount"));
        assertEquals("Peter Crouch", message.getMetadataValue("playersName"));
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testOneResultSetMetadataTranslator() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("one_resultset"));
      spp.setResultSetTranslator(new AllRowsMetadataTranslator());
      AdaptrisMessage message = createMessage();

      assertEquals(0, message.getMetadata().size());
      StandaloneProducer producer = configureForTests(spp, true);
      try {
        start(producer);
        producer.doService(message);
        assertEquals(15, message.getMetadata().size());
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testMultipleResultSetsMetadataTranslator() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("many_resultsets"));
      spp.setResultSetTranslator(new AllRowsMetadataTranslator());
      AdaptrisMessage message = createMessage();

      assertEquals(0, message.getMetadata().size());
      StandaloneProducer producer = configureForTests(spp, true);
      try {
        start(producer);
        producer.doService(message);
        assertEquals(30, message.getMetadata().size());
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testOneResultSetFirstRowMetadataTranslator() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("one_resultset"));
      spp.setResultSetTranslator(new FirstRowMetadataTranslator());
      AdaptrisMessage message = createMessage();

      assertEquals(0, message.getMetadata().size());
      StandaloneProducer producer = configureForTests(spp, true);
      try {
        start(producer);
        producer.doService(message);
        assertEquals(3, message.getMetadata().size());
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testMultipleResultSetsFirstRowMetadataTranslator() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("many_resultsets"));
      spp.setResultSetTranslator(new FirstRowMetadataTranslator());
      AdaptrisMessage message = createMessage();

      assertEquals(0, message.getMetadata().size());
      StandaloneProducer producer = configureForTests(spp, true);
      try {
        start(producer);
        producer.doService(message);
        assertEquals(6, message.getMetadata().size());
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testOneResultSetXmlTranslator() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("one_resultset"));
      XmlPayloadTranslator xmlPayloadTranslator = new XmlPayloadTranslator();
      xmlPayloadTranslator.setPreserveOriginalMessage(true);
      spp.setResultSetTranslator(xmlPayloadTranslator);
      AdaptrisMessage message = createMessage();

      assertEquals(0, message.getMetadata().size());
      StandaloneProducer producer = configureForTests(spp, true);
      try {
        start(producer);
        producer.doService(message);

        assertEquals("Djibril Cisse", resolveXPath(message, "/Results/Row[1]/player"));
        assertEquals("Robbie Fowler", resolveXPath(message, "/Results/Row[2]/player"));
        assertEquals("Emile Heskey", resolveXPath(message, "/Results/Row[3]/player"));
        assertEquals("Xabi Alonso", resolveXPath(message, "/Results/Row[4]/player"));
        assertEquals("El-Hadji Diouf", resolveXPath(message, "/Results/Row[5]/player"));
        assertNull(resolveXPath(message, "/Results/Row[6]/player"));
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testMultiResultSetXmlTranslator() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("many_resultsets"));
      XmlPayloadTranslator xmlPayloadTranslator = new XmlPayloadTranslator();
      xmlPayloadTranslator.setPreserveOriginalMessage(true);
      spp.setResultSetTranslator(xmlPayloadTranslator);
      AdaptrisMessage message = createMessage();

      assertEquals(0, message.getMetadata().size());
      StandaloneProducer producer = configureForTests(spp, true);
      try {
        start(producer);
        producer.doService(message);

        assertEquals("Robbie Fowler", resolveXPath(message, "/Results/Row[1]/player"));
        assertEquals("Michael Owen", resolveXPath(message, "/Results/Row[2]/player"));
        assertEquals("Stan Collymore", resolveXPath(message, "/Results/Row[3]/player"));
        assertEquals("Emile Heskey", resolveXPath(message, "/Results/Row[5]/player"));
        assertEquals("Djibril Cisse", resolveXPath(message, "/Results/Row[6]/player"));
        assertEquals("Emile Heskey", resolveXPath(message, "/Results/Row[7]/player"));
        assertEquals("Xabi Alonso", resolveXPath(message, "/Results/Row[8]/player"));
        assertEquals("El-Hadji Diouf", resolveXPath(message, "/Results/Row[9]/player"));
        assertEquals("Stan Collymore", resolveXPath(message, "/Results/Row[10]/player"));
        assertNull(resolveXPath(message, "/Results/Row[11]/player"));
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testStringPayloadParamIn() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("one_in"));

      JdbcStringPayloadParameter inParameter = new JdbcStringPayloadParameter();
      inParameter.setName("xType");
      inParameter.setType(ParameterValueType.VARCHAR);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Sold");

      InParameters inParameters = new InParameters();
      inParameters.add(inParameter);

      spp.setInParameters(inParameters);
      StandaloneProducer producer = configureForTests(spp, true);
      try {
        start(producer);
        producer.doService(msg);
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testProduce_PooledConnection() throws Exception {
    int maxServices = 5;
    final int iterations = 5;
    int poolsize = maxServices - 1;
    if (areTestsEnabled()) {
      List<Service> serviceList = new ArrayList<Service>();
      String name = Thread.currentThread().getName();
      Thread.currentThread().setName(getName());
      JdbcPooledConnection conn = PooledConnectionHelper.createPooledConnection(PROPERTIES.getProperty(JDBC_DRIVER),
          PROPERTIES.getProperty(JDBC_URL), poolsize);
      conn.setUsername(PROPERTIES.getProperty(JDBC_USER));
      conn.setPassword(PROPERTIES.getProperty(JDBC_PASSWORD));

      try {
        for (int i = 0; i < maxServices; i++) {
          JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();
          spp.setProcedureName(("one_in"));
          JdbcStringPayloadParameter inParameter = new JdbcStringPayloadParameter();
          inParameter.setName("xType");
          inParameter.setType(ParameterValueType.VARCHAR);
          AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Sold");

          InParameters inParameters = new InParameters();
          inParameters.add(inParameter);

          spp.setInParameters(inParameters);
          StandaloneProducer service = configureForTests(spp, false);
          service.setConnection(conn);
          serviceList.add(service);
          start(service);
        }
        PooledConnectionHelper.executeTest(serviceList, iterations, new PooledConnectionHelper.MessageCreator() {
          @Override
          public AdaptrisMessage createMsgForPooledConnectionTest() throws Exception {
            return createMessage("Sold");
          }
        });
        assertEquals(0, conn.currentBusyConnectionCount());
        assertEquals(poolsize, conn.currentIdleConnectionCount());
        assertEquals(poolsize, conn.currentConnectionCount());
      }
      finally {
        stop(serviceList.toArray(new ComponentLifecycle[0]));
        Thread.currentThread().setName(name);
      }
    }
  }

  @Test
  public void testProduce_AdvancedPooledConnection() throws Exception {
    int maxServices = 5;
    final int iterations = 5;
    int poolsize = maxServices - 1;
    if (areTestsEnabled()) {
      List<Service> serviceList = new ArrayList<Service>();
      String name = Thread.currentThread().getName();
      Thread.currentThread().setName(getName());
      AdvancedJdbcPooledConnection conn = PooledConnectionHelper.createAdvancedPooledConnection(PROPERTIES.getProperty(JDBC_DRIVER),
          PROPERTIES.getProperty(JDBC_URL), poolsize);
      conn.setUsername(PROPERTIES.getProperty(JDBC_USER));
      conn.setPassword(PROPERTIES.getProperty(JDBC_PASSWORD));

      try {
        for (int i = 0; i < maxServices; i++) {
          JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();
          spp.setProcedureName(("one_in"));
          JdbcStringPayloadParameter inParameter = new JdbcStringPayloadParameter();
          inParameter.setName("xType");
          inParameter.setType(ParameterValueType.VARCHAR);
          AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Sold");

          InParameters inParameters = new InParameters();
          inParameters.add(inParameter);

          spp.setInParameters(inParameters);
          StandaloneProducer service = configureForTests(spp, false);
          service.setConnection(conn);
          serviceList.add(service);
          start(service);
        }
        PooledConnectionHelper.executeTest(serviceList, iterations, new PooledConnectionHelper.MessageCreator() {
          @Override
          public AdaptrisMessage createMsgForPooledConnectionTest() throws Exception {
            return createMessage("Sold");
          }
        });
        assertEquals(0, conn.currentBusyConnectionCount());
        assertEquals(poolsize, conn.currentIdleConnectionCount());
        assertEquals(poolsize, conn.currentConnectionCount());
      }
      finally {
        stop(serviceList.toArray(new ComponentLifecycle[0]));
        Thread.currentThread().setName(name);
      }
    }
  }

  @Test
  public void testStringPayloadParamOut() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("one_out"));

      JdbcStringPayloadParameter param = new JdbcStringPayloadParameter();
      param.setName("transferCount");
      param.setType(ParameterValueType.VARCHAR);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Sold");

      OutParameters outParameters = new OutParameters();
      outParameters.add(param);

      spp.setOutParameters(outParameters);

      StandaloneProducer producer = configureForTests(spp, true);
      try {
        start(producer);
        producer.doService(msg);
        fail("Cannot use a constant for Out parameters");
      }
      catch (ServiceException ex) {
        // pass, expected
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testBytePayloadParamIn() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("one_in"));

      JdbcBytePayloadParameter inParameter = new JdbcBytePayloadParameter();
      inParameter.setName("xType");
      inParameter.setType(ParameterValueType.VARCHAR);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Sold");

      InParameters inParameters = new InParameters();
      inParameters.add(inParameter);

      spp.setInParameters(inParameters);
      StandaloneProducer producer = configureForTests(spp, true);
      try {
        start(producer);
        producer.doService(msg);
      }
      finally {
        stop(producer);
      }
    }
  }

  @Test
  public void testBytePayloadParamOut() throws Exception {
    if (areTestsEnabled()) {
      JdbcStoredProcedureProducer spp = new JdbcStoredProcedureProducer();

      spp.setProcedureName(("one_out"));

      JdbcBytePayloadParameter param = new JdbcBytePayloadParameter();
      param.setName("transferCount");
      param.setType(ParameterValueType.VARCHAR);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Sold");

      OutParameters outParameters = new OutParameters();
      outParameters.add(param);

      spp.setOutParameters(outParameters);

      StandaloneProducer producer = configureForTests(spp, true);
      try {
        start(producer);
        producer.doService(msg);
        fail("Cannot use a constant for Out parameters");
      }
      catch (ServiceException ex) {
        // pass, expected
      }
      finally {
        stop(producer);
      }
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {

    JdbcStoredProcedureProducer exampleProducer = new JdbcStoredProcedureProducer();
    exampleProducer.setStatementCreator(new MysqlStatementCreator());
    exampleProducer.setStatementExecutor(new ExecuteQueryCallableStatementExecutor());
    exampleProducer.setProcedureName("The Stored Procedure to call");
    TimeInterval timeout = new TimeInterval(10L, TimeUnit.SECONDS);
    exampleProducer.setTimeout(timeout);

    JdbcConnection jdbcConnection = new JdbcConnection("jdbc:mysql://localhost:3306/mydatabase", "com.mysql.jdbc.Driver");
    jdbcConnection.setUsername("optional_username");
    jdbcConnection.setPassword("optional_password");

    exampleProducer.setResultSetTranslator(new XmlPayloadTranslator());

    JdbcMetadataParameter inMetaParameter = new JdbcMetadataParameter();
    inMetaParameter.setMetadataKey("InParam1MetaKey");
    inMetaParameter.setName("InParam1");
    inMetaParameter.setType(ParameterValueType.VARCHAR);
    inMetaParameter.setOrder(5);

    JdbcObjectMetadataParameter inObjectParameter = new JdbcObjectMetadataParameter();
    inObjectParameter.setMetadataKey("InParam2MetaKey");
    inObjectParameter.setName("InParam2");
    inObjectParameter.setType(ParameterValueType.INTEGER);
    inObjectParameter.setOrder(6);

    JdbcXPathParameter inXPathParameter = new JdbcXPathParameter();
    inXPathParameter.setName("InParam3");
    inXPathParameter.setType(ParameterValueType.VARCHAR);
    inXPathParameter.setXpath("/some/xpath");
    inXPathParameter.setOrder(7);

    JdbcConstantParameter inConstantParameter = new JdbcConstantParameter();
    inConstantParameter.setConstant("InParam4Value");
    inConstantParameter.setName("InParam4");
    inConstantParameter.setType(ParameterValueType.VARCHAR);
    inConstantParameter.setOrder(8);

    JdbcStringPayloadParameter inPayloadParameter = new JdbcStringPayloadParameter();
    inPayloadParameter.setName("InParam4");
    inPayloadParameter.setType(ParameterValueType.CLOB);
    inPayloadParameter.setOrder(9);

    JdbcBytePayloadParameter inBytePayloadParameter = new JdbcBytePayloadParameter();
    inBytePayloadParameter.setName("InParam5");
    inBytePayloadParameter.setType(ParameterValueType.BLOB);
    inBytePayloadParameter.setOrder(10);

    JdbcMetadataParameter outMetaParameter = new JdbcMetadataParameter();
    outMetaParameter.setMetadataKey("OutParam1MetaKey");
    outMetaParameter.setName("OutParam1");
    outMetaParameter.setType(ParameterValueType.VARCHAR);
    outMetaParameter.setOrder(1);

    JdbcObjectMetadataParameter outObjectParameter = new JdbcObjectMetadataParameter();
    outObjectParameter.setMetadataKey("OutParam2MetaKey");
    outObjectParameter.setName("OutParam2");
    outObjectParameter.setType(ParameterValueType.BLOB);
    outObjectParameter.setOrder(2);

    JdbcMetadataParameter inOutMetaParameter = new JdbcMetadataParameter();
    inOutMetaParameter.setMetadataKey("InOutParam1MetaKey");
    inOutMetaParameter.setName("InOutParam1");
    inOutMetaParameter.setType(ParameterValueType.VARCHAR);
    inOutMetaParameter.setOrder(3);

    JdbcObjectMetadataParameter inOutObjectParameter = new JdbcObjectMetadataParameter();
    inOutObjectParameter.setMetadataKey("InOutParam2MetaKey");
    inOutObjectParameter.setName("InOutParam2");
    inOutObjectParameter.setType(ParameterValueType.VARCHAR);
    inOutObjectParameter.setOrder(4);

    InParameters inParams = new InParameters();
    inParams.add(inMetaParameter);
    inParams.add(inObjectParameter);
    inParams.add(inXPathParameter);
    inParams.add(inConstantParameter);
    inParams.add(inPayloadParameter);
    inParams.add(inBytePayloadParameter);

    OutParameters outParams = new OutParameters();
    outParams.add(outMetaParameter);
    outParams.add(outObjectParameter);

    InOutParameters inOutParams = new InOutParameters();
    inOutParams.add(inOutMetaParameter);
    inOutParams.add(inOutObjectParameter);

    exampleProducer.setInParameters(inParams);
    exampleProducer.setOutParameters(outParams);
    exampleProducer.setInOutParameters(inOutParams);

    return new StandaloneProducer(jdbcConnection, exampleProducer);
  }

  private String resolveXPath(AdaptrisMessage message, String xpath) throws XPathExpressionException, IOException {
    XmlUtils xmlUtility = new XmlUtils();
    xmlUtility.setSource(message.getInputStream());
    String textItem = new XPath().selectSingleTextItem(xmlUtility.getCurrentDoc(), xpath);

    if (isEmpty(textItem)) {
      return null;
    }
    else {
      return textItem;
    }
  }
}
