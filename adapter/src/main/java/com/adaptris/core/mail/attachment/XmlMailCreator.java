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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.mail.MailException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * MailCreator implementation that assumes the AdaptrisMessage is an XML document.
 * 
 * @config mail-xml-content-creator
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("mail-xml-content-creator")
public class XmlMailCreator implements MailContentCreator {

  private AttachmentHandler attachmentHandler;
  private BodyHandler bodyHandler;
  private transient Logger logR = LoggerFactory.getLogger(this.getClass());
  @AdvancedConfig
  private DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig;

  public XmlMailCreator() {
  }

  /**
   * @see MailContentCreator#createAttachments(AdaptrisMessage)
   */
  @Override
  public List<MailAttachment> createAttachments(AdaptrisMessage msg)
      throws MailException {
    if (attachmentHandler == null) {
      throw new MailException("No way of selecting the Attachments");
    }
    List<MailAttachment> result = new ArrayList<MailAttachment>();
    InputStream in = null;
    try {
      in = msg.getInputStream();
      Document d = documentBuilder().parse(in);
      result = attachmentHandler.resolve(d);
    }
    catch (Exception e) {
      throw new MailException(e);
    }
    finally {
      IOUtils.closeQuietly(in);
    }
    return result;
  }

  /**
   * @see MailContentCreator#createBody(AdaptrisMessage)
   */
  @Override
  public MailContent createBody(AdaptrisMessage msg) throws MailException {
    if (bodyHandler == null) {
      throw new MailException("No way of selecting the Body");
    }
    MailContent result = null;
    InputStream in = null;
    try {
      in = msg.getInputStream();
      Document d = documentBuilder().parse(in);
      result = bodyHandler.resolve(d);
    }
    catch (Exception e) {
      throw new MailException(e);
    }
    finally {
      IOUtils.closeQuietly(in);
    }
    return result;
  }

  /**
   * @return the attachmentHandler
   */
  public AttachmentHandler getAttachmentHandler() {
    return attachmentHandler;
  }

  /**
   * Set the Attachment handler.
   *
   * @param a the attachmentHandler to set
   */
  public void setAttachmentHandler(AttachmentHandler a) {
    attachmentHandler = a;
  }

  /**
   * @return the bodyHandler
   */
  public BodyHandler getBodyHandler() {
    return bodyHandler;
  }

  /**
   * Set the handler for extracting the Body from the XML message.
   *
   * @param b the bodyHandler to set
   */
  public void setBodyHandler(BodyHandler b) {
    bodyHandler = b;
  }

  public DocumentBuilderFactoryBuilder getXmlDocumentFactoryConfig() {
    return xmlDocumentFactoryConfig;
  }


  public void setXmlDocumentFactoryConfig(DocumentBuilderFactoryBuilder xml) {
    this.xmlDocumentFactoryConfig = xml;
  }

  DocumentBuilder documentBuilder() throws ParserConfigurationException {
    DocumentBuilderFactoryBuilder fac = getXmlDocumentFactoryConfig() != null
        ? getXmlDocumentFactoryConfig()
        : DocumentBuilderFactoryBuilder.newInstance().withNamespaceAware(true);
    return fac.newDocumentBuilder(DocumentBuilderFactory.newInstance());
  }
}
