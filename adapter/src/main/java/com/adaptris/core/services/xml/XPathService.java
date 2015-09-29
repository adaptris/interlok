package com.adaptris.core.services.xml;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DataDestination;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.license.License;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * 
 * @author amcgrath
 * @config xpath-service
 * @license BASIC
 */
@XStreamAlias("xpath-service")
public class XPathService extends ServiceImp {
  
  private DataDestination sourceXmlDestination;
  
  @XStreamImplicit(itemFieldName="xpath-execution")
  private List<Execution> executions;
  
  public XPathService() {
    this.setExecutions(new ArrayList<Execution>());
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      for(Execution execution: this.getExecutions()) {
        com.adaptris.util.text.xml.XPath xPathHandler = new com.adaptris.util.text.xml.XPath();
        
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

}
