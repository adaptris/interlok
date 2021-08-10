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

import com.adaptris.annotation.Removal;
import com.adaptris.validation.constraints.ConfigDeprecated;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link ProduceExceptionHandler} which attempts to restart the parent {@link com.adaptris.core.Channel} of the {@code Workflow}
 * that had the failure.
 * 
 * @config channel-restart-produce-exception-handler
 *
 * @deprecated since 4.2.0
 */
@Deprecated(since = "4.2.0")
@ConfigDeprecated(message = "If you need restarting capability wrap your producer into a standalone-producer and set restart services on failure.", removalVersion = "5.0.0", groups = Deprecated.class)
@Removal(message = "If you need restarting capability wrap your producer into a standalone-producer and set restart services on failure.", version = "5.0.0")
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
    else { // something else is rebooting the Channel...
      // do nothing?
      log.debug("Channel is not available, returning...");

    }
  }
}
