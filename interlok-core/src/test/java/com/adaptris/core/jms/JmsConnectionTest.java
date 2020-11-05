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

import static org.junit.Assert.assertNotSame;
import javax.jms.Session;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.util.LifecycleHelper;

public class JmsConnectionTest {
  @Rule
  public TestName testName = new TestName();

  private static EmbeddedActiveMq activeMqBroker;

  @BeforeClass
  public static void setUpAll() throws Exception {
    activeMqBroker = new EmbeddedActiveMq();
    activeMqBroker.start();
  }
  
  @AfterClass
  public static void tearDownAll() throws Exception {
    if(activeMqBroker != null)
      activeMqBroker.destroy();
  }
  
  @Test
  public void testSession() throws Exception {
    JmsConnection conn = activeMqBroker.getJmsConnection();
    try {
      LifecycleHelper.init(conn);
      LifecycleHelper.start(conn);
      Session s1 = conn.createSession(false, AcknowledgeMode.Mode.AUTO_ACKNOWLEDGE.acknowledgeMode());
      Session s2 = conn.createSession(false, AcknowledgeMode.Mode.AUTO_ACKNOWLEDGE.acknowledgeMode());
      assertNotSame(s1, s2);
    }
    finally {
      LifecycleHelper.close(conn);
    }
  }

  @Test
  public void testCloneForTesting() throws Exception {
    JmsConnection conn = new JmsConnection();
    conn.setClientId(testName.getMethodName());
    JmsConnection copy = conn.cloneForTesting();
    assertNotSame(conn.getClientId(), copy.getClientId());
  }
}
