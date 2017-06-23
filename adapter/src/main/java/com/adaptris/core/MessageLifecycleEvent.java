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

package com.adaptris.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * <p>
 * Container for message lifecycle events (<code>MleMarker</code>s)
 * for implementations of <code>AdaptrisMessage</code>.
 * </p>
 * <p>
 * In the adapter configuration file this class is aliased as <b>message-lifecycle-event</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 */
@XStreamAlias("message-lifecycle-event")
public class MessageLifecycleEvent extends Event implements Cloneable, Serializable {

  private static final long serialVersionUID = 2013110501L;

  private String workflowId;
  private String channelId;
  private String messageUniqueId;
  @XStreamImplicit(itemFieldName = "mle-marker")
  private List<MleMarker> mleMarkers;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public MessageLifecycleEvent() {
    super(EventNameSpaceConstants.MESSAGE_LIFECYCLE);
    mleMarkers = new ArrayList<MleMarker>();
  }

  /**
   * <p>
   * Returns the immutable name space of this <code>Event</code>.  Over-rides
   * implementation in <code>Event</code> and adds <code>.fail</code> or
   * <code>.success</code> to the end of the original name space, based on
   * calling <code>getWasSuccessful</code> on all <code>MleMarker</code>s.
   * </p>
   * @return the namespace of this <code>Event</code>
   */
  @Override
  public String getNameSpace() {
    StringBuffer result = new StringBuffer(super.getNameSpace());

    if (containsFails()) {
      result.append(".fail");
    }
    else {
      result.append(".success");
    }

    return result.toString();
  }

  /**
   * <p>
   * Returns true if any of this <code>MessageLifecycleEvent</code>s
   * <code>MleMarkers</code> were not successful.
   * </p>
   */
  private boolean containsFails() {
    boolean result = false;
    for (MleMarker marker : mleMarkers) {
      if (!marker.getWasSuccessful()) {
        result = true;
        break;
      }
    }
    return result;
  }

  /**
   * <p>
   * Adds a <code>MleMarker</code> to the end of the ordered internal store.
   * </p>
   * @param event the <code>MleMarker</code> to add, may not be null
   */
  public void addMleMarker(MleMarker event) {
    if (event == null) {
      throw new IllegalArgumentException("param is null");
    }
    mleMarkers.add(event);
  }

  /**
   * <p>
   * Returns the <code>List</code> of <code>MleMarker</code>s.
   * </p>
   *
   * @return the <code>List</code> of <code>MleMarker</code>s
   */
  public List<MleMarker> getMleMarkers() {
    return mleMarkers;
  }

  /**
   * <p>
   * Sets the <code>List</code> of <code>MleMarker</code>s.
   * </p>
   *
   * @param l the <code>List</code> of <code>MleMarker</code>s
   */
  public void setMleMarkers(List<MleMarker> l) {
    mleMarkers = l;
  }

  /**
   * <p>
   * Sets the unique ID of the message to which these <code>MleMarker</code>s
   * are related.
   * </p>
   * @param uniqueId the unique ID of the message to which these
   * <code>MleMarker</code>s are related
   */
  public void setMessageUniqueId(String uniqueId) {
    if (uniqueId == null) {
      throw new IllegalArgumentException("param is null");
    }
    messageUniqueId = uniqueId;
  }

  /**
   * <p>
   * Returns the unique ID of the message to which these <code>MleMarker</code>s
   * are related.
   * </p>
   * @return the unique ID of the message to which these <code>MleMarker</code>s
   * are related.
   */
  public String getMessageUniqueId() {
    return messageUniqueId;
  }

  /**
   * Get the id of the channel that started processing this message.
   * @return the channel id.
   */
  public String getChannelId() {
    return channelId;
  }

  /**
   * Set the id of the channel that is processing this message.
   * @param s the channel id
   */
  public void setChannelId(String s) {
    channelId = s;
  }

  /**
   * Set the id of workflow that is processing this message.
   * @param s the workflow id.
   */
  public void setWorkflowId(String s) {
    workflowId = s;
  }

  /**
   * Get the id of the workflow that is processing this message.
   * @return the workflow id.
   */
  public String getWorkflowId() {
    return workflowId;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", getMessageUniqueId())
        .append("channelID", getChannelId())
        .append("workflowID", getWorkflowId())
        .append("messageEvents", getMleMarkers()).toString();
  }

  @Override
  public MessageLifecycleEvent clone() throws CloneNotSupportedException {
    MessageLifecycleEvent copy = (MessageLifecycleEvent) super.clone();
    List<MleMarker> mleMarkerCopy = new ArrayList<MleMarker>();

    for (MleMarker m : mleMarkers) {
      mleMarkerCopy.add((MleMarker) m.clone());
    }
    copy.mleMarkers = mleMarkerCopy;
    return copy;
  }
}
