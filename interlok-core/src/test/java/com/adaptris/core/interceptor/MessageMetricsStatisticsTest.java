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

package com.adaptris.core.interceptor;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import java.util.List;
import javax.management.JMX;
import javax.management.ObjectName;
import org.junit.Test;
import com.adaptris.core.Adapter;
import com.adaptris.core.SerializableAdaptrisMessage;
import com.adaptris.core.runtime.BaseComponentMBean;
import com.adaptris.core.runtime.WorkflowManagerMBean;
import com.adaptris.interlok.management.MessageProcessor;

public class MessageMetricsStatisticsTest extends StatisticsMBeanCase {

  @Test
  public void testNoCachesExists() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      start(adapter);
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      ObjectName metricsObj = createMetricsObjectName(adapterName);
      MessageMetricsStatisticsMBean metrics = JMX.newMBeanProxy(mBeanServer, metricsObj, MessageMetricsStatisticsMBean.class);
      assertEquals(0, metrics.getNumberOfTimeSlices());
    }
    finally {
      stop(adapter);
    }
  }

  @Test
  public void testGetMessageStatistics() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    String payload = "SomePayload";
    MessageMetricsInterceptor interceptor = (MessageMetricsInterceptor) createInterceptor();
    interceptor.setUniqueId(getName());
    Adapter adapter = createSingleChannelAdapter(adapterName, interceptor);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      start(adapter);
      register(mBeans);
      ObjectName workflowObj = createWorkflowObjectName(adapterName);
      ObjectName metricsObj = createMetricsObjectName(adapterName, getName());
      MessageMetricsStatisticsMBean stats = JMX.newMBeanProxy(mBeanServer, metricsObj, MessageMetricsStatisticsMBean.class);
      MessageProcessor workflow = JMX.newMBeanProxy(mBeanServer, workflowObj, MessageProcessor.class);
      workflow.processAsync(createMessageForInjection(payload));
      
      assertEquals(1, stats.getStatistics().size());
      assertEquals(interceptor.getStats().size(), stats.getStatistics().size());
      MessageStatistic interceptorStat = (MessageStatistic) interceptor.getStats().get(0);
      MessageStatistic mbeanStat = stats.getStatistics().get(0);

      assertNotSame(interceptorStat, mbeanStat); // They're not objectively equals (not clones), and no equals method.

      assertEquals(interceptorStat.getEndMillis(), mbeanStat.getEndMillis());
      assertEquals(interceptorStat.getTotalMessageCount(), mbeanStat.getTotalMessageCount());
      assertEquals(interceptorStat.getTotalMessageErrorCount(), mbeanStat.getTotalMessageErrorCount());
      assertEquals(interceptorStat.getTotalMessageSize(), mbeanStat.getTotalMessageSize());
    }
    finally {
      stop(adapter);
    }
  }

  @Test
  public void testGetTotalStringStats() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      start(adapter);
      register(mBeans);
      ObjectName workflowObj = createWorkflowObjectName(adapterName);
      ObjectName metricsObj = createMetricsObjectName(adapterName);
      MessageMetricsStatisticsMBean stats = JMX.newMBeanProxy(mBeanServer, metricsObj, MessageMetricsStatisticsMBean.class);
      WorkflowManagerMBean workflow = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      assertNotNull(stats.getTotalStringStats());
      SerializableAdaptrisMessage msg = createMessageForInjection(null);
      workflow.processAsync(msg);
      assertNotNull(stats.getTotalStringStats());
    }
    finally {
      stop(adapter);
    }
  }


  @Override
  protected SerializableAdaptrisMessage createMessageForInjection(String payload) {
    if (!isEmpty(payload)) {
      return new SerializableAdaptrisMessage(GUID.getUUID(), payload);
    }
    return new SerializableAdaptrisMessage(GUID.getUUID());
  }

  @Override
  protected WorkflowInterceptorImpl createInterceptor() {
    return new MessageMetricsInterceptor();
  }
}
