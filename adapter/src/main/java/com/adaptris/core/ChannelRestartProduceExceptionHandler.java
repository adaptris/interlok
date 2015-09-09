package com.adaptris.core;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link ProduceExceptionHandler} which attempts to restart the parent {@link Channel} of the {@code Workflow}
 * that had the failure.
 * 
 * 
 * @config channel-restart-produce-exception-handler
 */
@XStreamAlias("channel-restart-produce-exception-handler")
public class ChannelRestartProduceExceptionHandler extends ProduceExceptionHandlerImp {

  /**
   * @see com.adaptris.core.ProduceExceptionHandler
   *      #handle(com.adaptris.core.Workflow)
   */
  public void handle(Workflow workflow) {

    // obtain Channel lock while still holding W/f lock in onAM...
    // LewinChan - This appears to be dodgy - See Bug:870
    // So we synchronize after checking the channel availability.
    // synchronized (workflow.obtainChannel()) {
    if (workflow.obtainChannel().isAvailable()) {
      synchronized (workflow.obtainChannel()) {
        workflow.obtainChannel().toggleAvailability(false);
        super.restart(workflow.obtainChannel());
      }
    }
    else { // sthg else is rebooting the Channel...
      // do nothing?
      log.debug("Channel is not available, returning...");

    }
  }
}
