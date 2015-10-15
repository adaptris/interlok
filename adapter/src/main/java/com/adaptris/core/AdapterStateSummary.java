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

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Summary of the state of an {@link Adapter} and associated {@link Channel}s
 * </p>
 * 
 * @config adapter-state-summary
 */
@XStreamAlias("adapter-state-summary")
public class AdapterStateSummary {

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

  private String lastStartTime;
  private String lastStopTime;
  private KeyValuePair adapterState; // bespoke SMCState class?
  private KeyValuePairSet channelStates;
  // Dummy for backwards compatibility with v2 events.
  // Do not remove.
  private String lastInitialised;

  public AdapterStateSummary() {
    channelStates = new KeyValuePairSet();
    adapterState = new KeyValuePair();
  }

  /**
   * <p>
   * Utility constructor that summarises the current state of the passed <code>Adapter</code>.
   * </p>
   * 
   * @param adapter the <code>Adapter</code> to report the state of
   */
  public AdapterStateSummary(Adapter adapter) {
    this();
    this.setAdapterState(adapter.getUniqueId(), adapter.retrieveComponentState());
    setLastStartTime(adapter.lastStartTime());
    setLastStopTime(adapter.lastStopTime());
    for (Channel channel : adapter.getChannelList()) {
      this.addChannelState(channel.getUniqueId(), channel.retrieveComponentState());
    }
  }

  /**
   * <p>
   * Sets the state of the <code>Adapter</code>.
   * </p>
   * 
   * @param uniqueId the unique ID, may not be null or empty
   * @param state the state, may not be null or empty
   */
  public void setAdapterState(String uniqueId, ComponentState state) {
    if (state == null) {
      throw new IllegalArgumentException("Null component state");
    }
    // ignore non-uniquely identified i.e. newly created Adapters
    if (uniqueId != null && !"".equals(uniqueId)) {
      this.setAdapterState(new KeyValuePair(uniqueId, state.getClass().getName()));
    }
  }

  /**
   * <p>
   * Sets the state of the <code>Adapter</code>. NB <code>KeyValuePair</code> does not allow nulls.
   * </p>
   * 
   * @param state a <code>KeyValuePair</code> of state against unique ID
   */
  public void setAdapterState(KeyValuePair state) {
    if (state == null) {
      throw new IllegalArgumentException("null param");
    }
    if (isEmpty(state.getKey()) || isEmpty(state.getValue())) {
      throw new IllegalArgumentException("null ID or state");
    }

    adapterState = state;
  }

  /**
   * <p>
   * Returns the state of the <code>Adapter</code>.
   * </p>
   * 
   * @return the state of the <code>Adapter</code>
   */
  public KeyValuePair getAdapterState() {
    return adapterState;
  }

  /**
   * <p>
   * Adds the state of a <code>Channel</code> to the internal store. If state for the passed unique ID has already been stored it
   * will be over-written. Non-uniquely identified <code>Channel</code>s are ignored.
   * </p>
   * 
   * @param uniqueId the unique ID of the <code>Channel</code>
   * @param state the state may not be null
   */
  public void addChannelState(String uniqueId, ComponentState state) {
    if (state == null) {
      throw new IllegalArgumentException("Null component state");
    }
    // ignore non-uniquely identified Channels
    if (!isEmpty(uniqueId)) {
      this.addChannelState(new KeyValuePair(uniqueId, state.getClass().getName()));
    }
  }

  /**
   * <p>
   * Adds the state of a <code>Channel</code> to the internal store. NB <code>KeyValuePair</code> does not allow nulls.
   * </p>
   * 
   * @param state the state of the <code>Channel</code> to add
   */
  public void addChannelState(KeyValuePair state) {
    if (state == null) {
      throw new IllegalArgumentException("null param");
    }
    // ignore non-uniquely identified Channel
    if (!isEmpty(state.getKey())) {
      if (isEmpty(state.getValue())) {
        throw new IllegalArgumentException("empty state");
      }
      channelStates.addKeyValuePair(state);
    }
  }

  public KeyValuePairSet getChannelStates() {
    return channelStates;
  }

  public void setChannelStates(KeyValuePairSet states) {
    channelStates = states;
  }

  public String getLastStartTime() {
    return lastStartTime;
  }

  public void setLastStartTime(String lastStartTime) {
    this.lastStartTime = lastStartTime;
  }

  public String getLastStopTime() {
    return lastStopTime;
  }

  public void setLastStopTime(String lastStopTime) {
    this.lastStopTime = lastStopTime;
  }

  private void setLastStartTime(Date d) {
    if (d != null) {
      setLastStartTime(DATE_FORMAT.format(d));
    }
  }

  private void setLastStopTime(Date d) {
    if (d != null) {
      setLastStopTime(DATE_FORMAT.format(d));
    }
  }

}
