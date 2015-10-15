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

package com.adaptris.core.services.xml;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.common.Execution;
import com.adaptris.core.common.MetadataDataInputParameter;
import com.adaptris.core.common.MetadataDataOutputParameter;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

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
    message.setContent(sampleXml, message.getContentEncoding());
    
    MetadataDataOutputParameter metadataDataDestination1 = new MetadataDataOutputParameter("targetMetadataKey");
    
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter("//some/random/xml/node1/text()");
    
    Execution execution = new Execution(constantDataDestination, metadataDataDestination1);
    
    List<Execution> executions = new ArrayList<>();
    executions.add(execution);
    
    service.setSourceXmlDestination(new StringPayloadDataInputParameter());
    service.setExecutions(executions);
    execute(service, message);
    
    assertEquals("value1", message.getMetadataValue("targetMetadataKey"));
  }
  
  public void testForCoveragePurposesInvalidXml() throws Exception {
    message.setContent("not valid xml!", message.getContentEncoding());
    
    MetadataDataOutputParameter metadataDataDestination1 = new MetadataDataOutputParameter("targetMetadataKey");
    
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter("//some/random/xml/node1/text()");
    
    Execution execution = new Execution(constantDataDestination, metadataDataDestination1);

    
    List<Execution> executions = new ArrayList<>();
    executions.add(execution);
    
    service.setSourceXmlDestination(new StringPayloadDataInputParameter());
    service.setExecutions(executions);
    try {
      execute(service, message);
    } catch (ServiceException ex) {
      // expected
    }
  }
  
  public void testPayloadSimpleValueXPathIntoPayload() throws Exception {
    message.setContent(sampleXml, message.getContentEncoding());
    message.addMetadata("sourceXpathMetadataKey", "//some/random/xml/node1/text()");
    
    MetadataDataInputParameter metadataDataDestination1 = new MetadataDataInputParameter("sourceXpathMetadataKey");
    
    Execution execution = new Execution(metadataDataDestination1, new StringPayloadDataOutputParameter());
    
    List<Execution> executions = new ArrayList<>();
    executions.add(execution);
    
    service.setSourceXmlDestination(new StringPayloadDataInputParameter());
    service.setExecutions(executions);
    execute(service, message);
    
    assertEquals("value1", message.getContent());
  }
  
  public void testPayloadComplexValueXPathIntoMetadata() throws Exception {
    message.setContent(sampleXml, message.getContentEncoding());
    
    MetadataDataOutputParameter metadataDataDestination1 = new MetadataDataOutputParameter("targetMetadataKey");
    
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter("//some/random/xml/node1");
    
    Execution execution = new Execution(constantDataDestination, metadataDataDestination1);
    
    List<Execution> executions = new ArrayList<>();
    executions.add(execution);
    
    service.setSourceXmlDestination(new StringPayloadDataInputParameter());
    service.setExecutions(executions);
    execute(service, message);
    
    assertEquals("<node1>value1</node1>", message.getMetadataValue("targetMetadataKey"));
  }
  
  public void testPayloadSimpleValueXPathIntoMultipleMetadataExecutions() throws Exception {
    message.setContent(sampleXml, message.getContentEncoding());
    
    MetadataDataOutputParameter metadataDataDestination1 = new MetadataDataOutputParameter("targetMetadataKey1");
    ConstantDataInputParameter constantDataDestination1 = new ConstantDataInputParameter("//some/random/xml/node1/text()");
    
    MetadataDataOutputParameter metadataDataDestination2 = new MetadataDataOutputParameter("targetMetadataKey2");
    ConstantDataInputParameter constantDataDestination2 = new ConstantDataInputParameter("//some/random/xml/node2/text()");
    
    MetadataDataOutputParameter metadataDataDestination3 = new MetadataDataOutputParameter("targetMetadataKey3");
    ConstantDataInputParameter constantDataDestination3 = new ConstantDataInputParameter("//some/random/xml/node3/text()");
    
    Execution execution = new Execution(constantDataDestination1, metadataDataDestination1);
    
    Execution execution2 = new Execution(constantDataDestination2, metadataDataDestination2);
    
    Execution execution3 = new Execution(constantDataDestination3, metadataDataDestination3);
    
    List<Execution> executions = new ArrayList<>();
    executions.add(execution);
    executions.add(execution2);
    executions.add(execution3);
    
    service.setSourceXmlDestination(new StringPayloadDataInputParameter());
    service.setExecutions(executions);
    execute(service, message);
    
    assertEquals("value1", message.getMetadataValue("targetMetadataKey1"));
    assertEquals("value2", message.getMetadataValue("targetMetadataKey2"));
    assertEquals("value3", message.getMetadataValue("targetMetadataKey3"));
  }
  
  public void testPayloadSimpleValueXPathIntoPayloadWithNamespace() throws Exception {
    message.setContent(sampleXmlWithInternalNamespaces, message.getContentEncoding());
    message.addMetadata("sourceXpathMetadataKey", "//some/random/xml/node1/text()");
    
    MetadataDataInputParameter metadataDataDestination1 = new MetadataDataInputParameter("sourceXpathMetadataKey");
    
    Execution execution = new Execution(metadataDataDestination1, new StringPayloadDataOutputParameter());
    
    List<Execution> executions = new ArrayList<>();
    executions.add(execution);
    
    service.setSourceXmlDestination(new StringPayloadDataInputParameter());
    service.setExecutions(executions);
    execute(service, message);
    
    assertEquals("value1", message.getContent());
  }
  
  public void testPayloadSimpleValueXPathIntoPayloadWithHeaderNamespaces() throws Exception {
    message.setContent(sampleXmlWithHeaderNamespaces, message.getContentEncoding());
    message.addMetadata("sourceXpathMetadataKey", "//some:some/random/xml/n1:node1/text()");
    
    MetadataDataInputParameter metadataDataDestination1 = new MetadataDataInputParameter("sourceXpathMetadataKey");
    
    Execution execution = new Execution(metadataDataDestination1, new StringPayloadDataOutputParameter());
    
    List<Execution> executions = new ArrayList<>();
    executions.add(execution);
    
    // Add the namespace mappings
    KeyValuePairSet namespaceMappings = new KeyValuePairSet();
    namespaceMappings.addKeyValuePair(new KeyValuePair("some", "http://adaptris.com/xml/some"));
    namespaceMappings.addKeyValuePair(new KeyValuePair("n1", "http://adaptris.com/xml/n1"));
    namespaceMappings.addKeyValuePair(new KeyValuePair("n2", "http://adaptris.com/xml/n2"));
    namespaceMappings.addKeyValuePair(new KeyValuePair("n3", "http://adaptris.com/xml/n3"));
    
    service.setNamespaceContext(namespaceMappings);
    service.setSourceXmlDestination(new StringPayloadDataInputParameter());
    service.setExecutions(executions);
    execute(service, message);
    
    assertEquals("value1", message.getContent());
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    MetadataDataOutputParameter metadataDataDestination1 = new MetadataDataOutputParameter("targetMetadataKey");
    
    MetadataDataInputParameter metadataDataDestination2 = new MetadataDataInputParameter("sourceXpathMetadataKey");
    
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter("//my/xpath/expression");
    constantDataDestination.setValue("//my/xpath/expression");
    
    Execution execution1 = new Execution();
    execution1.setSource(constantDataDestination);
    execution1.setTarget(metadataDataDestination1);
    
    Execution execution2 = new Execution();
    execution2.setSource(metadataDataDestination2);
    execution2.setTarget(new MetadataDataOutputParameter("targetMetadataKey2"));

    List<Execution> executions = new ArrayList<Execution>();
    executions.add(execution1);
    executions.add(execution2);
    
    KeyValuePairSet namespaceMappings = new KeyValuePairSet();
    namespaceMappings.addKeyValuePair(new KeyValuePair("some", "http://adaptris.com/xml/some"));
    namespaceMappings.addKeyValuePair(new KeyValuePair("n1", "http://adaptris.com/xml/n1"));
    namespaceMappings.addKeyValuePair(new KeyValuePair("n2", "http://adaptris.com/xml/n2"));
    namespaceMappings.addKeyValuePair(new KeyValuePair("n3", "http://adaptris.com/xml/n3"));
    
    service.setNamespaceContext(namespaceMappings);
    
    service.setSourceXmlDestination(new StringPayloadDataInputParameter());
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
  
  private String sampleXmlWithInternalNamespaces = ""
      + "<some xmlns:some=\"http://adaptris.com/xml/some\">"
        + "<random>"
          + "<xml>"
            + "<node1 xmlns:n1=\"http://adaptris.com/xml/n1\">value1</node1>"
            + "<node2 xmlns:n1=\"http://adaptris.com/xml/n2\">value2</node2>"
            + "<node3 xmlns:n1=\"http://adaptris.com/xml/n3\">value3</node3>"
          + "</xml>"
        + "</random>"
      + "</some>";
  
  private String sampleXmlWithHeaderNamespaces = ""
      + "<some:some xmlns:some=\"http://adaptris.com/xml/some\""
                    + " xmlns:n1=\"http://adaptris.com/xml/n1\""
                    + " xmlns:n2=\"http://adaptris.com/xml/n2\""
                    + " xmlns:n3=\"http://adaptris.com/xml/n3\">"
        + "<random>"
          + "<xml>"
            + "<n1:node1>value1</n1:node1>"
            + "<n2:node2>value2</n2:node2>"
            + "<n3:node3>value3</n3:node3>"
          + "</xml>"
        + "</random>"
      + "</some:some>";

}
