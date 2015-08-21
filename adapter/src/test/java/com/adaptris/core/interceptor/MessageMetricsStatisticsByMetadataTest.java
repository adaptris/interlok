package com.adaptris.core.interceptor;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.List;

import javax.management.JMX;
import javax.management.ObjectName;

import com.adaptris.core.Adapter;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.SerializableAdaptrisMessage;
import com.adaptris.core.runtime.BaseComponentMBean;
import com.adaptris.interlok.management.MessageProcessor;

public class MessageMetricsStatisticsByMetadataTest extends MessageMetricsStatisticsTest {

  public MessageMetricsStatisticsByMetadataTest(String name) {
    super(name);
  }

  @Override
  public void testGetMessageStatistics() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    String payload = "SomePayload";
    MessageMetricsInterceptorByMetadata interceptor = (MessageMetricsInterceptorByMetadata) createInterceptor();
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
      assertEquals(interceptor.getCacheArray().size(), stats.getStatistics().size());
      MessageStatistic interceptorStat = interceptor.getCacheArray().get(0);
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

  protected SerializableAdaptrisMessage createMessageForInjection(String payload) {
    SerializableAdaptrisMessage msg = null;
    if (!isEmpty(payload)) {
      msg =  new SerializableAdaptrisMessage(GUID.getUUID(), payload);
    }
    else {
      msg = new SerializableAdaptrisMessage(GUID.getUUID());
    }
    msg.addMetadata(new MetadataElement("messageType", "Order"));
    return msg;
  }

  @Override
  protected MessageMetricsInterceptorByMetadata createInterceptor() {
    return new MessageMetricsInterceptorByMetadata(new MetadataElement("messageType", "Ord.*"));
  }
}
