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
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.runtime.ParentRuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponentFactory;
import com.adaptris.core.runtime.WorkflowManager;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * WorkflowInterceptor implementation that exposes metrics about integer metadata values via JMX.
 * <p>
 * Each message that passes through the interceptor will be queried in turn for each configured key ({@link #getMetadataKeys()}).
 * The integer value associated with that key will be added to the existing total for that key within the given time period.
 * </p>
 * <p>
 * The general use-case is when you capture a count of some description (e.g. number of lines), store it against metadata, and to
 * keep a running total of that number for a given time interval. The counts are exposed via {@link MetadataStatisticsMBean} for
 * capture and display. Note that if the metadata keys contain non-numeric values, then results are undefined (at the very least you
 * will get {@link NumberFormatException} being thrown).
 * </p>
 * 
 * @config metadata-totals-interceptor
 * 
 */
@XStreamAlias("metadata-totals-interceptor")
@AdapterComponent
@ComponentProfile(summary = "Interceptor that adds the metadata value to an existing counter",
    tag = "interceptor")

public class MetadataTotalsInterceptor extends MetadataMetricsInterceptorImpl {

  static {
    RuntimeInfoComponentFactory.registerComponentFactory(new JmxFactory());
  }

  @NotNull
  @XStreamImplicit(itemFieldName = "metadata-key")
  private List<String> metadataKeys;

  public MetadataTotalsInterceptor() {
    super();
    metadataKeys = new ArrayList<String>();
  }

  public MetadataTotalsInterceptor(List<String> keys) {
    this();
    setMetadataKeys(keys);
  }

  @Override
  public synchronized void workflowStart(AdaptrisMessage inputMsg) {

  }

  @Override
  public synchronized void workflowEnd(AdaptrisMessage inputMsg, final AdaptrisMessage outputMsg) {
    update(new StatisticsDelta<MetadataStatistic>() {
      @Override
      public MetadataStatistic apply(MetadataStatistic currentStat) {
        increment(outputMsg, currentStat);
        return currentStat;
      }
    });
  }

  private void increment(AdaptrisMessage msg, MetadataStatistic stat) {
    for (String key : getMetadataKeys()) {
      stat.increment(key, getValue(key, msg));
    }
  }

  private int getValue(String key, AdaptrisMessage msg) {
    if (!msg.headersContainsKey(key)) {
      return 0;
    }
    return Integer.valueOf(msg.getMetadataValue(key)).intValue();
  }

  public List<String> getMetadataKeys() {
    return metadataKeys;
  }

  /**
   * Set the list of metadata keys to track.
   * 
   * @param l the metadata keys.
   */
  public void setMetadataKeys(List<String> l) {
    this.metadataKeys = l;
  }

  private static class JmxFactory extends RuntimeInfoComponentFactory {

    @Override
    protected boolean isSupported(AdaptrisComponent e) {
      if (e != null && e instanceof MetadataTotalsInterceptor) {
        return !isEmpty(((MetadataTotalsInterceptor) e).getUniqueId());
      }
      return false;
    }

    @Override
    protected RuntimeInfoComponent createComponent(ParentRuntimeInfoComponent parent, AdaptrisComponent e)
        throws MalformedObjectNameException {
      return new MetadataStatistics((WorkflowManager) parent, (MetadataTotalsInterceptor) e);
    }

  }

}
