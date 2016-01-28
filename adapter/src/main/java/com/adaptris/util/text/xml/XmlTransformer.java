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

package com.adaptris.util.text.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.adaptris.core.util.DocumentBuilderFactoryBuilder;

/**
 * Responsible for applying transforms.
 * 
 */
public class XmlTransformer {
  
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  private transient DocumentBuilderFactoryBuilder builder;
  
  public XmlTransformer() {
  }
  
  public void registerBuilder(DocumentBuilderFactoryBuilder b) {
    builder = b;
  }

  /**
   * Transform method which converts an input XML document into another format using the specified XSLT
   * 
   * @param transformer the {@link Transformer}
   * @param xmlIn the input document
   * @param xmlOut the output document
   * @param xsl the stylesheet
   * @param properties optional Map of parameters to be passed to the stylesheet engine and that will be accessible from the
   * stylesheet.
   * @throws Exception in the event of parsing / transform errors
   */
  public void transform(Transformer transformer, Source xmlIn, Result xmlOut, String xsl, Map properties) throws Exception {
    if (properties != null) {
      transformer.clearParameters();
      Iterator<String> iter = properties.keySet().iterator();
      while (iter.hasNext()) {
        Object o = iter.next();
        transformer.setParameter(o.toString(), properties.get(o));
      }
    }
    transformer.transform(xmlIn, xmlOut);
  }
  
  /**
   * Transform method which converts an input XML document into another format using the specified XSLT
   * 
   * @param transformer the {@link Transformer}
   * @param xmlIn the input document
   * @param xmlOut the output document
   * @param xsl the stylesheet
   * @param properties optional Map of parameters to be passed to the stylesheet engine and that will be accessible from the
   * stylesheet.
   * @throws Exception in the event of parsing / transform errors
   */
  public void transform(Transformer transformer, InputStream xmlIn, OutputStream xmlOut, String xsl, Map properties) throws Exception {
    transform(transformer, createSource(xmlIn), new StreamResult(xmlOut), xsl, properties);
  }

  /**
   * Transform method which converts an input XML document into another format using the specified XSLT
   * 
   * @param transformer the {@link Transformer}
   * @param xmlIn the input document
   * @param xmlOut the output document
   * @param xsl the stylesheet
   * @param properties optional Map of parameters to be passed to the stylesheet engine and that will be accessible from the
   * stylesheet.
   * @throws Exception in the event of parsing / transform errors
   */
  public void transform(Transformer transformer, Reader xmlIn, Writer xmlOut, String xsl, Map properties) throws Exception {
    transform(transformer, createSource(xmlIn), new StreamResult(xmlOut), xsl, properties);
  }

  /**
   * Transform method which converts an input XML document into another format using the specified XSLT
   * 
   * @param transformer the {@link Transformer}
   * @param xmlIn the input document
   * @param xmlOut the output document
   * @param xsl the stylesheet
   * @throws Exception in the event of parsing / transform errors
   */
  public void transform(Transformer transformer, InputStream xmlIn, OutputStream xmlOut, String xsl) throws Exception {
    transform(transformer, createSource(xmlIn), new StreamResult(xmlOut), xsl, null);
  }

  /**
   * Transform method which converts an input XML document into another format using the specified XSLT
   * 
   * @param transformer the {@link Transformer}
   * @param xmlIn the input document
   * @param xmlOut the output document
   * @param xsl the stylesheet
   * @throws Exception in the event of parsing / transform errors
   */
  public void transform(Transformer transformer, Reader xmlIn, Writer xmlOut, String xsl) throws Exception {
    transform(transformer, createSource(xmlIn), new StreamResult(xmlOut), xsl, null);
  }

  /**
   * Transform method which converts an input XML document into another format using the specified XSLT
   * 
   * @param transformer the {@link Transformer}
   * @param xmlIn the input document
   * @param xmlOut the output document
   * @param xsl the stylesheet
   * @throws Exception in the event of parsing / transform errors
   */
  public void transform(Transformer transformer, Source xmlIn, Result xmlOut, String xsl) throws Exception {
    transform(transformer, xmlIn, xmlOut, xsl, null);
  }

  private Source createSource(InputStream in) throws ParserConfigurationException, SAXException, IOException {
    Source result = null;
    if (builder != null) {
      DocumentBuilder docBuilder = builder.configure(DocumentBuilderFactory.newInstance()).newDocumentBuilder();
      result = new DOMSource(docBuilder.parse(in));
    } else {
      result = new StreamSource(in);
    }
    return result;
  }

  private Source createSource(Reader in) throws ParserConfigurationException, SAXException, IOException {
    Source result = null;
    if (builder != null) {
      DocumentBuilder docBuilder = builder.configure(DocumentBuilderFactory.newInstance()).newDocumentBuilder();
      result = new DOMSource(docBuilder.parse(new InputSource(in)));
    } else {
      result = new StreamSource(in);
    }
    return result;
  }

}
