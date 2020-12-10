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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
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

@SuppressWarnings("deprecation")
public class XmlTransformServiceTest
    extends com.adaptris.interlok.junit.scaffolding.services.TransformServiceExample {

  static final String KEY_XML_NODE_TRANSFORM_URL = "XmlTransformService.outputNodeTransform";

  public static final String KEY_XML_TEST_INPUT = "XmlTransformService.outputTestMessage";
  public static final String KEY_XML_TEST_TRANSFORM_URL = "XmlTransformService.outputTestTransform";
  public static final String KEY_XML_TEST_TRANSFORM_URL_XSL_MESSAGE = "XmlTransformService.outputTestTransformWithXslMessage";
  public static final String KEY_XML_TEST_INVALID_TRANSFORM_URL = "XmlTransformService.outputTestInvalidTransform";
  public static final String KEY_XML_TEST_FATAL_TRANSFORM_URL = "XmlTransformService.outputTestFatalTransform";
  public static final String KEY_XML_TEST_STX_TRANSFORM_URL = "XmlTransformService.outputTestStxTransform";
  public static final String KEY_XML_TEST_OUTPUT = "XmlTransformService.outputTestResult";

  static final String KEY_ISSUE2641_INPUT = "XmlTransformService.issue2641.input";
  static final String KEY_ISSUE2641_TRANSFORM_URL = "XmlTransformService.issue2641.transform";

  static final String ISSUE2641_DEST_XPATH = "/Envelope/QualityDocument/DocumentLine[6]/Criteria/CriteriaID";
  static final String ISSUE2641_SRC_XPATH = "/root/segment_Header/record_/critere6";

  static final String KEY_XML_REMOVE_NAMESPACE_MAPPING = "XmlTransformService.remove.namespace.mapping";

  static final String XML_WITH_NAMESPACE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
      + "<svrl:schematron-output xmlns:svrl=\"http://purl.oclc.org/dsdl/svrl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:sch=\"http://www.ascc.net/xml/schematron\" xmlns:iso=\"http://purl.oclc.org/dsdl/schematron\" xmlns:dp=\"http://www.dpawson.co.uk/ns#\" title=\"Anglia Farmers AF xml Invoice Schematron File\" schemaVersion=\"ISO19757-3\">\n"
      + "<svrl:ns-prefix-in-attribute-values uri=\"http://www.dpawson.co.uk/ns#\" prefix=\"dp\"/>\n" + "<svrl:active-pattern/>\n"
      + "<svrl:fired-rule context=\"SageData/JoinedData\"/>\n"
      + "<svrl:failed-assert test=\"STOCK_CODE != ''\" location=\"/SageData[1]/JoinedData[1]\">\n"
      + "<svrl:text>Error: Product Code must be present.</svrl:text>\n" + "</svrl:failed-assert>\n"
      + "<svrl:fired-rule context=\"SageData/JoinedData/CUST_ORDER_NUMBER[. != '']\"/>\n"
      + "<svrl:fired-rule context=\"SageData/JoinedData/AF_NUMBER[. != '']\"/>\n"
      + "<svrl:failed-assert test=\"string-length(.) = 5\" location=\"/SageData[1]/JoinedData[1]/AF_NUMBER[1]\">\n"
      + "<svrl:text>Error: Anglia Farmer's Supplier Number must be 5 digits long. (Current Value: 62826123)</svrl:text>\n"
      + "</svrl:failed-assert>\n" + "</svrl:schematron-output>";

  private enum FactoryConfig {
    STX(new StxTransformerFactory()),
    XSLT(new XsltTransformerFactory()) {
      @Override
      XmlTransformService configure(XmlTransformService s) {
        DocumentBuilderFactoryBuilder dbfb = new DocumentBuilderFactoryBuilder();
        dbfb.getFeatures().add(new KeyValuePair("http://xml.org/sax/features/external-general-entities", "false"));
        ((XsltTransformerFactory) factory).setXmlDocumentFactoryConfig(dbfb);
        s.setXmlTransformerFactory(factory);
        return s;
      }
    };

    XmlTransformerFactory factory;

    FactoryConfig(XmlTransformerFactory fac) {
      factory = fac;
    }

    XmlTransformService configure(XmlTransformService s) {
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

    XmlTransformService configure(XmlTransformService s) {
      s.setTransformParameter(param);
      return s;
    }
  }


  @Test
  public void testRemoveNamespaceMapping() throws Exception {
    // This explicit tests some behavioural changes that might have occured due to migration SaxonHE
    XmlTransformService service = new XmlTransformService();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_REMOVE_NAMESPACE_MAPPING));
    service.setOutputMessageEncoding("ISO-8859-1");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_WITH_NAMESPACE, "UTF-8");
    execute(service, msg);
    log.debug(msg.getContent());
  }

  @Test
  public void testSetUrl() {
    try {
      XmlTransformService service = new XmlTransformService();
      service.setUrl("");
      fail();
    }
    catch (IllegalArgumentException expected) {
    }
    XmlTransformService service = new XmlTransformService();
    service.setUrl("url");
    assertEquals("url", service.getUrl());
  }

  @Test
  public void testSetMetadataKey() {
    try {
      XmlTransformService service = new XmlTransformService();
      service.setMetadataKey("");
      fail();
    }
    catch (IllegalArgumentException expected) {
    }
    XmlTransformService service = new XmlTransformService();
    service.setMetadataKey("key");
    assertEquals("key", service.getMetadataKey());
  }

  @Test
  public void testInitDefault() {
    XmlTransformService service = new XmlTransformService();
    try {
      LifecycleHelper.init(service);
      assertTrue(service.allowOverride()); // set by init
    }
    catch (CoreException e) {
      fail(); // there is a now default metadata key
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  @Test
  public void testInitWithUrl() {
    XmlTransformService service = new XmlTransformService();
    try {
      service.setUrl("url");
      LifecycleHelper.init(service);
      assertTrue(!service.allowOverride()); // left as configured
    }
    catch (CoreException e) {
      fail();
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  @Test
  public void testInitWithMetadataKey() {
    XmlTransformService service = new XmlTransformService();
    try {
      service.setMetadataKey("key");
      LifecycleHelper.init(service);
      assertTrue(service.allowOverride()); // set by init
    }
    catch (CoreException e) {
      fail();
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  @Test
  public void testObtainUrlWithUrlOnly() throws Exception {
    XmlTransformService service = new XmlTransformService();
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    try {
      service.setUrl("url");
      LifecycleHelper.init(service);

      assertTrue(service.obtainUrlToUse(msg).equals("url"));
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  @Test
  public void testObtainUrlWithNullMetadataValueInMessage() throws Exception {
    XmlTransformService service = new XmlTransformService();
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    try {
      service.setUrl("url");
      service.setMetadataKey("key");
      LifecycleHelper.init(service);

      assertTrue(service.obtainUrlToUse(msg).equals("url"));
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  @Test
  public void testObtainUrlWithEmptyMetadataValueInMessage() throws Exception {
    XmlTransformService service = new XmlTransformService();
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    msg.addMetadata("key", "");
    try {
      service.setUrl("url");
      service.setMetadataKey("key");
      LifecycleHelper.init(service);

      assertTrue(service.obtainUrlToUse(msg).equals("url"));
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  @Test
  public void testObtainUrlWithMetadataValueInMessage() throws Exception {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    msg.addMetadata("key", "val");
    XmlTransformService service = new XmlTransformService();
    try {
      service.setUrl("url");
      service.setMetadataKey("key");
      LifecycleHelper.init(service);

      // allow override is false
      assertTrue(service.obtainUrlToUse(msg).equals("url"));
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  @Test
  public void testObtainUrlWithMetadataValueInMessageAllowOverride() throws Exception {

    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    XmlTransformService service = new XmlTransformService();
    msg.addMetadata("key", "val");
    try {
      service.setUrl("url");
      service.setMetadataKey("key");
      service.setAllowOverride(true);
      LifecycleHelper.init(service);

      // allow override is false
      assertTrue(service.obtainUrlToUse(msg).equals("val"));
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  @Test
  public void testObtainUrlWithNoMetadataValueNoUrl() throws Exception {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    XmlTransformService service = new XmlTransformService();
    try {
      service.setMetadataKey("key");
      service.setAllowOverride(true);
      LifecycleHelper.init(service);
      try {
        service.obtainUrlToUse(msg);
      }
      catch (ServiceException expected) {
      }
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  @Test
  public void testObtainUrlWithNoMetadataValue() throws Exception {
    XmlTransformService service = new XmlTransformService();
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    try {
      service.setUrl("url");
      service.setMetadataKey("key");
      service.setAllowOverride(true);
      LifecycleHelper.init(service);
      assertTrue(service.obtainUrlToUse(msg).equals("url"));
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  @Test
  public void testObtainUrlWithEmptyMetadataValue() throws Exception {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    msg.addMetadata("key", "");
    XmlTransformService service = new XmlTransformService();
    try {
      service.setUrl("url");
      service.setMetadataKey("key");
      service.setAllowOverride(true);
      LifecycleHelper.init(service);

      assertTrue(service.obtainUrlToUse(msg).equals("url"));
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  protected XmlTransformService createBaseExample() {
    XmlTransformService service = new XmlTransformService();
    service.setUrl("URL of transform to apply");
    service.setMetadataKey("optional metadata key against which over-ride URL may be stored, only if allowOverride == true");
    service.setAllowOverride(true);
    return service;
  }

  @Override
  protected String createBaseFileName(Object object) {
    XmlTransformService service = (XmlTransformService) object;
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
    AdaptrisMessage m1 = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    AdaptrisMessage m2 = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));

    XmlTransformService service = new XmlTransformService();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_TEST_TRANSFORM_URL));
    try {
      start(service);
      service.doService(m1);
      service.doService(m2);
      assertEquals("payload " + m1.getContent(), PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT), m1.getContent());
      assertEquals("payload " + m2.getContent(), PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT), m2.getContent());
    }
    finally {
      stop(service);
    }
  }

  // INTERLOK-3113

  @Test
  public void testOutputWithCacheResetsParameters() throws Exception {
    AdaptrisMessage m1 = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    m1.addMessageHeader("myKey", "myValue");
    AdaptrisMessage m2 = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));

    XmlTransformService service = new XmlTransformService();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_TEST_TRANSFORM_URL));
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
    AdaptrisMessage m1 = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    AdaptrisMessage m2 = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));

    XmlTransformService service = new XmlTransformService();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_TEST_TRANSFORM_URL));
    service.setCacheTransforms(false);
    try {
      start(service);
      service.doService(m1);
      service.doService(m2);
      assertEquals("payload " + m1.getContent(), PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT), m1.getContent());
      assertEquals("payload " + m2.getContent(), PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT), m2.getContent());
    }
    finally {
      stop(service);
    }
  }

  @Test
  public void testXSLTOutput() throws Exception {
    AdaptrisMessage m1 = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    XmlTransformService service = new XmlTransformService();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_TEST_TRANSFORM_URL));
    execute(service, m1);
    assertEquals("payload " + m1.getContent(), PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT), m1.getContent());
  }

  @Test
  public void testXSLTOutput_NamedXsltTransformFactory() throws Exception {
    AdaptrisMessage m1 = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    XmlTransformService service = new XmlTransformService();
    XsltTransformerFactory fac = new XsltTransformerFactory(net.sf.saxon.TransformerFactoryImpl.class.getCanonicalName());
    service.setXmlTransformerFactory(fac);
    service.setUrl(PROPERTIES.getProperty(KEY_XML_TEST_TRANSFORM_URL));
    execute(service, m1);
    assertEquals("payload " + m1.getContent(), PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT), m1.getContent());
  }

  @Test
  public void testSTXOutput() throws Exception {
    AdaptrisMessage m1 = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    XmlTransformService service = new XmlTransformService();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_TEST_STX_TRANSFORM_URL));

    service.setXmlTransformerFactory(new StxTransformerFactory());

    execute(service, m1);
    assertEquals("payload " + m1.getContent(), PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT), m1.getContent());
  }

  @Test
  public void testXSLT_RecoverableError() throws Exception {
    AdaptrisMessage m1 = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    String oldName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    XmlTransformService service = new XmlTransformService();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_TEST_INVALID_TRANSFORM_URL));
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
    AdaptrisMessage m1 = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    String oldName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    XmlTransformService service = new XmlTransformService();
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
    AdaptrisMessage m1 = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    XmlTransformService service = new XmlTransformService();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_TEST_FATAL_TRANSFORM_URL));
    try {
      execute(service, m1);
      fail("Exception expected but none thrown");
    } catch (ServiceException expected) {
      assertExceptionCause(expected, TransformerException.class, UncheckedXPathException.class);
    }
  }

  @Test
  public void testSingleParameter_XSLTOutput() throws Exception {
    AdaptrisMessage msg = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    msg.addMetadata("world", "World");
    XmlTransformService service = new XmlTransformService();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_TEST_TRANSFORM_URL));
    service.setTransformParameter(new StringMetadataParameter());
    execute(service, msg);
    assertTrue("payload " + msg.getContent(),
        msg.getContent().equals(PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT) + "World"));

  }

  @Test
  public void testObjectMetadataParameter_XSLTOutput() throws Exception {
    AdaptrisMessage msg = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    msg.addObjectHeader("myDocumentObject", XmlHelper.createDocument("<data>World</data>"));
    msg.addObjectHeader("anotherDocument", XmlHelper.createDocument("<data>GoodBye</data>"));
    XmlTransformService service = new XmlTransformService();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_NODE_TRANSFORM_URL));
    service.setTransformParameter(new ObjectMetadataParameter(".*my.*"));
    execute(service, msg);
    assertTrue("payload " + msg.getContent(),
        msg.getContent().equals(PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT) + "World"));

  }

  @Test
  public void testObjectMetadataParameter_NoRegexp() throws Exception {
    AdaptrisMessage msg = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    msg.addObjectHeader("myDocumentObject", XmlHelper.createDocument("<data>World</data>"));
    msg.addObjectHeader("anotherDocument", XmlHelper.createDocument("<data>GoodBye</data>"));
    XmlTransformService service = new XmlTransformService();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_NODE_TRANSFORM_URL));
    service.setTransformParameter(new ObjectMetadataParameter());
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  @Test
  public void testParameterBuilder_XSLTOutput() throws Exception {
    AdaptrisMessage msg = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    msg.addMetadata("world", "World");
    XmlTransformService service = new XmlTransformService();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_TEST_TRANSFORM_URL));
    service.setTransformParameter(new XmlTransformParameterBuilder(new IgnoreMetadataParameter(), new StringMetadataParameter()));
    execute(service, msg);

    assertTrue("payload " + msg.getContent(),
        msg.getContent().equals(PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT) + "World"));
  }

  @Test
  public void testParameterBuilder_ObjectMetadata_XSLTOutput() throws Exception {
    AdaptrisMessage msg = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    msg.addMetadata("key", "value");
    msg.addObjectHeader("myDocumentObject", XmlHelper.createDocument("<data>World</data>"));
    XmlTransformService service = new XmlTransformService();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_NODE_TRANSFORM_URL));
    service.setTransformParameter(new XmlTransformParameterBuilder(new IgnoreMetadataParameter(), new StringMetadataParameter(),
        new ObjectMetadataParameter(".*")));
    execute(service, msg);
    assertTrue("payload " + msg.getContent(),
        msg.getContent().equals(PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT) + "World"));
  }

  @Test
  public void testSingleParameter_STXOutput() throws Exception {
    AdaptrisMessage msg = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    msg.addMetadata("world", "World");
    XmlTransformService service = new XmlTransformService();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_TEST_STX_TRANSFORM_URL));
    service.setXmlTransformerFactory(new StxTransformerFactory());
    service.setTransformParameter(new StringMetadataParameter());
    execute(service, msg);
    assertTrue("payload " + msg.getContent(),
        msg.getContent().equals(PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT) + "World"));
  }

  @Test
  public void testMultipleParameters_XSLTOutput() throws Exception {
    AdaptrisMessage msg = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    msg.addMetadata("world", "World");
    msg.addMetadata("one", "World");
    msg.addMetadata("two", "World");
    msg.addMetadata("three", "World");
    msg.addMetadata("four", "World");
    XmlTransformService service = new XmlTransformService();

    service.setUrl(PROPERTIES.getProperty(KEY_XML_TEST_TRANSFORM_URL));
    service.setTransformParameter(new StringMetadataParameter());
    execute(service, msg);
    assertTrue("payload " + msg.getContent(),
        msg.getContent().equals(PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT) + "World"));
  }

  @Test
  public void testMultipleParameters_STXOutput() throws Exception {
    AdaptrisMessage msg = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    msg.addMetadata("world", "World");
    msg.addMetadata("one", "World");
    msg.addMetadata("two", "World");
    msg.addMetadata("three", "World");
    msg.addMetadata("four", "World");
    XmlTransformService service = new XmlTransformService();
    service.setUrl(PROPERTIES.getProperty(KEY_XML_TEST_STX_TRANSFORM_URL));
    service.setXmlTransformerFactory(new StxTransformerFactory());
    service.setTransformParameter(new StringMetadataParameter());
    execute(service, msg);
    assertTrue("payload " + msg.getContent(),
        msg.getContent().equals(PROPERTIES.getProperty(KEY_XML_TEST_OUTPUT) + "World"));
  }

  @Test
  public void testIssue2641() throws Exception {
    DefaultMessageFactory factory = new DefaultMessageFactory();
    factory.setDefaultCharEncoding("ISO-8859-1");
    AdaptrisMessage msg =
        MessageHelper.createMessage(factory, PROPERTIES.getProperty(KEY_ISSUE2641_INPUT));
    Document srcXml = createDocument(msg.getPayload());
    XPath srcXpath = new XPath();
    String srcValue = srcXpath.selectSingleTextItem(srcXml, ISSUE2641_SRC_XPATH);
    assertEquals("ISO-8859-1", msg.getContentEncoding());

    XmlTransformService service = new XmlTransformService();
    service.setUrl(PROPERTIES.getProperty(KEY_ISSUE2641_TRANSFORM_URL));
    service.setOutputMessageEncoding("UTF-8");
    execute(service, msg);

    assertEquals("UTF-8", msg.getContentEncoding());
    Document destXml = createDocument(msg.getPayload());

    XPath destXpath = new XPath();
    String destValue = destXpath.selectSingleTextItem(destXml, ISSUE2641_DEST_XPATH);
    log.debug("testIssue2641:: srcValue  = [" + srcValue + "]");
    log.debug("testIssue2641:: destValue = [" + destValue + "]");

    // All things being equal, they should be the same.
    assertEquals(srcValue, destValue);

  }

  @Test
  public void testIssue2641_NoOutputMessageEncoding() throws Exception {
    DefaultMessageFactory factory = new DefaultMessageFactory();
    factory.setDefaultCharEncoding("ISO-8859-1");
    AdaptrisMessage msg =
        MessageHelper.createMessage(factory, PROPERTIES.getProperty(KEY_ISSUE2641_INPUT));
    Document srcXml = createDocument(msg.getPayload());
    XPath srcXpath = new XPath();
    String srcValue = srcXpath.selectSingleTextItem(srcXml, ISSUE2641_SRC_XPATH);
    assertEquals("ISO-8859-1", msg.getContentEncoding());

    XmlTransformService service = new XmlTransformService();
    service.setUrl(PROPERTIES.getProperty(KEY_ISSUE2641_TRANSFORM_URL));
    execute(service, msg);

    assertEquals("ISO-8859-1", msg.getContentEncoding());
    // We're using UTF-8 as the encoding; get bytes will give us the right thing.
    Document destXml = createDocument(msg.getPayload());
    XPath destXpath = new XPath();
    String destValue = destXpath.selectSingleTextItem(destXml, ISSUE2641_DEST_XPATH);
    log.debug("testIssue2641:: srcValue  = [" + srcValue + "]");
    log.debug("testIssue2641:: destValue = [" + destValue + "]");

    // All things being equal, they should be the same.
    assertEquals(srcValue, destValue);
  }

  @Test
  public void testXSLT_XslMessageTerminate() throws Exception {
    AdaptrisMessage m1 = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    XmlTransformService service = new XmlTransformService();
    XsltTransformerFactory fac = new XsltTransformerFactory();
    fac.getTransformerFactoryAttributes()
        .add(new KeyValuePair("http://saxon.sf.net/feature/messageEmitterClass", MessageWarner.class.getCanonicalName()));
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


  private static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = domFactory.newDocumentBuilder();
    return builder;
  }

  private Document createDocument(byte[] bytes) throws Exception {
    ByteArrayInputStream in = new ByteArrayInputStream(bytes);
    return newDocumentBuilder().parse(new InputSource(in));
  }

  // INTERLOK-3101, Saxon-9.9.1-4 onwards throws a different Exception.
  private void assertExceptionCause(Exception e, Class...classes) {
    assertNotNull(e.getCause());
    List<Class> validClasses = Arrays.asList(classes);
    Throwable t = e.getCause();
    boolean matches =
        validClasses.stream().anyMatch((clazz) -> clazz.isAssignableFrom(t.getClass()));
    assertTrue("Exception cause is expected", matches);
  }

}
