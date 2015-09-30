package com.adaptris.core.services.xml;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;
import com.adaptris.interlok.config.ConstantDataDestination;
import com.adaptris.interlok.config.MetadataDataDestination;
import com.adaptris.interlok.config.PayloadDataDestination;

public class XPathServiceTest extends ServiceCase {

  private AdaptrisMessage message;
  private XPathService service;
  
  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "XmlServiceExamples.baseDir";

  public XPathServiceTest(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  public void setUp() throws Exception {
    service = new XPathService();
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
  }
  
  public void tearDown() throws Exception {
    
  }
  
  public void testPayloadSimpleValueXPathIntoMetadata() throws Exception {
    message.setStringPayload(sampleXml, message.getCharEncoding());
    
    MetadataDataDestination metadataDataDestination1 = new MetadataDataDestination();
    metadataDataDestination1.setMetadataKey("targetMetadataKey");
    
    ConstantDataDestination constantDataDestination = new ConstantDataDestination();
    constantDataDestination.setValue("//some/random/xml/node1/text()");
    
    Execution execution = new Execution();
    execution.setSourceXpathExpression(constantDataDestination);
    execution.setTargetDataDestination(metadataDataDestination1);
    
    List<Execution> executions = new ArrayList<>();
    executions.add(execution);
    
    service.setSourceXmlDestination(new PayloadDataDestination());
    service.setExecutions(executions);
    service.doService(message);
    
    assertEquals("value1", message.getMetadataValue("targetMetadataKey"));
  }
  
  public void testForCoveragePurposesInvalidXml() throws Exception {
    message.setStringPayload("not valid xml!", message.getCharEncoding());
    
    MetadataDataDestination metadataDataDestination1 = new MetadataDataDestination();
    metadataDataDestination1.setMetadataKey("targetMetadataKey");
    
    ConstantDataDestination constantDataDestination = new ConstantDataDestination();
    constantDataDestination.setValue("//some/random/xml/node1/text()");
    
    Execution execution = new Execution();
    execution.setSourceXpathExpression(constantDataDestination);
    execution.setTargetDataDestination(metadataDataDestination1);
    
    List<Execution> executions = new ArrayList<>();
    executions.add(execution);
    
    service.setSourceXmlDestination(new PayloadDataDestination());
    service.setExecutions(executions);
    try {
      service.doService(message);
    } catch (ServiceException ex) {
      // expected
    }
  }
  
  public void testPayloadSimpleValueXPathIntoPayload() throws Exception {
    message.setStringPayload(sampleXml, message.getCharEncoding());
    message.addMetadata("sourceXpathMetadataKey", "//some/random/xml/node1/text()");
    
    MetadataDataDestination metadataDataDestination1 = new MetadataDataDestination();
    metadataDataDestination1.setMetadataKey("sourceXpathMetadataKey");
    
    Execution execution = new Execution();
    execution.setSourceXpathExpression(metadataDataDestination1);
    execution.setTargetDataDestination(new PayloadDataDestination());
    
    List<Execution> executions = new ArrayList<>();
    executions.add(execution);
    
    service.setSourceXmlDestination(new PayloadDataDestination());
    service.setExecutions(executions);
    service.doService(message);
    
    assertEquals("value1", message.getStringPayload());
  }
  
  public void testPayloadComplexValueXPathIntoMetadata() throws Exception {
    message.setStringPayload(sampleXml, message.getCharEncoding());
    
    MetadataDataDestination metadataDataDestination1 = new MetadataDataDestination();
    metadataDataDestination1.setMetadataKey("targetMetadataKey");
    
    ConstantDataDestination constantDataDestination = new ConstantDataDestination();
    constantDataDestination.setValue("//some/random/xml/node1");
    
    Execution execution = new Execution();
    execution.setSourceXpathExpression(constantDataDestination);
    execution.setTargetDataDestination(metadataDataDestination1);
    
    List<Execution> executions = new ArrayList<>();
    executions.add(execution);
    
    service.setSourceXmlDestination(new PayloadDataDestination());
    service.setExecutions(executions);
    service.doService(message);
    
    assertEquals("<node1>value1</node1>", message.getMetadataValue("targetMetadataKey"));
  }
  
  public void testPayloadSimpleValueXPathIntoMultipleMetadataExecutions() throws Exception {
    message.setStringPayload(sampleXml, message.getCharEncoding());
    
    MetadataDataDestination metadataDataDestination1 = new MetadataDataDestination();
    metadataDataDestination1.setMetadataKey("targetMetadataKey1");
    ConstantDataDestination constantDataDestination1 = new ConstantDataDestination();
    constantDataDestination1.setValue("//some/random/xml/node1/text()");
    
    MetadataDataDestination metadataDataDestination2 = new MetadataDataDestination();
    metadataDataDestination2.setMetadataKey("targetMetadataKey2");
    ConstantDataDestination constantDataDestination2 = new ConstantDataDestination();
    constantDataDestination2.setValue("//some/random/xml/node2/text()");
    
    MetadataDataDestination metadataDataDestination3 = new MetadataDataDestination();
    metadataDataDestination3.setMetadataKey("targetMetadataKey3");
    ConstantDataDestination constantDataDestination3 = new ConstantDataDestination();
    constantDataDestination3.setValue("//some/random/xml/node3/text()");
    
    Execution execution = new Execution();
    execution.setSourceXpathExpression(constantDataDestination1);
    execution.setTargetDataDestination(metadataDataDestination1);
    
    Execution execution2 = new Execution();
    execution2.setSourceXpathExpression(constantDataDestination2);
    execution2.setTargetDataDestination(metadataDataDestination2);
    
    Execution execution3 = new Execution();
    execution3.setSourceXpathExpression(constantDataDestination3);
    execution3.setTargetDataDestination(metadataDataDestination3);
    
    List<Execution> executions = new ArrayList<>();
    executions.add(execution);
    executions.add(execution2);
    executions.add(execution3);
    
    service.setSourceXmlDestination(new PayloadDataDestination());
    service.setExecutions(executions);
    service.doService(message);
    
    assertEquals("value1", message.getMetadataValue("targetMetadataKey1"));
    assertEquals("value2", message.getMetadataValue("targetMetadataKey2"));
    assertEquals("value3", message.getMetadataValue("targetMetadataKey3"));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    MetadataDataDestination metadataDataDestination1 = new MetadataDataDestination();
    metadataDataDestination1.setMetadataKey("targetMetadataKey");
    
    MetadataDataDestination metadataDataDestination2 = new MetadataDataDestination();
    metadataDataDestination2.setMetadataKey("sourceXpathMetadataKey");
    
    ConstantDataDestination constantDataDestination = new ConstantDataDestination();
    constantDataDestination.setValue("//my/xpath/expression");
    
    Execution execution1 = new Execution();
    execution1.setSourceXpathExpression(constantDataDestination);
    execution1.setTargetDataDestination(metadataDataDestination1);
    
    Execution execution2 = new Execution();
    execution2.setSourceXpathExpression(metadataDataDestination2);
    execution2.setTargetDataDestination(new PayloadDataDestination());

    List<Execution> executions = new ArrayList<Execution>();
    executions.add(execution1);
    executions.add(execution2);
    
    service.setSourceXmlDestination(new PayloadDataDestination());
    service.setExecutions(executions);
    
    return service;
  }
  
  private String sampleXml = ""
      + "<some>"
        + "<random>"
          + "<xml>"
            + "<node1>value1</node1>"
            + "<node2>value2</node2>"
            + "<node3>value3</node3>"
          + "</xml>"
        + "</random>"
      + "</some>";

}
