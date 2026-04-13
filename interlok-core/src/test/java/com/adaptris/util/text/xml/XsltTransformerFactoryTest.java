/*
 * Copyright 2017 Adaptris Ltd.
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

package com.adaptris.util.text.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import com.adaptris.core.util.DocumentBuilderFactoryBuilder;

import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.lib.Initializer;

class XsltTransformerFactoryTest {

  private static final String SAXON_FACTORY_IMPL = "net.sf.saxon.TransformerFactoryImpl";

  @BeforeEach
  void resetState() {
    TestInitializer.invocationCount.set(0);
    TestInitializer.lastConfig = null;
  }

  @Test
  void constructorsWork() {
    XsltTransformerFactory factory = new XsltTransformerFactory();
    assertNull(factory.getTransformerFactoryImpl());

    XsltTransformerFactory factoryWithImpl = new XsltTransformerFactory(SAXON_FACTORY_IMPL);
    assertEquals(SAXON_FACTORY_IMPL, factoryWithImpl.getTransformerFactoryImpl());
  }

  @Test
  void getSaxonConfigurationReturnsEmptyOnNullFactory() {
    XsltTransformerFactory factory = new XsltTransformerFactory();
    assertTrue(factory.getSaxonConfiguration(null).isEmpty());
  }

  @Test
  void getSaxonConfigurationReturnsPresentForSaxonFactory() {
    XsltTransformerFactory factory = new XsltTransformerFactory();
    assertTrue(factory.getSaxonConfiguration(new TransformerFactoryImpl()).isPresent());
  }

  @Test
  void getSaxonConfigurationReturnsEmptyForNonSaxonFactory() {
    XsltTransformerFactory factory = new XsltTransformerFactory();
    assertTrue(factory.getSaxonConfiguration(new NonSaxonTransformerFactory()).isEmpty());
  }

  @Test
  void newInstanceRunsSaxonInitializersWhenFactoryIsSaxon() {
    XsltTransformerFactory factory = new XsltTransformerFactory();
    factory.setTransformerFactoryImpl(SAXON_FACTORY_IMPL);
    factory.setSaxonInitializerClassNames(List.of(TestInitializer.class.getName()));

    TransformerFactory tf = factory.newInstance();

    assertInstanceOf(TransformerFactoryImpl.class, tf);
    assertEquals(1, TestInitializer.invocationCount.get());
    assertNotNull(TestInitializer.lastConfig);
  }

  @Test
  void newInstanceDoesNotRunSaxonInitializersWhenFactoryIsNotSaxon() {
    XsltTransformerFactory factory = new XsltTransformerFactory();
    factory.setTransformerFactoryImpl(NonSaxonTransformerFactory.class.getName());
    factory.setSaxonInitializerClassNames(List.of(TestInitializer.class.getName()));

    TransformerFactory tf = factory.newInstance();

    assertInstanceOf(NonSaxonTransformerFactory.class, tf);
    assertEquals(0, TestInitializer.invocationCount.get());
    assertNull(TestInitializer.lastConfig);
  }

  @Test
  void newInstanceIgnoresInvalidSaxonInitializerClasses() {
    XsltTransformerFactory factory = new XsltTransformerFactory();
    factory.setTransformerFactoryImpl(SAXON_FACTORY_IMPL);
    factory.setSaxonInitializerClassNames(List.of(
        "com.adaptris.missing.DoesNotExist",
        String.class.getName(),
        "",
        "   "));

    TransformerFactory tf = factory.newInstance();

    assertInstanceOf(TransformerFactoryImpl.class, tf);
    assertEquals(0, TestInitializer.invocationCount.get());
    assertNull(TestInitializer.lastConfig);
  }

  @Test
  void newInstanceContinuesWhenSaxonInitializerThrowsDuringExecution() {
    XsltTransformerFactory factory = new XsltTransformerFactory();
    factory.setTransformerFactoryImpl(SAXON_FACTORY_IMPL);
    factory.setSaxonInitializerClassNames(List.of(
        ThrowingInitializer.class.getName(),
        TestInitializer.class.getName()));

    TransformerFactory tf = factory.newInstance();

    assertInstanceOf(TransformerFactoryImpl.class, tf);
    assertEquals(1, TestInitializer.invocationCount.get());
    assertNotNull(TestInitializer.lastConfig);
  }

  @Test
  void newInstanceWorksWithDefaultTransformerFactory() {
    XsltTransformerFactory factory = new XsltTransformerFactory();
    TransformerFactory tf = factory.newInstance();
    assertNotNull(tf);
  }

  @Test
  void createTransformerFromUrlWithoutEntityResolver() throws Exception {
    XsltTransformerFactory factory = new XsltTransformerFactory();
    factory.setTransformerFactoryImpl(SAXON_FACTORY_IMPL);

    String xsl = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
        + "<xsl:template match=\"/\"><out>ok</out></xsl:template>"
        + "</xsl:stylesheet>";

    Path xslFile = Files.createTempFile("xslt-transformer-factory-", ".xsl");
    Files.writeString(xslFile, xsl, StandardCharsets.UTF_8);

    try {
      Transformer transformer = factory.createTransformerFromUrl(xslFile.toUri().toString(), null);
      assertNotNull(transformer);
    } finally {
      Files.deleteIfExists(xslFile);
    }
  }

  @Test
  void createTransformerFromUrlUsesEntityResolverWhenProvided() throws Exception {
    XsltTransformerFactory factory = new XsltTransformerFactory();
    factory.setTransformerFactoryImpl(SAXON_FACTORY_IMPL);
    factory.setXmlDocumentFactoryConfig(DocumentBuilderFactoryBuilder.newLenientInstance());

    String xsl = """
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE xsl:stylesheet [<!ENTITY ext SYSTEM "urn:test-entity">]>
        <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
          <xsl:template match="/">
            <out>&ext;</out>
          </xsl:template>
        </xsl:stylesheet>
        """;

    Path xslFile = Files.createTempFile("xslt-transformer-factory-entity-", ".xsl");
    Files.writeString(xslFile, xsl, StandardCharsets.UTF_8);

    AtomicInteger resolverCalls = new AtomicInteger(0);
    EntityResolver resolver = (publicId, systemId) -> {
      resolverCalls.incrementAndGet();
      if ("urn:test-entity".equals(systemId)) {
        return new InputSource(new StringReader("resolved-value"));
      }
      return null;
    };

    try (StringWriter output = new StringWriter()) {
      Transformer transformer = factory.createTransformerFromUrl(xslFile.toUri().toString(), resolver);
      transformer.transform(new StreamSource(new StringReader("<root/>")), new StreamResult(output));

      assertTrue(resolverCalls.get() > 0);
      assertTrue(output.toString().contains("resolved-value"));
    } finally {
      Files.deleteIfExists(xslFile);
    }
  }

  public static class TestInitializer implements Initializer {
    static final AtomicInteger invocationCount = new AtomicInteger(0);
    static volatile Configuration lastConfig;

    @Override
    public void initialize(Configuration config) {
      invocationCount.incrementAndGet();
      lastConfig = config;
    }
  }

  public static class ThrowingInitializer implements Initializer {
    @Override
    public void initialize(Configuration config) {
      throw new RuntimeException("Intentional failure from ThrowingInitializer");
    }
  }

  public static class NonSaxonTransformerFactory extends TransformerFactory {

    private URIResolver resolver;
    private ErrorListener errorListener;

    @Override
    public Transformer newTransformer(Source source) {
      throw new UnsupportedOperationException("Not used in this test");
    }

    @Override
    public Transformer newTransformer() {
      throw new UnsupportedOperationException("Not used in this test");
    }

    @Override
    public Templates newTemplates(Source source) {
      throw new UnsupportedOperationException("Not used in this test");
    }

    @Override
    public Source getAssociatedStylesheet(Source source, String media, String title, String charset) {
      throw new UnsupportedOperationException("Not used in this test");
    }

    @Override
    public void setURIResolver(URIResolver resolver) {
      this.resolver = resolver;
    }

    @Override
    public URIResolver getURIResolver() {
      return resolver;
    }

    @Override
    public void setFeature(String name, boolean value) throws TransformerConfigurationException {
      // No-op for test-only factory implementation.
    }

    @Override
    public boolean getFeature(String name) {
      return false;
    }

    @Override
    public void setAttribute(String name, Object value) {
      // No-op for test-only factory implementation.
    }

    @Override
    public Object getAttribute(String name) {
      return null;
    }

    @Override
    public void setErrorListener(ErrorListener listener) {
      this.errorListener = listener;
    }

    @Override
    public ErrorListener getErrorListener() {
      return errorListener;
    }
  }
}

