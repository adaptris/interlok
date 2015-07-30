package com.adaptris.core;

import static com.adaptris.core.util.LoggingHelper.friendlyName;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ConnectionErrorHandler} which shutdowns the channel where there is a problem.
 * 
 * @config channel-close-error-handler
 */
@XStreamAlias("channel-close-error-handler")
public class ChannelCloseErrorHandler extends ConnectionErrorHandlerImp {

  @Override
  public void handleConnectionException() {
    log.info(getClass().getSimpleName() + ":: Closing affected channels");
    List<Channel> channels = getRegisteredChannels();
    for (Channel c : channels) {
      String loggingId = friendlyName(c);
      log.info("Closing affected component : [" + loggingId + "]");
      try {
        c.toggleAvailability(false);
        c.requestClose();
      }
      catch (Throwable e) {
        log.trace("Failed to close component cleanly, logging exception for informational purposes only", e);
      }
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
