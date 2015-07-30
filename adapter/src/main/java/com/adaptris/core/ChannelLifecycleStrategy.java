package com.adaptris.core;

import java.util.List;

/**
 * Strategy for handling channel lifecycle within a {@link ChannelList}.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public interface ChannelLifecycleStrategy {

  /**
   * Start a list of channels.
   * 
   * @param channelList a list of channels that have already been initialised.
   * @throws CoreException wrapping any underlying exception.
   */
  void start(List<Channel> channelList) throws CoreException;

  /**
   * Initialise a list of channels.
   * 
   * @param channels a list of channels that require initialising.
   * @throws CoreException wrapping any underlying exception.
   */
  void init(List<Channel> channels) throws CoreException;

  /**
   * Stop a list of channels.
   * 
   * @param channels a list of channels that have previously been started.
   */
  void stop(List<Channel> channels);

  /**
   * Close a list of channels.
   * 
   * @param channels a list of channels.
   */
  void close(List<Channel> channels);
}
