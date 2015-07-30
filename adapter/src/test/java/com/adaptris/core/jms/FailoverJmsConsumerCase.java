package com.adaptris.core.jms;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.Channel;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.util.LifecycleHelper;


public abstract class FailoverJmsConsumerCase extends JmsConsumerCase {

  public FailoverJmsConsumerCase(String name) {
    super(name);
  }


  public void testBug1012() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    FailoverJmsConnection connection = new FailoverJmsConnection();
    try {
      List<JmsConnection> ptp = new ArrayList<JmsConnection>();
      ptp.add(broker.getJmsConnection(new BasicActiveMqImplementation(), true));
      ptp.add(broker.getJmsConnection(new BasicActiveMqImplementation(), false));
      connection.setConnections(ptp);
      LifecycleHelper.init(connection);

      assertEquals(1, connection.currentJmsConnection().retrieveExceptionListeners().size());
      AdaptrisComponent owner = (AdaptrisComponent) connection.currentJmsConnection().retrieveExceptionListeners().toArray()[0];
      assertTrue("Owner should be failover connection", connection == owner);
      LifecycleHelper.close(connection);

      Channel channel = new MockChannel();
      connection = new FailoverJmsConnection();
      connection.setRegisterOwner(true);
      channel.setConsumeConnection(connection);
      ptp = new ArrayList<JmsConnection>();
      ptp.add(broker.getJmsConnection(new BasicActiveMqImplementation(), true));
      ptp.add(broker.getJmsConnection(new BasicActiveMqImplementation(), false));
      connection.setConnections(ptp);
      connection.setConnectionAttempts(1);
      LifecycleHelper.init(connection);
      assertEquals(0, connection.currentJmsConnection().retrieveExceptionListeners().size());
      LifecycleHelper.close(connection);
    }
    finally {
      LifecycleHelper.close(connection);
      broker.destroy();
    }
  }

}
