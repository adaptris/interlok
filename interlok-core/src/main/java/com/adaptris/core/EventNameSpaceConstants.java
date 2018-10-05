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

import com.adaptris.core.event.AdapterCloseEvent;
import com.adaptris.core.event.AdapterInitEvent;
import com.adaptris.core.event.AdapterShutdownEvent;
import com.adaptris.core.event.AdapterStartEvent;
import com.adaptris.core.event.AdapterStopEvent;
import com.adaptris.core.event.LicenseExpiryWarningEvent;

/**
 * <p>
 * All concrete subclasses of <code>Event</code> should define there own 'name
 * space' or hierarchical name, thus allowing interested parties to subscribe
 * for <code>Event</code>s selectively. This class defines these name spaces.
 * </p>
 */
public abstract class EventNameSpaceConstants {

  /**
   * <p>
   * Default name space, inherited by <code>Event</code>s which do not define
   * their own.
   * </p>
   */
  public static final String EVENT = "event";

  /**
   * <p>
   * Name space for {@link AdapterCloseEvent}.
   * </p>
   */
  public static final String ADAPTER_CLOSE = "event.ale.close";

  /**
   * <p>
   * Name space for {@link AdapterInitEvent}.
   * </p>
   */
  public static final String ADAPTER_INIT = "event.ale.init";

  /**
   * <p>
   * Name space for {@link AdapterShutdownEvent}.
   * </p>
   */
  public static final String ADAPTER_SHUTDOWN = "event.ale.shutdown";

  /**
   * Name space for {@link LicenseExpiryWarningEvent}.
   */
  public static final String LICENSE_EXPIRY = "event.ale.license.expiry";

  /**
   * <p>
   * Name space for {@link AdapterStartEvent}.
   * </p>
   */
  public static final String ADAPTER_START = "event.ale.start";

  /**
   * <p>
   * Name space for {@link AdapterStopEvent}.
   * </p>
   */
  public static final String ADAPTER_STOP = "event.ale.stop";

  /**
   * <p>
   * Name space for {@link AdapterStartUpEvent}.
   * </p>
   *
   * @see DefaultAdapterStartUpEvent
   */
  public static final String ADAPTER_START_UP = "event.ale.startup";

  /**
   * <p>
   * Name space for <code>ChannelRestartEvent</code>s.
   * </p>
   */
  public static final String CHANNEL_RESTART = "event.ale.channel.restart";

  /**
   * <p>
   * Name space for {@link HeartbeatEvent}.
   * </p>
   */
  public static final String HEARTBEAT = "event.ale.heartbeat";

  /**
   * <p>
   * Name space for {@link MessageLifecycleEvent}.
   * </p>
   */
  public static final String MESSAGE_LIFECYCLE = "event.mle";

}
