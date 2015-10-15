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


package com.adaptris.core.jms;

import com.adaptris.core.BaseCase;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class JmsConnectionErrorHandlerTest extends BaseCase {

  public JmsConnectionErrorHandlerTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {

  }

  @Override
  protected void tearDown() throws Exception {

  }

  public void testJmsAllowedInConjunction() throws Exception {
    JmsConnectionErrorHandler jms1 = new JmsConnectionErrorHandler();
    jms1.registerConnection(new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616")));
    JmsConnectionErrorHandler jms2 = new JmsConnectionErrorHandler();
    jms2.registerConnection(new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61617")));
    assertTrue(jms1.allowedInConjunctionWith(jms2));
  }

  public void testActiveJmsAllowedInConjunction() throws Exception {
    ActiveJmsConnectionErrorHandler jms1 = new ActiveJmsConnectionErrorHandler();
    jms1.registerConnection(new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616")));
    ActiveJmsConnectionErrorHandler jms2 = new ActiveJmsConnectionErrorHandler();
    jms2.registerConnection(new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61617")));
    assertTrue(jms1.allowedInConjunctionWith(jms2));
  }

  public void testRedmine_2303() throws Exception {
    testJmsAllowedInConjunctionWith_NullConnectString();
    testActiveJmsAllowedInConjunctionWith_NullConnectString();
  }

  public void testJmsAllowedInConjunctionWith_NullConnectString() throws Exception {
    JmsConnectionErrorHandler jms1 = new JmsConnectionErrorHandler();
    JmsConnection c = new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616"));
    jms1.registerConnection(c);
    JmsConnectionErrorHandler jms2 = new JmsConnectionErrorHandler();
    jms2.registerConnection(c);
    assertFalse(jms1.allowedInConjunctionWith(jms2));
  }

  public void testActiveJmsAllowedInConjunctionWith_NullConnectString() throws Exception {
    ActiveJmsConnectionErrorHandler jms1 = new ActiveJmsConnectionErrorHandler();
    JmsConnection c = new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616"));
    jms1.registerConnection(c);
    ActiveJmsConnectionErrorHandler jms2 = new ActiveJmsConnectionErrorHandler();
    jms2.registerConnection(c);
    assertFalse(jms1.allowedInConjunctionWith(jms2));
  }

  public void testJmsAllowedInConjunctionWith_MatchingConnectString() throws Exception {
    JmsConnectionErrorHandler jms1 = new JmsConnectionErrorHandler();
    jms1.registerConnection(new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616")));
    JmsConnectionErrorHandler jms2 = new JmsConnectionErrorHandler();
    jms2.registerConnection(new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616")));
    assertFalse(jms1.allowedInConjunctionWith(jms2));
  }

  public void testActiveJmsAllowedInConjunctionWith_MatchingConnectString() throws Exception {
    ActiveJmsConnectionErrorHandler jms1 = new ActiveJmsConnectionErrorHandler();
    jms1.registerConnection(new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616")));
    ActiveJmsConnectionErrorHandler jms2 = new ActiveJmsConnectionErrorHandler();
    jms2.registerConnection(new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616")));
    assertFalse(jms1.allowedInConjunctionWith(jms2));
  }

  public void testComparatorWith2IdenticalConnections() throws Exception {
    JmsConnectionErrorHandler connectionErrorHandler1 = new JmsConnectionErrorHandler();
    JmsConnectionErrorHandler connectionErrorHandler2 = new JmsConnectionErrorHandler();
    
    JmsConnection connection1 = new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616"));
    JmsConnection connection2 = new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616"));
    
    connectionErrorHandler1.registerConnection(connection1);
    connectionErrorHandler2.registerConnection(connection2);
    
    assertFalse(connectionErrorHandler1.allowedInConjunctionWith(connectionErrorHandler2));
  }
  
  public void testComparatorWith2IdenticalJNDIConnections() throws Exception {
    JmsConnectionErrorHandler connectionErrorHandler1 = new JmsConnectionErrorHandler();
    JmsConnectionErrorHandler connectionErrorHandler2 = new JmsConnectionErrorHandler();
    
    JmsConnection connection1 = new JmsConnection();
    JmsConnection connection2 = new JmsConnection();
    
    KeyValuePairSet keyValuePairSet = new KeyValuePairSet();
    
    KeyValuePair keyVPInitial = new KeyValuePair();
    keyVPInitial.setKey("java.naming.factory.initial");
    keyVPInitial.setValue("com.sonicsw.jndi.mfcontext.MFContextFactory");
  
    KeyValuePair keyVPCred = new KeyValuePair();
    keyVPCred.setKey("java.naming.security.credentials");
    keyVPCred.setValue("Administrator");
  
    KeyValuePair keyVPDomain = new KeyValuePair();
    keyVPDomain.setKey("com.sonicsw.jndi.mfcontext.domain");
    keyVPDomain.setValue("Domain1");
  
    KeyValuePair keyVPUrl= new KeyValuePair();
    keyVPUrl.setKey("java.naming.provider.url");
    keyVPUrl.setValue("tcp://localhost:2506");
  
    KeyValuePair keyVPPrinciple = new KeyValuePair();
    keyVPPrinciple.setKey("java.naming.security.principal");
    keyVPPrinciple.setValue("Administrator");
    
    keyValuePairSet.add(keyVPInitial);
    keyValuePairSet.add(keyVPCred);
    keyValuePairSet.add(keyVPCred);
    keyValuePairSet.add(keyVPInitial);
    
    StandardJndiImplementation jndiVendor1 = new StandardJndiImplementation();
    jndiVendor1.setJndiParams(keyValuePairSet);
    
    StandardJndiImplementation jndiVendor2 = new StandardJndiImplementation();
    jndiVendor2.setJndiParams(keyValuePairSet);
    
    connection1.setVendorImplementation(jndiVendor1);
    connection2.setVendorImplementation(jndiVendor2);
    
    connectionErrorHandler1.registerConnection(connection1);
    connectionErrorHandler2.registerConnection(connection2);
    
    assertFalse(connectionErrorHandler1.allowedInConjunctionWith(connectionErrorHandler2));
  }
  
  public void testComparatorWith2DifferemtJNDIConnections() throws Exception {
    JmsConnectionErrorHandler connectionErrorHandler1 = new JmsConnectionErrorHandler();
    JmsConnectionErrorHandler connectionErrorHandler2 = new JmsConnectionErrorHandler();
    
    JmsConnection connection1 = new JmsConnection();
    JmsConnection connection2 = new JmsConnection();
    
    KeyValuePairSet keyValuePairSet1 = new KeyValuePairSet();
    
    KeyValuePair keyVPInitial = new KeyValuePair();
    keyVPInitial.setKey("java.naming.factory.initial");
    keyVPInitial.setValue("com.sonicsw.jndi.mfcontext.MFContextFactory");
  
    KeyValuePair keyVPCred = new KeyValuePair();
    keyVPCred.setKey("java.naming.security.credentials");
    keyVPCred.setValue("Administrator");
  
    KeyValuePair keyVPDomain = new KeyValuePair();
    keyVPDomain.setKey("com.sonicsw.jndi.mfcontext.domain");
    keyVPDomain.setValue("Domain1");
  
    KeyValuePair keyVPUrl= new KeyValuePair();
    keyVPUrl.setKey("java.naming.provider.url");
    keyVPUrl.setValue("tcp://localhost:2506");
  
    KeyValuePair keyVPPrinciple = new KeyValuePair();
    keyVPPrinciple.setKey("java.naming.security.principal");
    keyVPPrinciple.setValue("Administrator");
    
    keyValuePairSet1.add(keyVPInitial);
    keyValuePairSet1.add(keyVPCred);
    keyValuePairSet1.add(keyVPCred);
    keyValuePairSet1.add(keyVPInitial);
    
    KeyValuePairSet keyValuePairSet2 = new KeyValuePairSet(keyValuePairSet1);
    keyValuePairSet2.addKeyValuePair(new KeyValuePair("java.naming.provider.url", "tcp://localhost:2507"));
    
    StandardJndiImplementation jndiVendor1 = new StandardJndiImplementation();
    jndiVendor1.setJndiParams(keyValuePairSet1);
    
    StandardJndiImplementation jndiVendor2 = new StandardJndiImplementation();
    jndiVendor2.setJndiParams(keyValuePairSet2);
    
    connection1.setVendorImplementation(jndiVendor1);
    connection2.setVendorImplementation(jndiVendor2);
    
    connectionErrorHandler1.registerConnection(connection1);
    connectionErrorHandler2.registerConnection(connection2);
    
    assertTrue(connectionErrorHandler1.allowedInConjunctionWith(connectionErrorHandler2));
  }
}
