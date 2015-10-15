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

package com.adaptris.core.event;

import com.adaptris.core.AdapterLifecycleEvent;
import com.adaptris.core.EventNameSpaceConstants;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * <code>AdapterLifecycleEvent</code> indicating a <code>Channel</code> restart.
 * </p>
 * 
 * @config channel-restart-event
 */
@XStreamAlias("channel-restart-event")
public class ChannelRestartEvent extends AdapterLifecycleEvent {
  private static final long serialVersionUID = 2014012301L;

  private String channelFriendlyName;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public ChannelRestartEvent() {
    super(EventNameSpaceConstants.CHANNEL_RESTART);
  }

  /**
   * <p>
   * Returns the friendly name of the <code>Channel</code> if one has been configured.
   * </p>
   * 
   * @return the friendly name of the <code>Channel</code> if one has been configured
   */
  public String getChannelFriendlyName() {
    return channelFriendlyName;
  }

  /**
   * <p>
   * Sets the friendly name of the <code>Channel</code> if one has been configured.
   * </p>
   * 
   * @param s the friendly name of the <code>Channel</code> if one has been configured
   */
  public void setChannelFriendlyName(String s) {
    channelFriendlyName = s;
  }
}
