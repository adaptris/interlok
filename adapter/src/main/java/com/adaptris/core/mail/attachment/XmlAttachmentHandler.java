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

package com.adaptris.core.mail.attachment;
import static com.adaptris.util.text.xml.XPath.newXPathInstance;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.MimeUtility;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.namespace.NamespaceContext;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.IdGenerator;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.stream.StreamUtil;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.adaptris.util.text.xml.XPath;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Handle attachments for {@link MultiAttachmentSmtpProducer}.
 * <p>
 * If the {@code DocumentBuilderFactoryBuilder} has been explicitly set to be not namespace aware and the document does in fact
 * contain namespaces, then Saxon can cause merry havoc in the sense that {@code //NonNamespaceXpath} doesn't work if the document
 * has namespaces in it. We have included a shim so that behaviour can be toggled based on what you have configured.
 * </p>
 * 
 * @see XPath#newXPathInstance(DocumentBuilderFactoryBuilder, NamespaceContext)
 * @config mail-xml-attachment-handler
 */
@XStreamAlias("mail-xml-attachment-handler")
@DisplayOrder(order = {"xpath", "filenameXpath", "encodingXpath", "namespaceContext"})
public class XmlAttachmentHandler implements AttachmentHandler {
  @NotNull
  @NotBlank
  private String xpath;
  @AdvancedConfig
  private String filenameXpath;
  @AdvancedConfig
  private String encodingXpath;
  @AdvancedConfig
  @InputFieldDefault(value = "base64")
  private String attachmentEncoding;
  private transient IdGenerator idGenerator = null;
  @AdvancedConfig
  private KeyValuePairSet namespaceContext;
  @AdvancedConfig
  @Valid
  private DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig;

  private transient Logger logR = LoggerFactory.getLogger(this.getClass().getName());

  public XmlAttachmentHandler() {
    idGenerator = new GuidGenerator();
  }

  public XmlAttachmentHandler(String xpath, String fnameXpath) {
    this();
    setXpath(xpath);
    setFilenameXpath(fnameXpath);
  }

  public XmlAttachmentHandler(String xpath, String filenameXpath, String encoding) {
    this(xpath, filenameXpath);
    setEncodingXpath(encoding);
  }

  /**
   * @return the xpath
   */
  public String getXpath() {
    return xpath;
  }

  /**
   * The XPath that will produce one or more attachments.
   *
   * @param s the xpath to set
   */
  public void setXpath(String s) {
    xpath = s;
  }

  /**
   * @return the filenameXpath
   */
  public String getFilenameXpath() {
    return filenameXpath;
  }

  /**
   * The Xpath that determines the file name associated with this attachment.
   *
   * @param filenameXpath the filenameXpath to set
   */
  public void setFilenameXpath(String filenameXpath) {
    this.filenameXpath = filenameXpath;
  }

  @Override
  public List<MailAttachment> resolve(Document doc) throws Exception {
    DocumentBuilderFactoryBuilder builder = documentFactoryBuilder();
    XPath x = newXPathInstance(builder, SimpleNamespaceContext.create(getNamespaceContext()));
    List<MailAttachment> result = new ArrayList<MailAttachment>();
    logR.trace("Resolving {}", getXpath());
    NodeList nl = x.selectNodeList(doc, getXpath());
    if (nl == null) {
      return result;
    }
    else {
      logR.trace("Found {} attachments", nl.getLength());
    }
    for (int i = 0; i < nl.getLength(); i++) {
      Node n = nl.item(i);
      n.normalize();
      String filename = null;
      if (getFilenameXpath() != null) {
        filename = x.selectSingleTextItem(n, getFilenameXpath());
        logR.trace("Found filename [{}] from XPath [{}]", filename, getFilenameXpath());
      }
      if (filename == null) {
        filename = idGenerator.create(n);
        logR.warn("Could not determine filename for MimeBodyPart, assigning unique filename of {}", filename);
      }
      result.add(new MailAttachment(getData(n), filename).withContentTransferEncoding(getAttachmentEncoding()));
    }
    return result;
  }

  protected byte[] getData(Node n) throws Exception {
    XPath x = new XPath(SimpleNamespaceContext.create(getNamespaceContext()));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    n.normalize();
    String s = n.getTextContent();
    ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes());
    InputStream encodedIn = in;
    if (getEncodingXpath() != null) {
      String encoding = x.selectSingleTextItem(n, getEncodingXpath());
      logR.trace("Found encoding type [{}] from XPath [{}]", encoding, getEncodingXpath());
      encodedIn = MimeUtility.decode(in, encoding);
    }
    StreamUtil.copyStream(encodedIn, out);
    out.flush();
    return out.toByteArray();
  }

  /**
   * @return the encodingXpath
   */
  public String getEncodingXpath() {
    return encodingXpath;
  }

  /**
   * If specified the value returned by the xpath will be used to decode the
   * contents of the attachment xpath.
   *
   * @param s the encodingXpath to set
   */
  public void setEncodingXpath(String s) {
    encodingXpath = s;
  }

  /**
   * @return the namespaceContext
   */
  public KeyValuePairSet getNamespaceContext() {
    return namespaceContext;
  }

  /**
   * Set the namespace context for resolving namespaces.
   * <ul>
   * <li>The key is the namespace prefix</li>
   * <li>The value is the namespace uri</li>
   * </ul>
   * 
   * @param kvps the namespace context
   * @see SimpleNamespaceContext#create(KeyValuePairSet)
   */
  public void setNamespaceContext(KeyValuePairSet kvps) {
    this.namespaceContext = kvps;
  }

  public String getAttachmentEncoding() {
    return attachmentEncoding;
  }

  /**
   * Specify the Content-Transfer-Encoding to be associated with each attachment.
   * 
   * @param e the encoding; default is base64 if not specified.
   */
  public void setAttachmentEncoding(String e) {
    this.attachmentEncoding = e;
  }

  public XmlAttachmentHandler withAttachmentEncoding(String s) {
    setAttachmentEncoding(s);
    return this;
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
