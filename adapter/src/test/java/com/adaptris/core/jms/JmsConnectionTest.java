package com.adaptris.core.jms;

import static org.junit.Assert.assertNotSame;

import javax.jms.Session;

import org.junit.Test;

import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.util.LifecycleHelper;

public class JmsConnectionTest {

  @Test
  public void testSession() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    try {
      broker.start();
      JmsConnection conn = broker.getJmsConnection();
      LifecycleHelper.init(conn);
      LifecycleHelper.start(conn);
      Session s1 = conn.createSession(false, AcknowledgeMode.Mode.AUTO_ACKNOWLEDGE.acknowledgeMode());
      Session s2 = conn.createSession(false, AcknowledgeMode.Mode.AUTO_ACKNOWLEDGE.acknowledgeMode());
      assertNotSame(s1, s2);
    }
    finally {
      broker.destroy();
    }
  }
}
