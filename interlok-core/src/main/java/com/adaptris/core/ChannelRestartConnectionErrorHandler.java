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

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static com.adaptris.core.util.LoggingHelper.friendlyName;

/**
 * {@link ConnectionErrorHandler} which restarts the channel when there is a problem.
 * 
 * @config channel-restart-error-handler
 */
@XStreamAlias("channel-restart-error-handler")
public class ChannelRestartConnectionErrorHandler extends ConnectionErrorHandlerImp {

  protected transient LocalDateTime lastRestartDateTime;
  protected transient Duration _durationBetweenRestarts = Duration.ofSeconds(60);
  protected final Timer channelAvailableTimer = new Timer();
  protected transient TimerTask channelAvailableTimerTask;
  private String durationBetweenRestarts = _durationBetweenRestarts.toString();

  protected ConnectionErrorHandler delegate;

  public ConnectionErrorHandler getDelegate() {
        return delegate;
    }

  public void setDelegate(ConnectionErrorHandler delegate) {
        this.delegate = delegate;
    }

  @Override
  public void handleConnectionException() {
    toggleChannelAvailability(false);
    try {
        if (delegate != null) {
            delegate.handleConnectionException();
        }
    } finally {
        log.info("{}:: Restarting affected channels", getClass().getSimpleName());
        restartAffectedComponents();
    }
  }

    @Override
    protected Set<StateManagedComponent> filter(Set<StateManagedComponent> list) {
      // we should always try to restart components regardless of
      return list;
    }

    public LocalDateTime getLastRestartDateTime() {
        return lastRestartDateTime;
    }

    public void setLastRestartDateTime(LocalDateTime lastRestartDateTime) {
        this.lastRestartDateTime = lastRestartDateTime;
    }

    public String getDurationBetweenRestarts() {
      return durationBetweenRestarts;
  }

  public void setDurationBetweenRestarts(String durationBetweenRestarts) {
      setDurationBetweenRestarts(Duration.parse(durationBetweenRestarts));
      this.durationBetweenRestarts = durationBetweenRestarts;
  }

  protected void setDurationBetweenRestarts(Duration durationBetweenRestarts) {
      this._durationBetweenRestarts = durationBetweenRestarts;
  }

  public Duration durationBetweenRestarts() {
      if (durationBetweenRestarts != null && _durationBetweenRestarts == null) setDurationBetweenRestarts(durationBetweenRestarts);
      return _durationBetweenRestarts;
  }

    @Override
    public void init() throws CoreException {
        if (delegate != null) delegate.init();
    }

    @Override
    public void start() throws CoreException {
        if (delegate != null) delegate.start();
    }

    @Override
    public void stop() {
        if (delegate != null) delegate.stop();
    }

    @Override
    public void close() {
      if (delegate != null) delegate.close();
    }

  public void reset() {
      lastRestartDateTime = null;
  }

  protected void toggleChannelAvailability(boolean available) {
      AdaptrisConnection connection = retrieveConnection(AdaptrisConnection.class);
      Set<StateManagedComponent> list = connection.retrieveExceptionListeners();
      for (StateManagedComponent c : list) {
          if (c instanceof Channel) {
              ((Channel) c).toggleAvailability(available);
          }
      }
  }

}
