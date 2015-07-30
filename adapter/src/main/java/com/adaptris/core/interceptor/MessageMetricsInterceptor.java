package com.adaptris.core.interceptor;

import static org.apache.commons.lang.StringUtils.isEmpty;

import javax.management.MalformedObjectNameException;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.runtime.ParentRuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponentFactory;
import com.adaptris.core.runtime.WorkflowManager;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * WorkflowInterceptor implementation that exposes metrics via JMX.
 * 
 * <p>
 * This workflow interceptor captures the total number of messages that passed through this workflow, and captures the size of
 * messages entering the workflow (but not the total size of messages exiting the workflow); and also the number of messages that
 * had an error condition at the end of the workflow.
 * </p>
 * 
 * @config message-metrics-interceptor
 * @license BASIC
 */
@XStreamAlias("message-metrics-interceptor")
public class MessageMetricsInterceptor extends MessageMetricsInterceptorImpl {

  static {
    RuntimeInfoComponentFactory.registerComponentFactory(new JmxFactory());
  }

  public MessageMetricsInterceptor() {
    super();
  }

  public MessageMetricsInterceptor(String uid, TimeInterval timesliceDuration) {
    this(uid, timesliceDuration, null);
  }

  public MessageMetricsInterceptor(String uid, TimeInterval timesliceDuration, Integer historyCount) {
    this();
    setUniqueId(uid);
    setTimesliceDuration(timesliceDuration);
    setTimesliceHistoryCount(historyCount);
  }

  @Override
  public synchronized void workflowStart(AdaptrisMessage inputMsg) {

  }

  @Override
  public synchronized void workflowEnd(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
    MessageStatistic currentTimeSlice = getCurrentTimeSlice();
    currentTimeSlice.setTotalMessageCount(currentTimeSlice.getTotalMessageCount() + 1);
    currentTimeSlice.setTotalMessageSize(currentTimeSlice.getTotalMessageSize() + inputMsg.getSize());
    if (!wasSuccessful(inputMsg, outputMsg)) {
      currentTimeSlice.setTotalMessageErrorCount(currentTimeSlice.getTotalMessageErrorCount() + 1);
    }
    updateCurrentTimeSlice(currentTimeSlice);
  }

  public boolean isEnabled(License license) {
    return license.isEnabled(LicenseType.Basic);
  }

  private static class JmxFactory extends RuntimeInfoComponentFactory {

    @Override
    protected boolean isSupported(AdaptrisComponent e) {
      if (e != null && e instanceof MessageMetricsInterceptor) {
        return !isEmpty(((MessageMetricsInterceptor) e).getUniqueId());
      }
      return false;
    }

    @Override
    protected RuntimeInfoComponent createComponent(ParentRuntimeInfoComponent parent, AdaptrisComponent e)
        throws MalformedObjectNameException {
      return new MessageMetricsStatistics((WorkflowManager) parent, (MessageMetricsInterceptor) e);
    }

  }

}
