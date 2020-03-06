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

package com.adaptris.core.transform;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.core.MultiPayloadMessageFactory;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.stubs.MessageHelper;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.text.xml.StxTransformerFactory;
import com.adaptris.util.text.xml.XPath;
import com.adaptris.util.text.xml.XmlTransformerFactory;
import com.adaptris.util.text.xml.XsltTransformerFactory;
import net.sf.saxon.serialize.MessageWarner;
import net.sf.saxon.trans.UncheckedXPathException;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * This test uses the data in XmlTransformServiceTest
 */
import static com.adaptris.core.transform.XmlTransformServiceTest.ISSUE2641_DEST_XPATH;
import static com.adaptris.core.transform.XmlTransformServiceTest.ISSUE2641_SRC_XPATH;
import static com.adaptris.core.transform.XmlTransformServiceTest.KEY_ISSUE2641_INPUT;
import static com.adaptris.core.transform.XmlTransformServiceTest.KEY_ISSUE2641_TRANSFORM_URL;
import static com.adaptris.core.transform.XmlTransformServiceTest.KEY_XML_NODE_TRANSFORM_URL;
import static com.adaptris.core.transform.XmlTransformServiceTest.KEY_XML_REMOVE_NAMESPACE_MAPPING;
import static com.adaptris.core.transform.XmlTransformServiceTest.KEY_XML_TEST_FATAL_TRANSFORM_URL;
import static com.adaptris.core.transform.XmlTransformServiceTest.KEY_XML_TEST_INPUT;
import static com.adaptris.core.transform.XmlTransformServiceTest.KEY_XML_TEST_INVALID_TRANSFORM_URL;
import static com.adaptris.core.transform.XmlTransformServiceTest.KEY_XML_TEST_OUTPUT;
import static com.adaptris.core.transform.XmlTransformServiceTest.KEY_XML_TEST_STX_TRANSFORM_URL;
import static com.adaptris.core.transform.XmlTransformServiceTest.KEY_XML_TEST_TRANSFORM_URL;
import static com.adaptris.core.transform.XmlTransformServiceTest.KEY_XML_TEST_TRANSFORM_URL_XSL_MESSAGE;
import static com.adaptris.core.transform.XmlTransformServiceTest.XML_WITH_NAMESPACE;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings("deprecation")
public class NewXmlTransformServiceTest extends TransformServiceExample {

  private static final String URL = "url";
  private static final String PAYLOAD_ID_SOURCE = "source-payload";
  private static final String PAYLOAD_ID_OUTPUT = "output-payload";

  private enum FactoryConfig {
    STX(new StxTransformerFactory()),
    XSLT(new XsltTransformerFactory()) {
      @Override
      NewXmlTransformService configure(NewXmlTransformService s) {
        DocumentBuilderFactoryBuilder dbfb = new DocumentBuilderFactoryBuilder();
        dbfb.getFeatures().add(new KeyValuePair("http://xml.org/sax/features/external-general-entities", "false"));
        ((XsltTransformerFactory) factory).setXmlDocumentFactoryConfig(dbfb);
        s.setXmlTransformerFactory(factory);
        return s;
      }
    };

    XmlTransformerFactory factory;

    FactoryConfig(XmlTransformerFactory fac) {
      this.factory = fac;
    }

    NewXmlTransformService configure(NewXmlTransformService s) {
      s.setXmlTransformerFactory(factory);
      return s;
    }
  }
  private enum ParameterConfig {
    IGNORE(new IgnoreMetadataParameter()), METADATA(new StringMetadataParameter(new String[]
    {
      ".*metadataToInclude.*"
    }, new String[]
    {
      ".*metadataToExclude.*"
    })), COMPOSITE(new XmlTransformParameterBuilder(new StringMetadataParameter(new String[]
    {
      ".*metadataToInclude.*"
    }, new String[]
    {
      ".*metadataToExclude.*"
    }), new ObjectMetadataParameter(".*myObjectMetadataKeys.*"))), OBJECT(new ObjectMetadataParameter(".*myObjectMetadataKeys.*"));
    XmlTransformParameter param;

    ParameterConfig(XmlTransformParameter p) {
      param = p;
    }

    NewXmlTransformService configure(NewXmlTransformService s) {
      s.setTransformParameter(param);
      return s;
    }
  }

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testRemoveNamespaceMapping() throws Exception {
    // This explicit tests some behavioural changes that might have occurred due to migration SaxonHE
    NewXmlTransformService service = createBaseExample();
    service.setOutputMessageEncoding("ISO-8859-1");
    MultiPayloadAdaptrisMessage msg = (MultiPayloadAdaptrisMessage)new MultiPayloadMessageFactory().newMessage(PAYLOAD_ID_SOURCE, XML_WITH_NAMESPACE, "UTF-8");
    execute(service, msg);
    log.debug(msg.getContent());
  }

  @Test
  public void testSetUrl() {
    try {
      NewXmlTransformService service = new NewXmlTransformService();
      service.setUrl("");
      fail();
    }
    catch (IllegalArgumentException expected) {
      // expected
    }
    NewXmlTransformService service = new NewXmlTransformService();
    service.setUrl(URL);
    assertEquals(URL, service.getUrl());
  }

  @Test
  public void testSetPayloadIDSource() {
    try {
      NewXmlTransformService service = new NewXmlTransformService();
      service.setSourcePayloadId("");
      fail();
    }
    catch (IllegalArgumentException expected) {
      // expected
    }
    NewXmlTransformService service = new NewXmlTransformService();
    service.setSourcePayloadId(PAYLOAD_ID_SOURCE);
    assertEquals(PAYLOAD_ID_SOURCE, service.getSourcePayloadId());
  }

  @Test
  public void testSetPayloadIDOutput() {
    try {
      NewXmlTransformService service = new NewXmlTransformService();
      service.setOutputPayloadId("");
      fail();
    }
    catch (IllegalArgumentException expected) {
      // expected
    }
    NewXmlTransformService service = new NewXmlTransformService();
    service.setOutputPayloadId(PAYLOAD_ID_OUTPUT);
    assertEquals(PAYLOAD_ID_OUTPUT, service.getOutputPayloadId());
  }

  @Test
  public void testSetMetadataKey() {
    try {
      NewXmlTransformService service = new NewXmlTransformService();
      service.setMetadataKey("");
      fail();
    }
    catch (IllegalArgumentException expected) {
      // expected
    }
    NewXmlTransformService service = new NewXmlTransformService();
    service.setMetadataKey("key");
    assertEquals("key", service.getMetadataKey());
  }

  @Test
  public void testInitDefault() throws Exception {
    NewXmlTransformService service = new NewXmlTransformService();
    try {
      LifecycleHelper.init(service);
      assertTrue(service.allowOverride()); // set by init
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  @Test
  public void testInitWithUrl() throws Exception {
    NewXmlTransformService service = new NewXmlTransformService();
    try {
      service.setUrl(URL);
      LifecycleHelper.init(service);
      assertFalse(service.allowOverride()); // left as configured
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  @Test
  public void testInitWithMetadataKey() throws Exception {
    NewXmlTransformService service = new NewXmlTransformService();
    try {
      service.setMetadataKey("key");
      LifecycleHelper.init(service);
      assertTrue(service.allowOverride()); // set by init
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  @Test
  public void testObtainUrlWithUrlOnly() throws Exception {
    NewXmlTransformService service = new NewXmlTransformService();
    MultiPayloadAdaptrisMessage msg = (MultiPayloadAdaptrisMessage)new MultiPayloadMessageFactory().newMessage();
    try {
      service.setUrl(URL);
      LifecycleHelper.init(service);
      assertEquals(URL, service.obtainUrlToUse(msg));
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  @Test
  public void testObtainUrlWithNullMetadataValueInMessage() throws Exception {
    NewXmlTransformService service = new NewXmlTransformService();
    MultiPayloadAdaptrisMessage msg = (MultiPayloadAdaptrisMessage)new MultiPayloadMessageFactory().newMessage();
    try {
      service.setUrl(URL);
      service.setMetadataKey("key");
      LifecycleHelper.init(service);
      assertEquals(URL, service.obtainUrlToUse(msg));
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  @Test
  public void testObtainUrlWithEmptyMetadataValueInMessage() throws Exception {
    NewXmlTransformService service = new NewXmlTransformService();
    MultiPayloadAdaptrisMessage msg = (MultiPayloadAdaptrisMessage)new MultiPayloadMessageFactory().newMessage();
    msg.addMetadata("key", "");
    try {
      service.setUrl(URL);
      service.setMetadataKey("key");
      LifecycleHelper.init(service);
      assertEquals(URL, service.obtainUrlToUse(msg));
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  @Test
  public void testObtainUrlWithMetadataValueInMessage() throws Exception {
    MultiPayloadAdaptrisMessage msg = (MultiPayloadAdaptrisMessage)new MultiPayloadMessageFactory().newMessage();
    msg.addMetadata("key", "val");
    NewXmlTransformService service = new NewXmlTransformService();
    try {
      service.setUrl(URL);
      service.setMetadataKey("key");
      LifecycleHelper.init(service);
      // allow override is false
      assertEquals(URL, service.obtainUrlToUse(msg));
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  @Test
  public void testObtainUrlWithMetadataValueInMessageAllowOverride() throws Exception {

    MultiPayloadAdaptrisMessage msg = (MultiPayloadAdaptrisMessage)new MultiPayloadMessageFactory().newMessage();
    NewXmlTransformService service = new NewXmlTransformService();
    msg.addMetadata("key", "val");
    try {
      service.setUrl(URL);
      service.setMetadataKey("key");
      service.setAllowOverride(true);
      LifecycleHelper.init(service);
      // allow override is false
      assertEquals("val", service.obtainUrlToUse(msg));
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  @Test
  public void testObtainUrlWithNoMetadataValueNoUrl() throws Exception {
    MultiPayloadAdaptrisMessage msg = (MultiPayloadAdaptrisMessage)new MultiPayloadMessageFactory().newMessage();
    NewXmlTransformService service = new NewXmlTransformService();
    try {
      service.setMetadataKey("key");
      service.setAllowOverride(true);
      LifecycleHelper.init(service);
      try {
        service.obtainUrlToUse(msg);
        fail();
      }
      catch (ServiceException expected) {
        // expected
      }
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  @Test
  public void testObtainUrlWithNoMetadataValue() throws Exception {
    NewXmlTransformService service = new NewXmlTransformService();
    MultiPayloadAdaptrisMessage msg = (MultiPayloadAdaptrisMessage)new MultiPayloadMessageFactory().newMessage();
    try {
      service.setUrl(URL);
      service.setMetadataKey("key");
      service.setAllowOverride(true);
      LifecycleHelper.init(service);
      assertEquals(URL, service.obtainUrlToUse(msg));
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  @Test
  public void testObtainUrlWithEmptyMetadataValue() throws Exception {
    MultiPayloadAdaptrisMessage msg = (MultiPayloadAdaptrisMessage)new MultiPayloadMessageFactory().newMessage();
    msg.addMetadata("key", "");
    NewXmlTransformService service = new NewXmlTransformService();
    try {
      service.setUrl(URL);
      service.setMetadataKey("key");
      service.setAllowOverride(true);
      LifecycleHelper.init(service);
      assertEquals(URL, service.obtainUrlToUse(msg));
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  protected NewXmlTransformService createBaseExample() {
    NewXmlTransformService service = new NewXmlTransformService();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_TEST_TRANSFORM_URL));
    service.setMetadataKey("optional metadata key against which over-ride URL may be stored, only if allowOverride == true");
    service.setAllowOverride(true);
    service.setSourcePayloadId(PAYLOAD_ID_SOURCE);
    service.setOutputPayloadId(PAYLOAD_ID_OUTPUT);
    return service;
  }

  @Override
  protected String createBaseFileName(Object object) {
    NewXmlTransformService service = (NewXmlTransformService) object;
    return super.createBaseFileName(object) + "-" + service.getXmlTransformerFactory().getClass().getSimpleName() + "-"
        + service.getTransformParameter().getClass().getSimpleName();
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null; // over-rides retrieveServices below instead
  }

  @Override
  protected List<Service> retrieveObjectsForSampleConfig() {
    List<Service> result = new ArrayList<>();

    for (FactoryConfig fac : FactoryConfig.values()) {
      for (ParameterConfig param : ParameterConfig.values()) {
        result.add(param.configure(fac.configure(createBaseExample())));
      }
    }
    return result;
  }

  @Test
  public void testOutputWithCache() throws Exception {
    MultiPayloadAdaptrisMessage m1 = MessageHelper.createMultiPayloadMessage(PAYLOAD_ID_SOURCE, PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    MultiPayloadAdaptrisMessage m2 = MessageHelper.createMultiPayloadMessage(PAYLOAD_ID_SOURCE, PROPERTIES.getProperty(KEY_XML_TEST_INPUT));

    NewXmlTransformService service = createBaseExample();
    try {
      start(service);
      service.doService(m1);
      service.doService(m2);
      assertEquals(PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT), m1.getContent(PAYLOAD_ID_OUTPUT));
      assertEquals(PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT), m2.getContent(PAYLOAD_ID_OUTPUT));
    }
    finally {
      stop(service);
    }
  }

  // INTERLOK-3113

  @Test
  public void testOutputWithCacheResetsParameters() throws Exception {
    MultiPayloadAdaptrisMessage m1 = MessageHelper.createMultiPayloadMessage(PAYLOAD_ID_SOURCE, PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    m1.addMessageHeader("myKey", "myValue");
    MultiPayloadAdaptrisMessage m2 = MessageHelper.createMultiPayloadMessage(PAYLOAD_ID_SOURCE, PROPERTIES.getProperty(KEY_XML_TEST_INPUT));

    NewXmlTransformService service = createBaseExample();
    service.setCacheTransforms(true);
    service.setTransformParameter(new StringMetadataParameter(new String[] {"myKey"}, new String[0]));
    try {
      start(service);
      service.doService(m1);
      assertNotNull(service.getTransforms().get(PROPERTIES.getProperty(KEY_XML_TEST_TRANSFORM_URL)).getParameter("myKey"));
      service.doService(m2);
      assertNull(service.getTransforms().get(PROPERTIES.getProperty(KEY_XML_TEST_TRANSFORM_URL)).getParameter("myKey"));
    }
    finally {
      stop(service);
    }
  }

  @Test
  public void testOutputWithNoCache() throws Exception {
    MultiPayloadAdaptrisMessage m1 = MessageHelper.createMultiPayloadMessage(PAYLOAD_ID_SOURCE, PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    MultiPayloadAdaptrisMessage m2 = MessageHelper.createMultiPayloadMessage(PAYLOAD_ID_SOURCE, PROPERTIES.getProperty(KEY_XML_TEST_INPUT));

    NewXmlTransformService service = createBaseExample();
    service.setCacheTransforms(false);
    try {
      start(service);
      service.doService(m1);
      service.doService(m2);
      assertEquals(PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT), m1.getContent(PAYLOAD_ID_OUTPUT));
      assertEquals(PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT), m2.getContent(PAYLOAD_ID_OUTPUT));
    }
    finally {
      stop(service);
    }
  }

  @Test
  public void testXSLTOutput() throws Exception {
    MultiPayloadAdaptrisMessage m1 = MessageHelper.createMultiPayloadMessage(PAYLOAD_ID_SOURCE, PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    NewXmlTransformService service = createBaseExample();
    execute(service, m1);
    assertEquals(PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT), m1.getContent(PAYLOAD_ID_OUTPUT));
  }

  @Test
  public void testXSLTOutput_NamedXsltTransformFactory() throws Exception {
    MultiPayloadAdaptrisMessage m1 = MessageHelper.createMultiPayloadMessage(PAYLOAD_ID_SOURCE, PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    NewXmlTransformService service = createBaseExample();
    XsltTransformerFactory fac = new XsltTransformerFactory(net.sf.saxon.TransformerFactoryImpl.class.getCanonicalName());
    service.setXmlTransformerFactory(fac);
    execute(service, m1);
    assertEquals(PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT), m1.getContent(PAYLOAD_ID_OUTPUT));
  }

  @Test
  public void testSTXOutput() throws Exception {
    MultiPayloadAdaptrisMessage m1 = MessageHelper.createMultiPayloadMessage(PAYLOAD_ID_SOURCE, PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    NewXmlTransformService service = createBaseExample();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_TEST_STX_TRANSFORM_URL));
    service.setXmlTransformerFactory(new StxTransformerFactory());
    execute(service, m1);
    assertEquals(PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT), m1.getContent(PAYLOAD_ID_OUTPUT));
  }

  @Test
  public void testXSLT_RecoverableError() throws Exception {
    MultiPayloadAdaptrisMessage m1 = MessageHelper.createMultiPayloadMessage(PAYLOAD_ID_SOURCE, PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    String oldName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    NewXmlTransformService service = createBaseExample();
    try {
      execute(service, m1);
      // INTERLOK-1850 - Saxon 9.7 won't report exceptions, so even if we throw, it'll just eat it.
      // fail("Exception expected but none thrown");
    } catch (ServiceException e) {
      assertTrue(e.getCause() instanceof TransformerException);
    }
    finally {
      Thread.currentThread().setName(oldName);
    }
  }

  @Test
  public void testXSLT_RecoverableError_NoFail() throws Exception {
    MultiPayloadAdaptrisMessage m1 = MessageHelper.createMultiPayloadMessage(PAYLOAD_ID_SOURCE, PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    String oldName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    NewXmlTransformService service = createBaseExample();
    XsltTransformerFactory fac = new XsltTransformerFactory();
    fac.setFailOnRecoverableError(false);
    service.setXmlTransformerFactory(fac);
    service.setUrl(PROPERTIES.getProperty(KEY_XML_TEST_INVALID_TRANSFORM_URL));
    try {
      execute(service, m1);
    }
    finally {
      Thread.currentThread().setName(oldName);
    }
  }

  @Test
  public void testXSLT_FatalError() throws Exception {
    MultiPayloadAdaptrisMessage m1 = MessageHelper.createMultiPayloadMessage(PAYLOAD_ID_SOURCE, PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    NewXmlTransformService service = createBaseExample();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_TEST_FATAL_TRANSFORM_URL));
    try {
      execute(service, m1);
      fail();
    } catch (ServiceException expected) {
      assertExceptionCause(expected,  TransformerException.class, UncheckedXPathException.class);
    }
  }

  @Test
  public void testSingleParameter_XSLTOutput() throws Exception {
    MultiPayloadAdaptrisMessage msg = MessageHelper.createMultiPayloadMessage(PAYLOAD_ID_SOURCE, PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    msg.addMetadata("world", "World");
    NewXmlTransformService service = createBaseExample();
    service.setTransformParameter(new StringMetadataParameter());
    execute(service, msg);
    assertEquals(PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT) + "World", msg.getContent(PAYLOAD_ID_OUTPUT));
  }

  @Test
  public void testObjectMetadataParameter_XSLTOutput() throws Exception {
    MultiPayloadAdaptrisMessage msg = MessageHelper.createMultiPayloadMessage(PAYLOAD_ID_SOURCE, PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    msg.addObjectHeader("myDocumentObject", XmlHelper.createDocument("<data>World</data>"));
    msg.addObjectHeader("anotherDocument", XmlHelper.createDocument("<data>GoodBye</data>"));
    NewXmlTransformService service = createBaseExample();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_NODE_TRANSFORM_URL));
    service.setTransformParameter(new ObjectMetadataParameter(".*my.*"));
    execute(service, msg);
    assertEquals(PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT) + "World", msg.getContent(PAYLOAD_ID_OUTPUT));
  }

  @Test
  public void testObjectMetadataParameter_NoRegexp() throws Exception {
    MultiPayloadAdaptrisMessage msg = MessageHelper.createMultiPayloadMessage(PAYLOAD_ID_SOURCE, PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    msg.addObjectHeader("myDocumentObject", XmlHelper.createDocument("<data>World</data>"));
    msg.addObjectHeader("anotherDocument", XmlHelper.createDocument("<data>GoodBye</data>"));
    NewXmlTransformService service = createBaseExample();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_NODE_TRANSFORM_URL));
    service.setTransformParameter(new ObjectMetadataParameter());
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {
      // expected
    }
  }

  @Test
  public void testParameterBuilder_XSLTOutput() throws Exception {
    MultiPayloadAdaptrisMessage msg = MessageHelper.createMultiPayloadMessage(PAYLOAD_ID_SOURCE, PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    msg.addMetadata("world", "World");
    NewXmlTransformService service = createBaseExample();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_TEST_TRANSFORM_URL));
    service.setTransformParameter(new XmlTransformParameterBuilder(new IgnoreMetadataParameter(), new StringMetadataParameter()));
    execute(service, msg);
    assertEquals(PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT) + "World", msg.getContent(PAYLOAD_ID_OUTPUT));
  }

  @Test
  public void testParameterBuilder_ObjectMetadata_XSLTOutput() throws Exception {
    MultiPayloadAdaptrisMessage msg = MessageHelper.createMultiPayloadMessage(PAYLOAD_ID_SOURCE, PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    msg.addMetadata("key", "value");
    msg.addObjectHeader("myDocumentObject", XmlHelper.createDocument("<data>World</data>"));
    NewXmlTransformService service = createBaseExample();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_NODE_TRANSFORM_URL));
    service.setTransformParameter(new XmlTransformParameterBuilder(new IgnoreMetadataParameter(), new StringMetadataParameter(), new ObjectMetadataParameter(".*")));
    execute(service, msg);
    assertEquals(PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT) + "World", msg.getContent(PAYLOAD_ID_OUTPUT));
  }

  @Test
  public void testSingleParameter_STXOutput() throws Exception {
    MultiPayloadAdaptrisMessage msg = MessageHelper.createMultiPayloadMessage(PAYLOAD_ID_SOURCE, PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    msg.addMetadata("world", "World");
    NewXmlTransformService service = createBaseExample();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_TEST_STX_TRANSFORM_URL));
    service.setXmlTransformerFactory(new StxTransformerFactory());
    service.setTransformParameter(new StringMetadataParameter());
    execute(service, msg);
    assertEquals(PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT) + "World", msg.getContent(PAYLOAD_ID_OUTPUT));
  }

  @Test
  public void testMultipleParameters_XSLTOutput() throws Exception {
    MultiPayloadAdaptrisMessage msg = MessageHelper.createMultiPayloadMessage(PAYLOAD_ID_SOURCE, PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    msg.addMetadata("world", "World");
    msg.addMetadata("one", "World");
    msg.addMetadata("two", "World");
    msg.addMetadata("three", "World");
    msg.addMetadata("four", "World");
    NewXmlTransformService service = createBaseExample();
    service.setTransformParameter(new StringMetadataParameter());
    execute(service, msg);
    assertEquals(PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT) + "World", msg.getContent(PAYLOAD_ID_OUTPUT));
  }

  @Test
  public void testMultipleParameters_STXOutput() throws Exception {
    MultiPayloadAdaptrisMessage msg = MessageHelper.createMultiPayloadMessage(PAYLOAD_ID_SOURCE, PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    msg.addMetadata("world", "World");
    msg.addMetadata("one", "World");
    msg.addMetadata("two", "World");
    msg.addMetadata("three", "World");
    msg.addMetadata("four", "World");
    NewXmlTransformService service = createBaseExample();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_TEST_STX_TRANSFORM_URL));
    service.setXmlTransformerFactory(new StxTransformerFactory());
    service.setTransformParameter(new StringMetadataParameter());
    execute(service, msg);
    assertEquals(PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT) + "World", msg.getContent(PAYLOAD_ID_OUTPUT));
  }

  @Test
  public void testIssue2641() throws Exception {
    MultiPayloadAdaptrisMessage msg = MessageHelper.createMultiPayloadMessage(PAYLOAD_ID_SOURCE, PROPERTIES.getProperty(KEY_ISSUE2641_INPUT), "ISO-8859-1");
    Document srcXml = createDocument(msg.getPayload(PAYLOAD_ID_SOURCE));
    XPath srcXpath = new XPath();
    String srcValue = srcXpath.selectSingleTextItem(srcXml, ISSUE2641_SRC_XPATH);
    assertEquals("ISO-8859-1", msg.getContentEncoding());

    NewXmlTransformService service = createBaseExample();
    service.setUrl(PROPERTIES.getProperty(KEY_ISSUE2641_TRANSFORM_URL));
    service.setOutputMessageEncoding("UTF-8");
    execute(service, msg);

    assertEquals("UTF-8", msg.getContentEncoding());
    Document destXml = createDocument(msg.getPayload(PAYLOAD_ID_OUTPUT));

    XPath destXpath = new XPath();
    String destValue = destXpath.selectSingleTextItem(destXml, ISSUE2641_DEST_XPATH);
    log.debug("testIssue2641:: srcValue  = [" + srcValue + "]");
    log.debug("testIssue2641:: destValue = [" + destValue + "]");

    // All things being equal, they should be the same.
    assertEquals(srcValue, destValue);
  }

  @Test
  public void testIssue2641_NoOutputMessageEncoding() throws Exception {
    MultiPayloadAdaptrisMessage msg = MessageHelper.createMultiPayloadMessage(PAYLOAD_ID_SOURCE, PROPERTIES.getProperty(KEY_ISSUE2641_INPUT), "ISO-8859-1");
    Document srcXml = createDocument(msg.getPayload(PAYLOAD_ID_SOURCE));
    XPath srcXpath = new XPath();
    String srcValue = srcXpath.selectSingleTextItem(srcXml, ISSUE2641_SRC_XPATH);
    assertEquals("ISO-8859-1", msg.getContentEncoding());

    NewXmlTransformService service = createBaseExample();
    service.setUrl(PROPERTIES.getProperty(KEY_ISSUE2641_TRANSFORM_URL));
    execute(service, msg);

    assertEquals("ISO-8859-1", msg.getContentEncoding());
    // We're using UTF-8 as the encoding; get bytes will give us the right thing.
    Document destXml = createDocument(msg.getPayload(PAYLOAD_ID_OUTPUT));
    XPath destXpath = new XPath();
    String destValue = destXpath.selectSingleTextItem(destXml, ISSUE2641_DEST_XPATH);
    log.debug("testIssue2641:: srcValue  = [" + srcValue + "]");
    log.debug("testIssue2641:: destValue = [" + destValue + "]");

    // All things being equal, they should be the same.
    assertEquals(srcValue, destValue);
  }

  @Test
  public void testXSLT_XslMessageTerminate() throws Exception {
    MultiPayloadAdaptrisMessage m1 = MessageHelper.createMultiPayloadMessage(PAYLOAD_ID_SOURCE, PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    NewXmlTransformService service = createBaseExample();
    XsltTransformerFactory fac = new XsltTransformerFactory();
    fac.getTransformerFactoryAttributes().add(new KeyValuePair("http://saxon.sf.net/feature/messageEmitterClass", MessageWarner.class.getCanonicalName()));
    fac.getTransformerFactoryFeatures().add(new KeyValuePair(XMLConstants.FEATURE_SECURE_PROCESSING, "true"));
    service.setXmlTransformerFactory(fac);
    service.setUrl(PROPERTIES.getProperty(KEY_XML_TEST_TRANSFORM_URL_XSL_MESSAGE));
    try {
      execute(service, m1);
      fail();
    } catch (ServiceException expected) {
      assertExceptionCause(expected, TransformerException.class, UncheckedXPathException.class);
    }
  }

  @Test
  public void testOutputWrongMessageType() throws Exception {
    AdaptrisMessage m1 = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    NewXmlTransformService service = new NewXmlTransformService();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_TEST_TRANSFORM_URL));
    service.setCacheTransforms(false);
    try {
      start(service);
      service.doService(m1);
      assertEquals(PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT), m1.getContent());
    } finally {
      stop(service);
    }
  }

  private static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
    return DocumentBuilderFactory.newInstance().newDocumentBuilder();
  }

  private Document createDocument(byte[] bytes) throws Exception {
    return newDocumentBuilder().parse(new InputSource(new ByteArrayInputStream(bytes)));
  }

  // INTERLOK-3101, Saxon-9.9.1-4 onwards throws a different Exception.
  private void assertExceptionCause(Exception e, Class...classes) {
    assertNotNull(e.getCause());
    List<Class> validClasses = Arrays.asList(classes);
    Throwable t = e.getCause();
    boolean matches  = validClasses.stream().anyMatch((clazz) -> t.getClass().isAssignableFrom(clazz));
    assertTrue(matches);
  }
}
