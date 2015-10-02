package com.adaptris.core.services.xml;

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

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.common.PayloadDataDestination;
import com.adaptris.interlok.config.DataDestination;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.license.License;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * <p>
 * This service allows you to configure an xpath expression which will be executed on source xml, the result of which can be saved to multiple locations.
 * </p>
 * <p>
 * To specify where the source xml, source xpath expression and the result of the xpath execution should be saved, you will use {@link DataDestination} for each.
 * <br />
 * For example you can specify the source xml can be found in the {@link AdaptrisMessage} payload, by using {@link PayloadDataDestination} like this;
 * <pre>
 * {@code
 * <xpath-service>
 *   <source-xml-destination class="payload-data-destination"/>
 *   ...
 * }
 * </pre>
 * 
 * And perhaps the source xpath expression will be configured directly in Interlok config, using {@link ConstantDataDestination};
 * 
 * <pre>
 * {@code
 * <xpath-service>
 *   <xpath-execution>
 *     <source-xpath-expression class="constant-data-destination">
 *       <value>//my/xpath/expression</value>
 *     </source-xpath-expression>
 *   ...
 * }
 * </pre>
 * 
 * And then maybe the result of the xpath execution is to be saved in {@link AdaptrisMessage} metadata, using {@link MetadataDataDestination};
 * 
 * <pre>
 * {@code
 * <xpath-service>
 *   <xpath-execution>
 *     <target-data-destination class="metadata-data-destination">
 *       <metadata-key>targetMetadataKey</metadata-key>
 *     </target-data-destination>
 *   ...
 * }
 * </pre>
 * </p>
 * <p>
 * While you may only specify a single source xml destination, you may if you wish apply multiple XPath expressions, each of which saves the result to a different location.
 * <br />
 * To do this, simply configure multiple executions.  Take the following example, where we specify the payload containing the source xml and 3 XPath expressions will be executed
 * each of which will store the result in 3 different metadata items;
 * <pre>
 * {@code
 * <xpath-service>
 *   <source-xml-destination class="payload-data-destination"/>
 * 
 *   <xpath-execution>
 *     <source-xpath-expression class="constant-data-destination">
 *       <value>//my/first/xpath/expression</value>
 *     </source-xpath-expression>
 *     
 *     <target-data-destination class="metadata-data-destination">
 *       <metadata-key>targetMetadataKey1</metadata-key>
 *     </target-data-destination>
 *   </xpath-execution>
 *   
 *   <xpath-execution>
 *     <source-xpath-expression class="constant-data-destination">
 *       <value>//my/second/xpath/expression</value>
 *     </source-xpath-expression>
 *     
 *     <target-data-destination class="metadata-data-destination">
 *       <metadata-key>targetMetadataKey2</metadata-key>
 *     </target-data-destination>
 *   </xpath-execution>
 *   
 *   <xpath-execution>
 *     <source-xpath-expression class="constant-data-destination">
 *       <value>//my/third/xpath/expression</value>
 *     </source-xpath-expression>
 *     
 *     <target-data-destination class="metadata-data-destination">
 *       <metadata-key>targetMetadataKey3</metadata-key>
 *     </target-data-destination>
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
 * @author amcgrath
 * @config xpath-service
 * @license BASIC
 */
@XStreamAlias("xpath-service")
public class XPathService extends ServiceImp {
  
  @NotNull
  @AutoPopulated
  @Valid
  private DataDestination sourceXmlDestination;
  
  @NotNull
  @Valid
  @XStreamImplicit(itemFieldName="xpath-execution")
  private List<Execution> executions;
  
  @Valid
  @AutoPopulated
  private KeyValuePairSet namespaceContext;
  
  public XPathService() {
    this.setExecutions(new ArrayList<Execution>());
    this.setSourceXmlDestination(new PayloadDataDestination());
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    NamespaceContext namespaceContext = SimpleNamespaceContext.create(getNamespaceContext(), msg);
    try {
      for(Execution execution: this.getExecutions()) {
        com.adaptris.util.text.xml.XPath xPathHandler = new com.adaptris.util.text.xml.XPath(namespaceContext);
        
        Document document = buildDocument((String) this.getSourceXmlDestination().getData(msg));
        execution.getTargetDataDestination().setData(msg, this.serializeNode(xPathHandler.selectNodeList(document, (String) execution.getSourceXpathExpression().getData(msg))));
      }
    } catch (Exception ex) {
      throw new ServiceException(ex);
    }
  }

  private Document buildDocument(String xmlData) throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
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
  public boolean isEnabled(License license) throws CoreException {
    return true;
  }

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void close() {
  }

  public DataDestination getSourceXmlDestination() {
    return sourceXmlDestination;
  }

  public void setSourceXmlDestination(DataDestination sourceDestination) {
    this.sourceXmlDestination = sourceDestination;
  }

  public List<Execution> getExecutions() {
    return executions;
  }

  public void setExecutions(List<Execution> executions) {
    this.executions = executions;
  }

  public KeyValuePairSet getNamespaceContext() {
    return namespaceContext;
  }

  public void setNamespaceContext(KeyValuePairSet namespaceContext) {
    this.namespaceContext = namespaceContext;
  }

}
