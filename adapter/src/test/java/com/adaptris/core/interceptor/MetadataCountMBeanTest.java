package com.adaptris.core.interceptor;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.management.JMX;
import javax.management.ObjectName;

import com.adaptris.core.Adapter;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.SerializableAdaptrisMessage;
import com.adaptris.core.runtime.BaseComponentMBean;
import com.adaptris.core.runtime.WorkflowManagerMBean;

@SuppressWarnings("deprecation")
public class MetadataCountMBeanTest extends MetadataStatisticsMBeanCase {

  private static final String COUNTER_1 = "counter1";
  private static final String COUNTER_2 = "counter2";
  public MetadataCountMBeanTest(String name) {
    super(name);
  }

  public void testGetMetadataTotals() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      start(adapter);
      register(mBeans);
      ObjectName workflowObj = createWorkflowObjectName(adapterName);
      ObjectName metricsObj = createMetricsObjectName(adapterName);
      MetadataStatisticsMBean stats = JMX.newMBeanProxy(mBeanServer, metricsObj, MetadataStatisticsMBean.class);
      WorkflowManagerMBean workflow = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      SerializableAdaptrisMessage msg = createMessageForInjection(null);
      workflow.injectMessage(msg);
      assertEquals(10, stats.getTotal(0, COUNTER_1));
      assertEquals(10, stats.getTotal(0, COUNTER_2));
      assertEquals(0, stats.getTotal(0, "blah"));
      assertEquals(0, stats.getTotal(5, "blah"));
    }
    finally {
      stop(adapter);
    }
  }

  public void testGetMetadataKeys() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      start(adapter);
      register(mBeans);
      ObjectName workflowObj = createWorkflowObjectName(adapterName);
      ObjectName metricsObj = createMetricsObjectName(adapterName);
      MetadataStatisticsMBean stats = JMX.newMBeanProxy(mBeanServer, metricsObj, MetadataStatisticsMBean.class);
      WorkflowManagerMBean workflow = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      SerializableAdaptrisMessage msg = createMessageForInjection(null);
      workflow.injectMessage(msg);
      assertEquals(2, stats.getMetadataKeys(0).size());
      assertEquals(0, stats.getMetadataKeys(1).size());
      assertEquals(new HashSet(Arrays.asList(new String[]
      {
          COUNTER_1, COUNTER_2
      })), new HashSet(stats.getMetadataKeys(0)));
    }
    finally {
      stop(adapter);
    }
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
      workflow.injectMessage(msg);

      assertEquals(1, stats.getStatistics().size());
      assertEquals(interceptor.getStats().size(), stats.getStatistics().size());

      MetadataStatistic interceptorStat = interceptor.getStats().get(0);
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
