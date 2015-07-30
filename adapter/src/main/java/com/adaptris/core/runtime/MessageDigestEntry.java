package com.adaptris.core.runtime;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

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
