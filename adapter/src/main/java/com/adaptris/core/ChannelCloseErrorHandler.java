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
