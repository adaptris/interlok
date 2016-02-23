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

package com.adaptris.core.services.aggregator;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.Collection;

import org.w3c.dom.Document;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.text.xml.DocumentMerge;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link MessageAggregator} implementation that creates single XML using each message that needs to be joined up.
 * 
 * <p>
 * The original pre-split document is completely ignored; you should specify in the template the XML document that will be used to
 * merge split documents.
 * </p>
 * <p>
 * Use {@link #setDocumentEncoding(String)} to force the encoding of the resulting XML document to the required value; if not set,
 * then either the original {@link com.adaptris.core.AdaptrisMessage#getCharEncoding()} (if set) or <code>UTF-8</code> will be used in that order.
 * </p>
 * 
 * @config ignore-original-xml-document-aggregator
 * @author lchan
 * 
 */
@XStreamAlias("ignore-original-xml-document-aggregator")
@DisplayOrder(order = {"template", "documentEncoding", "mergeImplementation", "xmlDocumentFactoryConfig"})
public class IgnoreOriginalXmlDocumentAggregator extends XmlDocumentAggregator {

  private String template;

  public IgnoreOriginalXmlDocumentAggregator() {
  }

  public IgnoreOriginalXmlDocumentAggregator(String template) {
    this();
    setTemplate(template);
  }

  public IgnoreOriginalXmlDocumentAggregator(String template, DocumentMerge merge) {
    this();
    setTemplate(template);
    setMergeImplementation(merge);
  }

  @Override
  public void joinMessage(AdaptrisMessage original, Collection<AdaptrisMessage> messages) throws CoreException {
    try {
      if (isEmpty(getTemplate())) {
        throw new CoreException("Template is null / empty, cannot continue");
      }
      Document resultDoc = XmlHelper.createDocument(getTemplate());
      for (AdaptrisMessage m : messages) {
        Document mergeDoc = XmlHelper.createDocument(m, documentFactoryBuilder());
        overwriteMetadata(m, original);
        resultDoc = getMergeImplementation().merge(resultDoc, mergeDoc);
      }
      writeXmlDocument(resultDoc, original);
    }
    catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }
  }

  /**
   * @return the template
   */
  public String getTemplate() {
    return template;
  }

  /**
   * Set the template for the resulting XML document
   * 
   * @param s the template to set; wrapped in CDATA tags as appropriate.
   */
  public void setTemplate(String s) {
    this.template = s;
  }
}
