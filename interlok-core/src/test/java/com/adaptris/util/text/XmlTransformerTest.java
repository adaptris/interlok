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
package com.adaptris.util.text;

import static com.adaptris.core.transform.XmlTransformServiceTest.KEY_XML_TEST_INPUT;
import static com.adaptris.core.transform.XmlTransformServiceTest.KEY_XML_TEST_TRANSFORM_URL;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.stubs.MessageHelper;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.util.text.xml.XmlTransformer;
import com.adaptris.util.text.xml.XmlTransformerFactory;
import com.adaptris.util.text.xml.XsltTransformerFactory;

public class XmlTransformerTest extends com.adaptris.interlok.junit.scaffolding.BaseCase {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testTransform() throws Exception {
    XmlTransformerFactory factory = new XsltTransformerFactory();
    XmlTransformer transform = factory.configure(new XmlTransformer());
    transform.registerBuilder(DocumentBuilderFactoryBuilder.newInstance());
    String xsl = backslashToSlash(PROPERTIES.getProperty(KEY_XML_TEST_TRANSFORM_URL));
    AdaptrisMessage m1 = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    try (InputStream in = m1.getInputStream(); OutputStream out = m1.getOutputStream()) {
      StreamResult output = new StreamResult(out);
      StreamSource input = new StreamSource(in);
      transform.transform(factory.createTransformer(xsl), input, output, xsl, new HashMap<>(System.getProperties()));
    }
    AdaptrisMessage m2 = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));

    try (InputStream in = m2.getInputStream(); OutputStream out = m2.getOutputStream()) {
      StreamResult output = new StreamResult(out);
      StreamSource input = new StreamSource(in);
      transform.transform(factory.createTransformer(xsl), input, output, xsl);
    }
  }

  @Test
  public void testTransform_InputStreamOutputStream() throws Exception {
    XmlTransformerFactory factory = new XsltTransformerFactory();
    XmlTransformer transform = factory.configure(new XmlTransformer());
    String xsl = backslashToSlash(PROPERTIES.getProperty(KEY_XML_TEST_TRANSFORM_URL));
    AdaptrisMessage m1 = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    try (InputStream in = m1.getInputStream(); OutputStream out = m1.getOutputStream()) {
      transform.transform(factory.createTransformer(xsl), in, out, xsl);
    }
  }

  @Test
  public void testTransform_ReaderWriter() throws Exception {
    XmlTransformerFactory factory = new XsltTransformerFactory();
    XmlTransformer transform = new XmlTransformer();
    String xsl = backslashToSlash(PROPERTIES.getProperty(KEY_XML_TEST_TRANSFORM_URL));
    AdaptrisMessage m1 = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_XML_TEST_INPUT));
    try (Reader in = m1.getReader(); Writer out = m1.getWriter()) {
      transform.transform(factory.createTransformer(xsl), in, out, xsl);
    }
  }

  private static String backslashToSlash(String url) {
    if (!isEmpty(url)) {
      return url.replaceAll("\\\\", "/");
    }
    return url;
  }
}
