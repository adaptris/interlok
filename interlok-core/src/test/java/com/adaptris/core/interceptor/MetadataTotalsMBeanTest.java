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

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.List;

import javax.management.JMX;
import javax.management.ObjectName;

import com.adaptris.core.Adapter;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.SerializableAdaptrisMessage;
import com.adaptris.core.runtime.BaseComponentMBean;
import com.adaptris.core.runtime.WorkflowManagerMBean;

public class MetadataTotalsMBeanTest extends MetadataStatisticsMBeanCase {

  private static final String STATS_KEY = "SomeValue";
  private static final String METADATA_KEY1 = "key1";
  private static final String METADATA_KEY2 = "key2";

  public MetadataTotalsMBeanTest(String name) {
    super(name);
  }

  public void testGetMetadataStatistics() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    MetadataCountInterceptor interceptor = createInterceptor();
    interceptor.setUniqueId(getName());
    Adapter adapter = createSingleChannelAdapter(adapterName, interceptor);

    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      start(adapter);
      register(mBeans);
      ObjectName workflowObj = createWorkflowObjectName(adapterName);
      ObjectName metricsObj = createMetricsObjectName(adapterName, getName());
      MetadataStatisticsMBean stats = JMX.newMBeanProxy(mBeanServer, metricsObj, MetadataStatisticsMBean.class);
      WorkflowManagerMBean workflow = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      SerializableAdaptrisMessage msg = createMessageForInjection(null);
      workflow.processAsync(msg);

      assertEquals(1, stats.getStatistics().size());
      assertEquals(interceptor.getStats().size(), stats.getStatistics().size());

      MetadataStatistic interceptorStat = (MetadataStatistic) interceptor.getStats().get(0);
      MetadataStatistic mbeanStat = stats.getStatistics().get(0);

      assertNotSame(interceptorStat, mbeanStat); // They're not objectively equals (not clones), and no equals method.
      assertEquals(interceptorStat.getEndMillis(), mbeanStat.getEndMillis());
      assertEquals(interceptorStat.getMetadataStatistics(), mbeanStat.getMetadataStatistics());
    }
    finally {
      stop(adapter);
    }
  }

  protected SerializableAdaptrisMessage createMessageForInjection(String payload) {
    SerializableAdaptrisMessage msg = null;
    if (!isEmpty(payload)) {
      msg = new SerializableAdaptrisMessage(GUID.getUUID(), payload);
    }
    else {
      msg = new SerializableAdaptrisMessage(GUID.getUUID());
    }
    msg.addMetadata(new MetadataElement(METADATA_KEY1, STATS_KEY));
    return msg;
  }

  @Override
  protected MetadataCountInterceptor createInterceptor() {
    return new MetadataCountInterceptor(METADATA_KEY1);
  }

}
