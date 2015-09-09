package com.adaptris.core;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>ConnectionErrorHandler</code> for use with polling consumers where you do not want an Exception thrown
 * back to run to re-init the Channel.
 * </p>
 * 
 * @config null-connection-error-handler
 */
@XStreamAlias("null-connection-error-handler")
public class NullConnectionErrorHandler extends ConnectionErrorHandlerImp {

  @Override
  public void handleConnectionException() {
    List<Channel> channels = getRegisteredChannels();
    for (Channel c : channels) {
      c.toggleAvailability(true);
    }
  }

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public void close() {
  }

}
