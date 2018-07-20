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

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.OutputStream;
import java.util.Collection;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.w3c.dom.Document;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.XmlUtils;
import com.adaptris.util.text.xml.DocumentMerge;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link MessageAggregator} implementation that creates single XML using each message that needs to be joined up.
 * 
 * <p>
 * The original pre-split document forms the basis of the resulting document; each of the split documents is merged into the main
 * document using the configured {@link DocumentMerge} function.
 * </p>
 * <p>
 * Use {@link #setDocumentEncoding(String)} to force the encoding of the resulting XML document to the required value; if not set,
 * then either the original {@link com.adaptris.core.AdaptrisMessage#getCharEncoding()} (if set) or <code>UTF-8</code> will be used in that order.
 * </p>
 * 
 * @config xml-document-aggregator
 * @author lchan
 * 
 */
@XStreamAlias("xml-document-aggregator")
@DisplayOrder(order = {"documentEncoding", "mergeImplementation", "xmlDocumentFactoryConfig"})
public class XmlDocumentAggregator extends MessageAggregatorImpl {

  @AdvancedConfig
  private String documentEncoding;
  @NotNull
  @Valid
  private DocumentMerge mergeImplementation;
  @AdvancedConfig
  private DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig;

  public XmlDocumentAggregator() {
  }

  public XmlDocumentAggregator(DocumentMerge merge) {
    this();
    setMergeImplementation(merge);
  }

  @Override
  public void joinMessage(AdaptrisMessage original, Collection<AdaptrisMessage> messages) throws CoreException {
    try {
      Document resultDoc = XmlHelper.createDocument(original, documentFactoryBuilder());
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

  private String evaluateEncoding(AdaptrisMessage msg) {
    String encoding = "UTF-8";
    if (!isEmpty(getDocumentEncoding())) {
      encoding = getDocumentEncoding();
    }
    else if (!isEmpty(msg.getContentEncoding())) {
      encoding = msg.getContentEncoding();
    }
    return encoding;
  }

  protected void writeXmlDocument(Document doc, AdaptrisMessage msg) throws Exception {
    OutputStream out = null;
    try {
      String encoding = evaluateEncoding(msg);
      out = msg.getOutputStream();
      new XmlUtils().writeDocument(doc, out, encoding);
      msg.setContentEncoding(encoding);
    }
    finally {
      closeQuietly(out);
    }
  }

  /**
   * @return the documentEncoding
   */
  public String getDocumentEncoding() {
    return documentEncoding;
  }

  /**
   * Set the XML encoding for the resulting document.
   * 
   * @param s the documentEncoding to set (defaults to UTF-8).
   */
  public void setDocumentEncoding(String s) {
    this.documentEncoding = s;
  }

  /**
   * @return the mergeImplementation
   */
  public DocumentMerge getMergeImplementation() {
    return mergeImplementation;
  }

  /**
   * Set how to merge the split documents into the main XML document.
   * 
   * @param dm the mergeImplementation to set
   */
  public void setMergeImplementation(DocumentMerge dm) {
    this.mergeImplementation = Args.notNull(dm, "mergeImplementation");
  }

  public DocumentBuilderFactoryBuilder getXmlDocumentFactoryConfig() {
    return xmlDocumentFactoryConfig;
  }


  public void setXmlDocumentFactoryConfig(DocumentBuilderFactoryBuilder xml) {
    this.xmlDocumentFactoryConfig = xml;
  }

  DocumentBuilderFactoryBuilder documentFactoryBuilder() {
    return DocumentBuilderFactoryBuilder.newInstance(getXmlDocumentFactoryConfig());
  }
}
