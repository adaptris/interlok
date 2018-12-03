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

import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_METRICS_TYPE;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.CoreException;
import com.adaptris.core.runtime.ChildRuntimeInfoComponentImpl;
import com.adaptris.core.runtime.ParentRuntimeInfoComponent;
import com.adaptris.core.runtime.WorkflowManager;
import com.adaptris.core.util.ExceptionHelper;

public class MessageMetricsStatistics extends ChildRuntimeInfoComponentImpl
    implements MessageMetricsStatisticsMBean {

  private transient WorkflowManager parent;
  private transient MessageMetricsInterceptorImpl wrappedComponent;

  private MessageMetricsStatistics() {
    super();
  }

  protected MessageMetricsStatistics(WorkflowManager owner, MessageMetricsInterceptorImpl interceptor) {
    parent = owner;
    wrappedComponent = interceptor;
  }

  @Override
  protected String getType() {
    return JMX_METRICS_TYPE;
  }

  @Override
  protected String uniqueId() {
    return wrappedComponent.getUniqueId();
  }


  @Override
  public int getNumberOfTimeSlices() {
    return wrappedComponent.getStats().size();
  }


  @Override
  public int getTimeSliceDurationSeconds() {
    return Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(wrappedComponent.timesliceDurationMs())).intValue();
  }

  @Override
  public String getTotalStringStats() {
    long timeinMillis = Calendar.getInstance().getTimeInMillis();
    StringBuffer buffer = new StringBuffer();
    buffer.append(wrappedComponent.getStats().size());
    buffer.append(" time slice(s) have been recorded.\n");

    if (wrappedComponent.getStats().size() > 0) {
      MessageStatistic timeSlice = (MessageStatistic) wrappedComponent.getStats().get(wrappedComponent.getStats().size() - 1);
      buffer.append("Current time slice statistics; \n");
      buffer.append("    Time Slice ends in  - " + (timeSlice.getEndMillis() - timeinMillis) / 1000 + " seconds.\n");
      buffer.append("    Message count - " + timeSlice.getTotalMessageCount() + "\n");
      buffer.append("    Message total size - " + timeSlice.getTotalMessageSize());
    }

    return buffer.toString();
  }
  
  @Override
  public ParentRuntimeInfoComponent getParentRuntimeInfoComponent() {
    return parent;
  }

  @Override
  public List<MessageStatistic> getStatistics() throws CoreException {
    return getStatistics(0, wrappedComponent.getStats().size());
  }

  @Override
  public List<MessageStatistic> getStatistics(int fromIndex, int toIndex) throws CoreException {
    List<MessageStatistic> result = new ArrayList<MessageStatistic>();
    try {
      List<InterceptorStatistic> sublist = wrappedComponent.getStats().subList(fromIndex, toIndex);
      for (InterceptorStatistic ms : sublist) {
        result.add(((MessageStatistic) ms).clone());
      }
    } catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    return result;
  }

  @Override
  public void clearStatistics() throws CoreException {
    wrappedComponent.clearStatistics();
  }
}
