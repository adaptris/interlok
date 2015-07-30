package com.adaptris.core.interceptor;

import static com.adaptris.core.runtime.AdapterComponentMBean.ID_PREFIX;
import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_METRICS_TYPE;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.adaptris.core.CoreException;
import com.adaptris.core.runtime.ChildRuntimeInfoComponent;
import com.adaptris.core.runtime.ParentRuntimeInfoComponent;
import com.adaptris.core.runtime.WorkflowManager;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JmxHelper;

public class MessageMetricsStatistics implements MessageMetricsStatisticsMBean, ChildRuntimeInfoComponent {

  private transient WorkflowManager parent;
  private transient MessageMetricsInterceptorImpl wrappedComponent;
  private transient ObjectName myObjectName = null;

  protected MessageMetricsStatistics(WorkflowManager owner, MessageMetricsInterceptorImpl interceptor) throws MalformedObjectNameException {
    parent = owner;
    wrappedComponent = interceptor;
    initMembers();
  }

  private void initMembers() throws MalformedObjectNameException {
    // Builds up a name com.adaptris:type=Metrics,adapter=<adapter-id,>,channel=<channel-id>,workflow=<workflow-id>,id=myid
    myObjectName = ObjectName.getInstance(JMX_METRICS_TYPE + parent.createObjectHierarchyString() + ID_PREFIX
        + wrappedComponent.getUniqueId());
  }

  @Override
  public int getNumberOfTimeSlices() {
    return wrappedComponent.getCacheArray().size();
  }

  @Override
  public int getNumberOfMessagesForTimeSliceIndex(int index) {
    try {
      MessageStatistic timeSlice = wrappedComponent.getCacheArray().get(index);
      return timeSlice.getTotalMessageCount();
    }
    catch (IndexOutOfBoundsException ex) {
      return 0;
    }
  }

  @Override
  public int getTimeSliceDurationSeconds() {
    return Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(wrappedComponent.timesliceDurationMs())).intValue();
  }

  @Override
  public long getTotalSizeOfMessagesForTimeSliceIndex(int index) {
    try {
      MessageStatistic timeSlice = wrappedComponent.getCacheArray().get(index);
      return timeSlice.getTotalMessageSize();
    }
    catch (IndexOutOfBoundsException ex) {
      return 0;
    }
  }

  @Override
  public String getTotalStringStats() {
    long timeinMillis = Calendar.getInstance().getTimeInMillis();
    StringBuffer buffer = new StringBuffer();
    buffer.append(wrappedComponent.getCacheArray().size());
    buffer.append(" time slice(s) have been recorded.\n");

    if (wrappedComponent.getCacheArray().size() > 0) {
      MessageStatistic timeSlice = wrappedComponent.getCacheArray().get(wrappedComponent.getCacheArray().size() - 1);
      buffer.append("Current time slice statistics; \n");
      buffer.append("    Time Slice ends in  - " + (timeSlice.getEndMillis() - timeinMillis) / 1000 + " seconds.\n");
      buffer.append("    Message count - " + timeSlice.getTotalMessageCount() + "\n");
      buffer.append("    Message total size - " + timeSlice.getTotalMessageSize());
    }

    return buffer.toString();
  }

  @Override
  public int getNumberOfErrorMessagesForTimeSliceIndex(int index) {
    try {
      MessageStatistic timeSlice = wrappedComponent.getCacheArray().get(index);
      return timeSlice.getTotalMessageErrorCount();
    }
    catch (IndexOutOfBoundsException ex) {
      return 0;
    }
  }
  
  @Override
  public long getEndMillisForTimeSliceIndex(int index) {
    try {
      MessageStatistic timeSlice = wrappedComponent.getCacheArray().get(index);
      return timeSlice.getEndMillis();
    }
    catch (IndexOutOfBoundsException ex) {
      return 0;
    }
  }

  @Override
  public ParentRuntimeInfoComponent getParentRuntimeInfoComponent() {
    return parent;
  }

  @Override
  public ObjectName getParentObjectName() throws MalformedObjectNameException {
    return parent.createObjectName();
  }

  @Override
  public String getParentId() {
    return parent.getUniqueId();
  }

  @Override
  public ObjectName createObjectName() throws MalformedObjectNameException {
    return myObjectName;
  }

  @Override
  public void registerMBean() throws CoreException {
    try {
      JmxHelper.register(createObjectName(), this);
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
  }

  @Override
  public void unregisterMBean() throws CoreException {
    try {
      JmxHelper.unregister(createObjectName());
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
  }

  @Override
  public List<MessageStatistic> getStatistics() throws CoreException {
    return getStatistics(0, wrappedComponent.getCacheArray().size());
  }

  @Override
  public List<MessageStatistic> getStatistics(int fromIndex, int toIndex) throws CoreException {
    List<MessageStatistic> result = new ArrayList<MessageStatistic>();
    try {
      List<MessageStatistic> sublist = wrappedComponent.getCacheArray().subList(fromIndex, toIndex);
      for (MessageStatistic ms : sublist) {
        result.add(ms.clone());
      }
    } catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    return result;
  }
}
