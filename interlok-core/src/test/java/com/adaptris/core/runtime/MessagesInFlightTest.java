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

package com.adaptris.core.runtime;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.concurrent.TimeUnit;
import javax.management.JMX;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;
import com.adaptris.core.PoolingWorkflow;
import com.adaptris.core.interceptor.InFlightWorkflowInterceptor;
import com.adaptris.core.interceptor.MessageInFlightMBean;
import com.adaptris.core.services.WaitService;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class MessagesInFlightTest extends ComponentManagerCase {

  protected transient Log logR = LogFactory.getLog(this.getClass());

  public MessagesInFlightTest() {
  }


  @Test
  public void testInFlightMessageCount() throws Exception {
    PoolingWorkflow workflow = new PoolingWorkflow(getName());
    Adapter adapter = buildAdapter(workflow);
    AdapterManager adapterManager = new AdapterManager(adapter);
    try {
      adapterManager.registerMBean();
      ObjectName objName = createInFlightObjectName(adapterManager);
      MessageInFlightMBean mbean = JMX.newMBeanProxy(mBeanServer, objName,
          MessageInFlightMBean.class);
      assertNotNull(mbean);
      assertEquals(0, mbean.messagesInFlightCount());
      adapterManager.requestStart();
      workflow.onAdaptrisMessage(AdaptrisMessageFactory.getDefaultInstance().newMessage());
      LifecycleHelper.waitQuietly(100L);
      assertEquals(1, mbean.messagesInFlightCount());
      assertTrue(mbean.messagesInFlight());
    } finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testPendingMessageCount() throws Exception {
    final PoolingWorkflow workflow = new PoolingWorkflow(getName());
    Adapter adapter = buildAdapter(workflow);
    AdapterManager adapterManager = new AdapterManager(adapter);
    try {
      adapterManager.registerMBean();
      ObjectName objName = createInFlightObjectName(adapterManager);
      MessageInFlightMBean mbean = JMX.newMBeanProxy(mBeanServer, objName, MessageInFlightMBean.class);
      assertNotNull(mbean);
      assertEquals(0, mbean.messagesInFlightCount());
      adapterManager.requestStart();
      workflow.onAdaptrisMessage(AdaptrisMessageFactory.getDefaultInstance().newMessage());
      new Thread(new Runnable() {

        @Override
        public void run() {
          workflow.onAdaptrisMessage(AdaptrisMessageFactory.getDefaultInstance().newMessage());
        }

      }).start();
      LifecycleHelper.waitQuietly(200L);
      assertEquals(1, mbean.messagesInFlightCount());
      assertTrue(mbean.messagesInFlight());
      assertEquals(1, mbean.messagesPendingCount());
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  private Adapter buildAdapter(PoolingWorkflow workflow) throws Exception {
    Adapter adapter = createAdapter(getName());
    Channel channel = new Channel(getName());
    workflow.setPoolSize(1);
    workflow.setShutdownWaitTime(new TimeInterval(1L, TimeUnit.SECONDS));
    workflow.addInterceptor(new InFlightWorkflowInterceptor(getName()));
    workflow.getServiceCollection().add(new WaitService(new TimeInterval(5L, TimeUnit.SECONDS)));
    channel.getWorkflowList().add(workflow);
    adapter.getChannelList().add(channel);
    return adapter;
  }

  private ObjectName createInFlightObjectName(AdapterManager parent) throws MalformedObjectNameException {
    ObjectName result = null;
    for (BaseComponentMBean bean : parent.getAllDescendants()) {
      if (bean instanceof MessageInFlightMBean) {
        result = bean.createObjectName();
      }
    }
    if (result == null) {
      throw new RuntimeException();
    }
    return result;
  }
}
