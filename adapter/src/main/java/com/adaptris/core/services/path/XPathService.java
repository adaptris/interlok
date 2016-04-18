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

package com.adaptris.core.services.path;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.common.Execution;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * <p>
 * This service allows you to configure an xpath expression which will be executed on source xml, the result of which can be saved 
 * to multiple locations.
 * </p>
 * <p>
 * To specify where the source xml, source xpath expression and the result of the xpath execution should be saved, you shoud
 * use {@link DataInputParameter} or {@link com.adaptris.interlok.config.DataOutputParameter}.
 * <br />
 * For example you can specify the source xml can be found in the {@link com.adaptris.core.AdaptrisMessage} payload, by using 
 * {@link StringPayloadDataInputParameter} like this :
 * <pre>
 * {@code
 * <xpath-service>
 *   <xml-source class="string-payload-data-input-parameter"/>
 *   ...
 * }
 * </pre>
 * 
 * And perhaps the source xpath expression will be configured directly in Interlok config, using 
 * {@link com.adaptris.core.common.ConstantDataInputParameter};
 * 
 * <pre>
 * {@code
 * <xpath-service>
 *   <xpath-execution>
 *     <source class="constant-data-input-parameter">
 *       <value>//my/xpath/expression</value>
 *     </source>
 *   ...
 * }
 * </pre>
 * 
 * And then maybe the result of the xpath execution is to be saved in {@link com.adaptris.core.AdaptrisMessage} metadata, using 
 * {@link com.adaptris.core.common.MetadataDataOutputParameter};
 * 
 * <pre>
 * {@code
 * <xpath-service>
 *   <xpath-execution>
 *     <target class="metadata-data-output-parameter">
 *       <metadata-key>targetMetadataKey</metadata-key>
 *     </target>
 *   ...
 * }
 * </pre>
 * </p>
 * <p>
 * While you may only specify a single source xml destination, you may if you wish apply multiple XPath expressions, 
 * each of which saves the result to a different location. To do this, simply configure multiple executions.  Take the following 
 * example, where we specify the payload containing the source xml and 3 XPath expressions will be executed each of which will 
 * store the result in 3 different metadata items;
 * <pre>
 * {@code
 * <xpath-service>
 *   <xml-source class="string-payload-data-input-parameter"/>
 * 
 *   <xpath-execution>
 *     <source class="constant-data-input-parameter">
 *       <value>//my/first/xpath/expression</value>
 *     </source>
 *     
 *     <target class="metadata-data-output-parameter">
 *       <metadata-key>targetMetadataKey1</metadata-key>
 *     </target>
 *   </xpath-execution>
 *   
 *   <xpath-execution>
 *     <source class="constant-data-input-parameter">
 *       <value>//my/second/xpath/expression</value>
 *     </source>
 *     
 *     <target class="metadata-data-output-parameter">
 *       <metadata-key>targetMetadataKey2</metadata-key>
 *     </target>
 *   </xpath-execution>
 *   
 *   <xpath-execution>
 *     <source class="constant-data-input-parameter">
 *       <value>//my/third/xpath/expression</value>
 *     </source>
 *     
 *     <target class="metadata-data-output-parameter">
 *       <metadata-key>targetMetadataKey3</metadata-key>
 *     </target>
 *   </xpath-execution>
 * 
 * </xpath-service>
 * }
 * </pre>
 * </p>
 * <p>
 * Should your source xml contain namespaces, you will need to configure the mappings in this service like this;
 * <pre>
 * {@code
 * <xpath-service>
 * ...
 *   <namespace-context>
 *     <key-value-pair>
 *       <key>n1</key>
 *       <value>http://adaptris.com/xml/namespace1</value>
 *     </key-value-pair>
 *     <key-value-pair>
 *       <key>n2</key>
 *       <value>http://adaptris.com/xml/namespace2</value>
 *     </key-value-pair>
 *     <key-value-pair>
 *       <key>n3</key>
 *       <value>http://adaptris.com/xml/namespace3</value>
 *     </key-value-pair>
 *     <key-value-pair>
 *       <key>n4</key>
 *       <value>http://adaptris.com/xml/namespace4</value>
 *     </key-value-pair>
 *    </namespace-context>
 *  ...
 * </xpath-service>
 * }
 * </pre>
 * </p>
 * 
 * @since 3.0.6
 * @author amcgrath
 * @config xpath-service
 * 
 */
@XStreamAlias("xpath-service")
@AdapterComponent
@ComponentProfile(summary = "Extract data via XPath and store it", tag = "service,xml")
@DisplayOrder(order = {"xmlSource", "executions", "namespaceContext", "xmlDocumentFactoryConfig"})
public class XPathService extends ServiceImp {
  
  @NotNull
  @AutoPopulated
  @Valid
  private DataInputParameter<String> xmlSource;
  
  @NotNull
  @Valid
  @AutoPopulated
  @XStreamImplicit(itemFieldName="xpath-execution")
  private List<Execution> executions;
  
  @AdvancedConfig
  private KeyValuePairSet namespaceContext;
  @AdvancedConfig
  private DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig;
  
  public XPathService() {
    this.setExecutions(new ArrayList<Execution>());
    this.setXmlSource(new StringPayloadDataInputParameter());
  }

  // @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    NamespaceContext namespaceContext = SimpleNamespaceContext.create(getNamespaceContext(), msg);
    try {
      Document document = buildDocument(this.getXmlSource().extract(msg));
      com.adaptris.util.text.xml.XPath xPathHandler = new com.adaptris.util.text.xml.XPath(namespaceContext);
      for (Execution execution : this.getExecutions()) {
        String result = this.serializeNode(xPathHandler.selectNodeList(document, execution.getSource().extract(msg)));
        execution.getTarget().insert(result, msg);
      }
    } catch (Exception ex) {
      throw new ServiceException(ex);
    }
  }

  private Document buildDocument(String xmlData) throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory factory = documentFactoryBuilder().configure(DocumentBuilderFactory.newInstance());
    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.parse(new InputSource(new StringReader(xmlData)));
  }

  private String serializeNode(NodeList nodeList) throws TransformerException {
    StringBuilder stringBuilder = new StringBuilder();
    for(int counter = 0; counter < nodeList.getLength(); counter ++) {
      stringBuilder.append(this.serializeNode(nodeList.item(counter)));
    }
    return stringBuilder.toString();
  }
  
  private String serializeNode(Node node) throws TransformerException {
    StreamResult xmlOutput = new StreamResult(new StringWriter());
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.transform(new DOMSource(node), xmlOutput);
    return xmlOutput.getWriter().toString();
  }
  
  @Override
  public void prepare() throws CoreException {
  }


  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  public DataInputParameter<String> getXmlSource() {
    return xmlSource;
  }

  public void setXmlSource(DataInputParameter<String> sourceDestination) {
    this.xmlSource = Args.notNull(sourceDestination, "source-xml");
  }

  public List<Execution> getExecutions() {
    return executions;
  }

  public void setExecutions(List<Execution> list) {
    this.executions = Args.notNull(list, "xpath executions");
  }

  public KeyValuePairSet getNamespaceContext() {
    return namespaceContext;
  }

  public void setNamespaceContext(KeyValuePairSet namespaceContext) {
    this.namespaceContext = namespaceContext;
  }

  public DocumentBuilderFactoryBuilder getXmlDocumentFactoryConfig() {
    return xmlDocumentFactoryConfig;
  }


  public void setXmlDocumentFactoryConfig(DocumentBuilderFactoryBuilder xml) {
    this.xmlDocumentFactoryConfig = xml;
  }

  DocumentBuilderFactoryBuilder documentFactoryBuilder() {
    return getXmlDocumentFactoryConfig() != null ? getXmlDocumentFactoryConfig()
        : DocumentBuilderFactoryBuilder.newInstance().withNamespaceAware(true);
  }

}
