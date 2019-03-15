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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A metadata statistic stored by concrete implementations {@link MetadataMetricsInterceptorImpl}
 * 
 * @config interceptor-metadata-statistic
 */
@XStreamAlias("interceptor-metadata-statistic")
public class MetadataStatistic extends InterceptorStatistic implements Externalizable, Cloneable {

  private static final long serialVersionUID = 2015052101L;

  private Properties metadataStatistics;

  public MetadataStatistic() {
    super();
    setMetadataStatistics(new Properties());
  }

  public MetadataStatistic(long end) {
    this();
    setEndMillis(end);
  }

  Properties getMetadataStatistics() {
    return metadataStatistics;
  }

  void setMetadataStatistics(Properties stats) {
    this.metadataStatistics = stats;
  }

  /**
   * Get the metadata keys captured by this statistic.
   * 
   * @return the keys.
   */
  public Collection<String> getKeys() {
    return new ArrayList<String>(getMetadataStatistics().stringPropertyNames());
  }

  /**
   * Get the value associated with the key.
   * 
   * @param key the key.
   * @return the value associated with key, 0 if the key does not exist.
   */
  public int getValue(String key) {
    if (!getMetadataStatistics().containsKey(key)) {
      return 0;
    }
    return Integer.parseInt(getMetadataStatistics().getProperty(key));
  }

  public void putValue(String key, int value) {
    getMetadataStatistics().setProperty(key, String.valueOf(value));
  }

  /**
   * Convenience method to increment the key by 1.
   * 
   * @param key the key.
   * @see #increment(String, int)
   */
  public void increment(String key) {
    increment(key, 1);
  }

  /**
   * Convenience method to increment a given key.
   * 
   * @param key the key
   * @param increment how much to increment by
   */
  public void increment(String key, int increment) {
    if (!getMetadataStatistics().containsKey(key)) {
      putValue(key, increment);
    } else {
      int current = Integer.parseInt(getMetadataStatistics().getProperty(key));
      current += increment;
      putValue(key, current);
    }
  }

  @Override
  public MetadataStatistic clone() throws CloneNotSupportedException {
    MetadataStatistic result = (MetadataStatistic) super.clone();
    result.setEndMillis(getEndMillis());
    result.setMetadataStatistics((Properties) getMetadataStatistics().clone());
    return result;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("StartMillis", getStartMillis())
        .append("EndMillis", getEndMillis()).append("MetadataStatistics", getMetadataStatistics())
        .toString();
  }



  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeLong(getEndMillis());
    out.writeObject(getMetadataStatistics());
    out.writeLong(getStartMillis());
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    setEndMillis(in.readLong());
    setMetadataStatistics((Properties) in.readObject());
    setStartMillis(in.readLong());
  }


}
