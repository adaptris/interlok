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

import javax.management.MalformedObjectNameException;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.runtime.ParentRuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponentFactory;
import com.adaptris.core.runtime.WorkflowManager;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * WorkflowInterceptor implementation that exposes metrics about metadata values via JMX.
 * <p>
 * Each message that passes through the interceptor will be queried in turn for the configured key ({@link #getMetadataKey()}). The
 * string value associated with that key will be added to the existing total for that value within the given time period.
 * Effectively this interceptor counts the number of times a given metadata value is processed by the workflow within a given time
 * period.
 * </p>
 * 
 * @config metadata-count-interceptor
 * 
 */
@XStreamAlias("metadata-count-interceptor")
@AdapterComponent
@ComponentProfile(summary = "Interceptor that increments a counter based on some metadata value",
    tag = "interceptor")

public class MetadataCountInterceptor extends MetadataMetricsInterceptorImpl {

  static {
    RuntimeInfoComponentFactory.registerComponentFactory(new JmxFactory());
  }

  @NotBlank
  private String metadataKey;

  public MetadataCountInterceptor() {
    super();
  }

  public MetadataCountInterceptor(String key) {
    this();
    setMetadataKey(key);
  }

  @Override
  public void init() throws CoreException {
    if (isEmpty(getMetadataKey())) throw new CoreException("metadata-key is empty/null");
    super.init();
  }

  @Override
  public synchronized void workflowStart(AdaptrisMessage inputMsg) {

  }

  @Override
  public synchronized void workflowEnd(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
    MetadataStatistic currentStat = getCurrentStat();
    // Only work on outputMsg.
    increment(outputMsg, currentStat);
    updateCurrent(currentStat);
  }

  private void increment(AdaptrisMessage msg, MetadataStatistic stat) {
    String value = msg.getMetadataValue(getMetadataKey());
    if (!isEmpty(value)) {
      stat.increment(value);
    }
  }

  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * Set the metadata key whose values you wish to track.
   * 
   * @param key the metadata key.
   */
  public void setMetadataKey(String key) {
    this.metadataKey = key;
  }

  private static class JmxFactory extends RuntimeInfoComponentFactory {

    @Override
    protected boolean isSupported(AdaptrisComponent e) {
      if (e != null && e instanceof MetadataCountInterceptor) {
        return !isEmpty(((MetadataCountInterceptor) e).getUniqueId());
      }
      return false;
    }

    @Override
    protected RuntimeInfoComponent createComponent(ParentRuntimeInfoComponent parent, AdaptrisComponent e)
        throws MalformedObjectNameException {
      return new MetadataStatistics((WorkflowManager) parent, (MetadataCountInterceptor) e);
    }

  }

}
