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
