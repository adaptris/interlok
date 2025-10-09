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

import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static com.adaptris.core.util.LoggingHelper.friendlyName;

/**
 * {@link ConnectionErrorHandler} which restarts the channel when there is a problem.
 * 
 * @config channel-restart-error-handler
 */
@XStreamAlias("channel-restart-error-handler")
public class ChannelRestartErrorHandler extends ConnectionErrorHandlerImp {

  protected transient LocalDateTime lastConnectionExceptionDateTime;
  Duration durationBetweenRestarts = Duration.ofMinutes(60);
  @Override
  public void handleConnectionException() {

    LocalDateTime now = LocalDateTime.now();
    if (lastConnectionExceptionDateTime == null || now.isAfter(lastConnectionExceptionDateTime.plus(durationBetweenRestarts))) {
      log.info("{}:: Restarting affected channels", getClass().getSimpleName());
      lastConnectionExceptionDateTime = now;
      restartAffectedComponents();
    } else {
        toggleChannelAvailability(false);
    }
  }

  public Duration getDurationBetweenRestarts() {
      return durationBetweenRestarts;
  }

  public void setDurationBetweenRestarts(Duration durationBetweenRestarts) {
      this.durationBetweenRestarts = durationBetweenRestarts;
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
      lastConnectionExceptionDateTime = null;
  }

  protected void toggleChannelAvailability(boolean available) {
      AdaptrisConnection connection = retrieveConnection(AdaptrisConnection.class);
      Set<StateManagedComponent> listeners = connection.retrieveExceptionListeners();
      Set<StateManagedComponent> list = filter(listeners);
      for (StateManagedComponent c : list) {
          if (c instanceof Channel) {
              ((Channel) c).toggleAvailability(available);
          }
      }
  }

}
