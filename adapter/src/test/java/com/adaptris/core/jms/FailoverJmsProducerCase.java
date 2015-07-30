/*
 * $RCSfile: FailoverJmsProducerCase.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/03/25 11:56:41 $
 * $Author: lchan $
 */
package com.adaptris.core.jms;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.Channel;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.util.LifecycleHelper;

public abstract class FailoverJmsProducerCase extends JmsProducerCase {

  private FailoverJmsConnection connection;

  public FailoverJmsProducerCase(String name) {
    super(name);
  }

  public void testBug1012() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    FailoverJmsConnection connection = new FailoverJmsConnection();
    try {
      broker.start();
      List<JmsConnection> ptp = new ArrayList<JmsConnection>();
      ptp.add(broker.getJmsConnection());
      ptp.add(broker.getJmsConnection(new BasicActiveMqImplementation(), true));
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
      ptp.add(broker.getJmsConnection());
      ptp.add(broker.getJmsConnection(new BasicActiveMqImplementation(), true));
      connection.setConnections(ptp);
      LifecycleHelper.init(connection);
      //setting the consume connection no longer sets up the exception handler, so expect 0 here.
      assertEquals(0, connection.currentJmsConnection().retrieveExceptionListeners().size());
      LifecycleHelper.close(connection);
    }
    finally {
      broker.destroy();
    }
  }

}
