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
package com.adaptris.core.http.jetty;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.adaptris.core.BaseCase;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.LifecycleHelper;

public class JettyHashUserRealmVerifierTest {

  @Rule
  public TestName testName = new TestName();

  private Properties config;
  
  @Before
  public void setUp() throws Exception {
    config = BaseCase.PROPERTIES;
  }

  @After
  public void tearDown() throws Exception {

  }
  
  @Test
  public void testTryInit() throws Exception {
    JettyHashUserRealmVerifier verify = new JettyHashUserRealmVerifier();
    assertNull(verify.getFilename());
    try {
      LifecycleHelper.init(verify);
      fail();
    }
    catch (CoreException expected) {

    }
    finally {
      LifecycleHelper.close(verify);
      ;
    }
    verify.setFilename(config.getProperty(HttpConsumerTest.JETTY_USER_REALM));
    try {
      LifecycleHelper.init(verify);

    }
    finally {
      LifecycleHelper.close(verify);
      ;
    }
  }

  @Test
  public void testValidate_Basic() throws Exception {
    JettyHashUserRealmVerifier verify = new JettyHashUserRealmVerifier(config.getProperty(HttpConsumerTest.JETTY_USER_REALM));
    Map<String, Object> identityMap = new HashMap<>();
    identityMap.put(JettyHashUserRealmVerifier.KEY_USERNAME, "user");
    identityMap.put(JettyHashUserRealmVerifier.KEY_PASSWORD, "password");
    identityMap.put(JettyHashUserRealmVerifier.KEY_ROLE, "user");
    try {
      BaseCase.start(verify);
      assertTrue(verify.validate(identityMap));
    }
    finally {
      BaseCase.stop(verify);
    }
  }

  @Test
  public void testValidate_NoRole() throws Exception {
    JettyHashUserRealmVerifier verify = new JettyHashUserRealmVerifier(config.getProperty(HttpConsumerTest.JETTY_USER_REALM));
    Map<String, Object> identityMap = new HashMap<>();
    identityMap.put(JettyHashUserRealmVerifier.KEY_USERNAME, "user");
    identityMap.put(JettyHashUserRealmVerifier.KEY_PASSWORD, "password");
    try {
      BaseCase.start(verify);
      assertFalse(verify.validate(identityMap));
    }
    finally {
      BaseCase.stop(verify);
    }
  }

  @Test
  public void testValidate_EmptyRoles() throws Exception {
    JettyHashUserRealmVerifier verify = new JettyHashUserRealmVerifier(config.getProperty(HttpConsumerTest.JETTY_USER_REALM));
    Map<String, Object> identityMap = new HashMap<>();
    identityMap.put(JettyHashUserRealmVerifier.KEY_USERNAME, "plain");
    identityMap.put(JettyHashUserRealmVerifier.KEY_PASSWORD, "plain");
    try {
      BaseCase.start(verify);
      assertTrue(verify.validate(identityMap));
      identityMap.put(JettyHashUserRealmVerifier.KEY_ROLE, "plain");
      assertTrue(verify.validate(identityMap));
    }
    finally {
      BaseCase.stop(verify);
    }
  }

  @Test
  public void testValidate_BlankPassword() throws Exception {
    JettyHashUserRealmVerifier verify = new JettyHashUserRealmVerifier(config.getProperty(HttpConsumerTest.JETTY_USER_REALM));
    Map<String, Object> identityMap = new HashMap<>();
    identityMap.put(JettyHashUserRealmVerifier.KEY_USERNAME, "blank");
    identityMap.put(JettyHashUserRealmVerifier.KEY_PASSWORD, "");
    try {
      BaseCase.start(verify);
      assertFalse(verify.validate(identityMap));
      identityMap.put(JettyHashUserRealmVerifier.KEY_ROLE, "justarole");
      assertTrue(verify.validate(identityMap));
    }
    finally {
      BaseCase.stop(verify);
    }
  }

  @Test
  public void testValidate_BadPassword() throws Exception {
    JettyHashUserRealmVerifier verify = new JettyHashUserRealmVerifier(config.getProperty(HttpConsumerTest.JETTY_USER_REALM));
    Map<String, Object> identityMap = new HashMap<>();
    identityMap.put(JettyHashUserRealmVerifier.KEY_USERNAME, "user");
    identityMap.put(JettyHashUserRealmVerifier.KEY_PASSWORD, testName.getMethodName());
    try {
      BaseCase.start(verify);
      assertFalse(verify.validate(identityMap));
    }
    finally {
      BaseCase.stop(verify);
    }
  }

  @Test
  public void testValidate_BadUser() throws Exception {
    JettyHashUserRealmVerifier verify = new JettyHashUserRealmVerifier(config.getProperty(HttpConsumerTest.JETTY_USER_REALM));
    Map<String, Object> identityMap = new HashMap<>();
    identityMap.put(JettyHashUserRealmVerifier.KEY_USERNAME, testName.getMethodName());
    identityMap.put(JettyHashUserRealmVerifier.KEY_PASSWORD, testName.getMethodName());
    try {
      BaseCase.start(verify);
      assertFalse(verify.validate(identityMap));
    }
    finally {
      BaseCase.stop(verify);
    }
  }
}
