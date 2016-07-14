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

import static com.adaptris.core.runtime.AdapterComponentMBean.ID_PREFIX;
import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_METRICS_TYPE;

import java.util.ArrayList;
import java.util.Collection;
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

public class MetadataStatistics implements MetadataStatisticsMBean, ChildRuntimeInfoComponent {

  private transient WorkflowManager parent;
  private transient MetadataMetricsInterceptorImpl wrappedComponent;
  private transient ObjectName myObjectName = null;

  protected MetadataStatistics(WorkflowManager owner, MetadataMetricsInterceptorImpl interceptor)
      throws MalformedObjectNameException {
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
  public int getNumberOfTimeSlices() {
    return wrappedComponent.getStats().size();
  }


  @Override
  public int getTimeSliceDurationSeconds() {
    return Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(wrappedComponent.timesliceDurationMs())).intValue();
  }

  private MetadataStatistic get(int index) {
    MetadataStatistic result = null;
    try {
      result = wrappedComponent.getStats().get(index);
    }
    catch (IndexOutOfBoundsException e) {
      result = new MetadataStatistic();
    }
    return result;
  }

  @Override
  public Collection<String> getMetadataKeys(int index) {
    return get(index).getKeys();
  }

  @Override
  public int getTotal(int index, String key) {
    return get(index).getValue(key);
  }

  @Override
  public List<MetadataStatistic> getStatistics() throws CoreException {
    return getStatistics(0, wrappedComponent.getStats().size());
  }

  @Override
  public List<MetadataStatistic> getStatistics(int fromIndex, int toIndex) throws CoreException {
    List<MetadataStatistic> result = new ArrayList<MetadataStatistic>();
    try {
      List<MetadataStatistic> sublist = wrappedComponent.getStats().subList(fromIndex, toIndex);
      for (MetadataStatistic ms : sublist) {
        result.add(ms.clone());
      }
    } catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    return result;
  }
}
