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

package com.adaptris.core.services.metadata;

import com.adaptris.annotation.*;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.metadata.xpath.XpathQuery;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.adaptris.util.text.xml.XPath;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.w3c.dom.Document;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.namespace.NamespaceContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Store values extracted from message payload using {@link XpathQuery} as metadata.
 * <p>
 * If the {@code DocumentBuilderFactoryBuilder} has been explicitly set to be not namespace aware and the document does in fact
 * contain namespaces, then Saxon can cause merry havoc in the sense that {@code //NonNamespaceXpath} doesn't work if the document
 * has namespaces in it. We have included a shim so that behaviour can be toggled based on what you have configured.
 * </p>
 * 
 * @see XPath#newXPathInstance(DocumentBuilderFactoryBuilder, NamespaceContext)
 * @config xpath-metadata-service
 * 
 * 
 */
@XStreamAlias("xpath-metadata-service")
@AdapterComponent
@ComponentProfile(summary = "Extract data via XPath and store it as metadata", tag = "service,metadata,xml,xpath")
@DisplayOrder(order = {"xpathQueries", "namespaceContext", "xmlDocumentFactoryConfig", "metadataLogger"})
public class XpathMetadataService extends MetadataServiceImpl {

  @NotNull
  @AutoPopulated
  @Valid
  @XStreamImplicit(itemFieldName = "xpath-query")
  private List<XpathQuery> xpathQueries;
  @AdvancedConfig
  @Valid
  private KeyValuePairSet namespaceContext;
  @AdvancedConfig
  @Valid
  private DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig;

  private transient List<XpathQuery> queriesToExecute;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public XpathMetadataService() {
    setXpathQueries(new ArrayList<XpathQuery>());
  }

  @Override
  protected void initService() throws CoreException {
    for (XpathQuery query : xpathQueries) {
      query.verify();
    }
    queriesToExecute = new ArrayList<>();
    queriesToExecute.addAll(getXpathQueries());
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {

    Set<MetadataElement> metadataElements = new HashSet<MetadataElement>();
    NamespaceContext namespaceCtx = SimpleNamespaceContext.create(getNamespaceContext(), msg);
    try {
      DocumentBuilderFactoryBuilder builder = documentFactoryBuilder();
      if (namespaceCtx != null) {
        builder.setNamespaceAware(true);
      }
      XPath xpathToUse = XPath.newXPathInstance(builder, namespaceCtx);
      Document doc = XmlHelper.createDocument(msg, builder);
      for (XpathQuery query : queriesToExecute) {
        metadataElements.add(query.resolveXpath(doc, xpathToUse, query.createXpathQuery(msg)));
      }
      logMetadata("Xpath Metadata resolved {}", metadataElements);
      msg.setMetadata(metadataElements);
    }
    catch (Exception e) {
      throw new ServiceException(e);
    }
  }


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
   * @param namespaceContext
   */
  public void setNamespaceContext(KeyValuePairSet namespaceContext) {
    this.namespaceContext = namespaceContext;
  }


  public List<XpathQuery> getXpathQueries() {
    return xpathQueries;
  }

  /**
   * Set the list of {@linkplain XpathQuery} instances that will be executed.
   *
   * @param xql
   */
  public void setXpathQueries(List<XpathQuery> xql) {
    xpathQueries = Args.notNull(xql, "xpathQueries");
  }

  public void addXpathQuery(XpathQuery query) {
    xpathQueries.add(Args.notNull(query, "query"));
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
