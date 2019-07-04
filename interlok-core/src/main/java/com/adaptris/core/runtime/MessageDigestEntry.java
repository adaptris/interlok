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

package com.adaptris.core.runtime;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class MessageDigestEntry implements Serializable {

  private static final long serialVersionUID = 201211231143L;

  private String uniqueId;
  private String workflowId;
  private Date date;

  public MessageDigestEntry() {
    this(null, null);
  }

  public MessageDigestEntry(String uniqueId, String workflowId) {
    this(uniqueId, workflowId, new Date());
  }

  public MessageDigestEntry(String uniqueId, String workflowId, Date date) {
    setUniqueId(uniqueId);
    setWorkflowId(workflowId);
    setDate(date);
  }

  public String getUniqueId() {
    return uniqueId;
  }

  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  public String getWorkflowId() {
    return workflowId;
  }

  public void setWorkflowId(String workflowId) {
    this.workflowId = workflowId;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date dateRaised) {
    date = dateRaised;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (o instanceof MessageDigestEntry) {
      MessageDigestEntry rhs = (MessageDigestEntry) o;
      return new EqualsBuilder().append(getUniqueId(), rhs.getUniqueId()).isEquals();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(11, 17).append(getUniqueId()).toHashCode();
  }
}
