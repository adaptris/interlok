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

import static com.adaptris.core.runtime.AdapterComponentMBean.ID_PREFIX;
import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_RETRY_MONITOR_TYPE;

import java.util.Arrays;
import java.util.Properties;

import javax.management.JMX;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.collections.CollectionUtils;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.JndiContextFactory;
import com.adaptris.core.RetryMessageErrorHandler;
import com.adaptris.core.RetryMessageErrorHandlerMonitorMBean;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.stubs.MockMessageProducer;

public class RetryMessageErrorHandlerMonitorTest extends ComponentManagerCase {

  private Properties env = new Properties();
  private InitialContext initialContext = null;

  public RetryMessageErrorHandlerMonitorTest(String name) {
    super(name);
  }

  public void setUp() throws Exception {
    super.setUp();
    env.put(Context.INITIAL_CONTEXT_FACTORY, JndiContextFactory.class.getName());
    initialContext = new InitialContext(env);
  }


  public void testMBean_FailAllMessages() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    MockMessageProducer failProducer = new MockMessageProducer();
    RetryMessageErrorHandler handler = new RetryMessageErrorHandler(getName(), new StandaloneProducer(failProducer));
    adapter.setMessageErrorHandler(handler);
    AdapterManager adapterManager = new AdapterManager(adapter);
    try {
      registerMBeans(adapterManager);
      adapterManager.requestStart();
      ObjectName retryObjectName = ObjectName
          .getInstance(JMX_RETRY_MONITOR_TYPE + adapterManager.createObjectHierarchyString() + ID_PREFIX + getName());
      RetryMessageErrorHandlerMonitorMBean mbean = JMX.newMBeanProxy(mBeanServer, retryObjectName,
          RetryMessageErrorHandlerMonitorMBean.class);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      // 10 retries 10 minutes apart, so it should be waiting.
      handler.handleProcessingException(msg);
      assertEquals(1, mbean.waitingForRetry().size());
      assertTrue(mbean.waitingForRetry().contains(msg.getUniqueId()));
      mbean.failAllMessages();
      assertEquals(1, failProducer.messageCount());
    }
    finally {
      adapterManager.requestClose();
    }
  }

  public void testMBean_FailMessageById() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    MockMessageProducer failProducer = new MockMessageProducer();
    RetryMessageErrorHandler handler = new RetryMessageErrorHandler(getName(), new StandaloneProducer(failProducer));
    adapter.setMessageErrorHandler(handler);
    AdapterManager adapterManager = new AdapterManager(adapter);
    try {
      registerMBeans(adapterManager);
      adapterManager.requestStart();
      ObjectName retryObjectName = ObjectName
          .getInstance(JMX_RETRY_MONITOR_TYPE + adapterManager.createObjectHierarchyString() + ID_PREFIX + getName());
      RetryMessageErrorHandlerMonitorMBean mbean = JMX.newMBeanProxy(mBeanServer, retryObjectName,
          RetryMessageErrorHandlerMonitorMBean.class);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      // 10 retries 10 minutes apart, so it should be waiting.
      handler.handleProcessingException(msg);
      assertEquals(1, mbean.waitingForRetry().size());
      assertTrue(mbean.waitingForRetry().contains(msg.getUniqueId()));
      mbean.failMessage("hello");
      mbean.failMessage(msg.getUniqueId());
      assertEquals(1, failProducer.messageCount());
    }
    finally {
      adapterManager.requestClose();
    }
  }

  private void registerMBeans(AdapterManager mgr) throws Exception {
    register(CollectionUtils.union(Arrays.asList(new BaseComponentMBean[]
    {
        mgr
    }), mgr.getAllDescendants()));
  }
}
