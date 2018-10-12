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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.JMX;
import javax.management.ObjectName;

import com.adaptris.core.Adapter;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.SerializableAdaptrisMessage;
import com.adaptris.core.runtime.BaseComponentMBean;
import com.adaptris.core.runtime.WorkflowManagerMBean;

public class MetadataCountMBeanTest extends MetadataStatisticsMBeanCase {

  private static final String COUNTER_1 = "counter1";
  private static final String COUNTER_2 = "counter2";
  public MetadataCountMBeanTest(String name) {
    super(name);
  }



  public void testGetMetadataStatistics() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    MetadataTotalsInterceptor interceptor = createInterceptor();
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

      MetadataStatistic interceptorStat = (MetadataStatistic)  interceptor.getStats().get(0);
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
    msg.addMetadata(new MetadataElement(COUNTER_1, "10"));
    msg.addMetadata(new MetadataElement(COUNTER_2, "10"));
    return msg;
  }

  @Override
  protected MetadataTotalsInterceptor createInterceptor() {
    return new MetadataTotalsInterceptor(new ArrayList(Arrays.asList(new String[]
    {
        COUNTER_1, COUNTER_2
    })));
  }

}
