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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;

public interface XmlTransformer {
  
  /**
   * Transform method which converts an input XML document into another format using the specified XSLT
   * 
   * @param transformer the {@link Transformer}
   * @param xmlIn the input document
   * @param xmlOut the output document
   * @param xsl the stylesheet
   * @param properties optional Map of parameters to be passed to the stylesheet engine and that will be accessible from the
   *          stylesheet.
   * @throws Exception in the event of parsing / transform errors
   */
  void transform(Transformer transformer, Source xmlIn, Result xmlOut, String xsl, Map properties) throws Exception;
  
  /**
   * Transform method which converts an input XML document into another format using the specified XSLT
   * 
   * @param transformer the {@link Transformer}
   * @param xmlIn the input document
   * @param xmlOut the output document
   * @param xsl the stylesheet
   * @param properties optional Map of parameters to be passed to the stylesheet engine and that will be accessible from the
   *          stylesheet.
   * @throws Exception in the event of parsing / transform errors
   */
  void transform(Transformer transformer, InputStream xmlIn, OutputStream xmlOut, String xsl, Map properties) throws Exception;

  /**
   * Transform method which converts an input XML document into another format using the specified XSLT
   * 
   * @param transformer the {@link Transformer}
   * @param xmlIn the input document
   * @param xmlOut the output document
   * @param xsl the stylesheet
   * @param properties optional Map of parameters to be passed to the stylesheet engine and that will be accessible from the
   *          stylesheet.
   * @throws Exception in the event of parsing / transform errors
   */
  void transform(Transformer transformer, Reader xmlIn, Writer xmlOut, String xsl, Map properties) throws Exception;

  /**
   * Transform method which converts an input XML document into another format using the specified XSLT
   * 
   * @param transformer the {@link Transformer}
   * @param xmlIn the input document
   * @param xmlOut the output document
   * @param xsl the stylesheet
   * @throws Exception in the event of parsing / transform errors
   */
  void transform(Transformer transformer, InputStream xmlIn, OutputStream xmlOut, String xsl) throws Exception;

  /**
   * Transform method which converts an input XML document into another format using the specified XSLT
   * 
   * @param transformer the {@link Transformer}
   * @param xmlIn the input document
   * @param xmlOut the output document
   * @param xsl the stylesheet
   * @throws Exception in the event of parsing / transform errors
   */
  void transform(Transformer transformer, Reader xmlIn, Writer xmlOut, String xsl) throws Exception;
  /**
   * Transform method which converts an input XML document into another format using the specified XSLT
   * 
   * @param transformer the {@link Transformer}
   * @param xmlIn the input document
   * @param xmlOut the output document
   * @param xsl the stylesheet
   * @throws Exception in the event of parsing / transform errors
   */
  void transform(Transformer transformer, Source xmlIn, Result xmlOut, String xsl) throws Exception;

  
}
