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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A message statistic stored by a {@link MessageMetricsInterceptorImpl} instances.
 * 
 * @config interceptor-message-statistic
 */
@XStreamAlias("interceptor-message-statistic")
public class MessageStatistic extends InterceptorStatistic implements Externalizable, Cloneable {

  private static final long serialVersionUID = 2015052101L;

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  private int totalMessageCount;
  private long totalMessageSize;
  private int totalMessageErrorCount;

  public MessageStatistic() {
    super();
  }

  public MessageStatistic(long end) {
    this();
    setEndMillis(end);
  }

  public int getTotalMessageCount() {
    return totalMessageCount;
  }

  public void setTotalMessageCount(int messageCount) {
    totalMessageCount = messageCount;
  }

  public long getTotalMessageSize() {
    return totalMessageSize;
  }

  public void setTotalMessageSize(long totalMessageSize) {
    this.totalMessageSize = totalMessageSize;
  }

  public int getTotalMessageErrorCount() {
    return totalMessageErrorCount;
  }

  public void setTotalMessageErrorCount(int totalMessageErrorCount) {
    this.totalMessageErrorCount = totalMessageErrorCount;
  }

  @Override
  public MessageStatistic clone() throws CloneNotSupportedException {
    MessageStatistic result = (MessageStatistic) super.clone();
    result.setEndMillis(getEndMillis());
    result.setTotalMessageCount(getTotalMessageCount());
    result.setTotalMessageErrorCount(getTotalMessageErrorCount());
    result.setTotalMessageSize(getTotalMessageSize());
    result.setStartMillis(getStartMillis());
    return result;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("StartMillis", getStartMillis())
        .append("EndMillis", getEndMillis()).append("TotalMessageCount", getTotalMessageCount())
        .append("TotalMessageErrorCount", getTotalMessageErrorCount())
        .append("TotalMessageSize", getTotalMessageSize()).toString();
  }


  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeLong(getEndMillis());
    out.writeInt(getTotalMessageCount());
    out.writeInt(getTotalMessageErrorCount());
    out.writeLong(getTotalMessageSize());
    out.writeLong(getStartMillis());
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    setEndMillis(in.readLong());
    setTotalMessageCount(in.readInt());
    setTotalMessageErrorCount(in.readInt());
    setTotalMessageSize(in.readLong());
    setStartMillis(in.readLong());
  }

}
