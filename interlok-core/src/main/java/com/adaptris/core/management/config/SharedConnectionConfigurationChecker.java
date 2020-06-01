package com.adaptris.core.management.config;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.UnifiedBootstrap;

public class SharedConnectionConfigurationChecker implements ConfigurationChecker {
  
  private static final String FRIENDLY_NAME = "Shared connection check.";
  
  private static final String DESCRIPTION = "This test will scan your configuration for shared connections, making sure each is used and referenced correctly.";
  
  private static final String XPATH_AVAILABLE_CONNECTIONS = "//shared-components/connections/*/unique-id";
  
  private static final String XPATH_REFERENCED_CONSUME_CONNECTIONS = "//consume-connection/lookup-name";

  private static final String XPATH_REFERENCED_PRODUCE_CONNECTIONS = "//produce-connection/lookup-name";

  @Override
  public ConfigurationCheckReport performConfigCheck(BootstrapProperties bootProperties, UnifiedBootstrap bootstrap) {
    ConfigurationCheckReport report = new ConfigurationCheckReport();
    report.setCheckName(this.getFriendlyName());
    
    try {      
      Document xmlDocument = buildXmlDocument(bootProperties);
      
      NodeList availableConnections = scanForConnections(xmlDocument, XPATH_AVAILABLE_CONNECTIONS);
      System.err.println("Found " + availableConnections.getLength() + " available connection(s).");
      
      NodeList referencedConsumeConnections = scanForConnections(xmlDocument, XPATH_REFERENCED_CONSUME_CONNECTIONS);
      System.err.println("Found " + referencedConsumeConnections.getLength() + " referenced consume connection(s).");
      
      NodeList referencedProduceConnections = scanForConnections(xmlDocument, XPATH_REFERENCED_PRODUCE_CONNECTIONS);
      System.err.println("Found " + referencedProduceConnections.getLength() + " referenced produce connection(s).");
      
      boolean warningsFound = false;
      StringBuilder warningText = new StringBuilder();
      
      // **********************************
      // Check all shared connections are used.
      for(int counter = 0; counter < availableConnections.getLength(); counter ++) {
        if(!this.testReferenceExistsInNodeSets(availableConnections.item(counter).getTextContent(), referencedConsumeConnections)) {
          warningsFound = true;
          warningText.append("\nShared connection unused: " + availableConnections.item(counter).getTextContent());
        }
      }
      
      // **********************************
      // Check all consume shared connections exist.
      for(int counter = 0; counter < referencedConsumeConnections.getLength(); counter ++) {
        if(!this.testReferenceExistsInNodeSets(referencedConsumeConnections.item(counter).getTextContent(), availableConnections)) {
          warningsFound = true;
          warningText.append("\nConsume shared connection does not exist in shared connections: " + referencedConsumeConnections.item(counter).getTextContent());
        }
      }
      
      // **********************************
      // Check all produce shared connections exist.
      for(int counter = 0; counter < referencedProduceConnections.getLength(); counter ++) {
        if(!this.testReferenceExistsInNodeSets(referencedProduceConnections.item(counter).getTextContent(), availableConnections)) {
          warningsFound = true;
          warningText.append("\nProduce shared connection does not exist in shared connections: " + referencedProduceConnections.item(counter).getTextContent());
        }
      }
      
      report.setCheckPassed(!warningsFound);
      if(warningsFound)
        throw new ConfigurationException(warningText.toString());
      
    } catch (Exception ex) {
      report.setFailureException(ex);
    }
    
    return report;
  }
  
  private boolean testReferenceExistsInNodeSets(String searchValue, NodeList listOfNodes) {
    boolean result = false;
    for(int counter = 0; counter < listOfNodes.getLength(); counter ++) {
      if(searchValue.equals(listOfNodes.item(counter).getTextContent())) {
        result = true;
        break;
      }
    }
    
    return result;
  }

  private Document buildXmlDocument(BootstrapProperties bootProperties) throws Exception {
    InputStream configuration = bootProperties.getConfigurationStream();
    
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    DocumentBuilder builder = builderFactory.newDocumentBuilder();
    Document xmlDocument = builder.parse(configuration);
    return xmlDocument;
  }
  
  private NodeList scanForConnections(Document xmlDocument, String xpath) throws XPathExpressionException {
    XPath xPathAvailableConnections = XPathFactory.newInstance().newXPath();
    return (NodeList) xPathAvailableConnections.compile(xpath).evaluate(xmlDocument, XPathConstants.NODESET);
  }
  
  @Override
  public String getFriendlyName() {
    return FRIENDLY_NAME;
  }

  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

}
