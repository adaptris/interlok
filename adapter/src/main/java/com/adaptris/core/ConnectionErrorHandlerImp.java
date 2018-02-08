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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;

/**
 * <p>
 * Behaviour common to <code>ConnectionErrorHandler</code>s.
 * </p>
 */
public abstract class ConnectionErrorHandlerImp implements ConnectionErrorHandler {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private transient AdaptrisConnection adpConnection;

  @Override
  public void registerConnection(AdaptrisConnection connection) {
    adpConnection = Args.notNull(connection, "connection");
  }

  @Override
  public <T> T retrieveConnection(Class<T> type) {
    return (T) adpConnection;
  }

  @Override
  public boolean allowedInConjunctionWith(ConnectionErrorHandler ceh) {
    return true;
  }


  /**
   * Standard functionality to restart the owner of the connection.
   * 
   */
  protected void restartAffectedComponents() {
    AdaptrisConnection connection = retrieveConnection(AdaptrisConnection.class);
    Set<StateManagedComponent> toRestart = filter(connection.retrieveExceptionListeners());
    try {
      tryRestart(toRestart);
    } catch (RuntimeException e) {
      waitQuietly(TimeUnit.SECONDS.toMillis(10L));
      tryRestart(toRestart);
    }
  }

  private void waitQuietly(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
    }
  }

  private void tryRestart(Set<StateManagedComponent> toRestart) {
    AdaptrisConnection connection = retrieveConnection(AdaptrisConnection.class);
    Set<StateManagedComponent> listeners = connection.retrieveExceptionListeners();
    // Close all component first.
    // By stopping all components first, we ensure that the connection isn't continually restarted.
    // As connections might be shared across multiple components.
    stopAndClose(listeners);
    // If the connection is a normal connection and part of a channel, it should already be stopped
    // So StateManagedComp will ignore it.
    // If it's part of a SharedConnnection list, then it won't be stopped, so let's stop it.
    log.trace("Stopping Connection : [{}]", friendlyName(connection));
    LifecycleHelper.stopAndClose(connection);

    // The init here should start the connections, because the channel is in fact a listener still
    // make a new LinkedHashSet out of it, because AU have reported a conncurrent modification exception
    // error during the start phase.
    init(new LinkedHashSet<StateManagedComponent>(toRestart));
    start(new LinkedHashSet<StateManagedComponent>(toRestart));
  }

  private void init(Set<StateManagedComponent> list) {
    for (StateManagedComponent c : list) {
      String loggingId = friendlyName(c);
      try {
        log.trace("Initialising affected component : [{}]", loggingId);
        LifecycleHelper.init(c);
      }
      catch (CoreException e) {
        log.error("Exception initialising component", e);
        log.error("component [{}] cannot be restarted", loggingId);

      }
    }
  }

  private void start(Set<StateManagedComponent> list) {
    for (StateManagedComponent c : list) {
      String loggingId = friendlyName(c);
      try {
        log.trace("Starting affected component : [{}]", loggingId);
        LifecycleHelper.start(c);
        if (c instanceof Channel) {
          ((Channel) c).toggleAvailability(true);
        }
        log.info("Component restart complete : [{}]", loggingId);
      }
      catch (CoreException e) {
        log.error("Exception starting component", e);
        log.error("component [{}] cannot be restarted", loggingId);

      }
    }
  }

  private Set<StateManagedComponent> filter(Set<StateManagedComponent> list) {
    Set<StateManagedComponent> result = new LinkedHashSet<>();
    for (StateManagedComponent c : list) {
      String loggingId = friendlyName(c);
      if (c.retrieveComponentState() == StartedState.getInstance()) {
        log.trace("Component : [{}] will be restarted after recovery", loggingId);
        result.add(c);
      }
    }
    return result;
  }

  private void stopAndClose(Set<StateManagedComponent> list) {
    Set<StateManagedComponent> result = new LinkedHashSet<>();
    for (StateManagedComponent c : list) {
      String loggingId = friendlyName(c);
      // ID 167 - Let's check if the component is not in a closed state
      // If it's not then let's stop/close it, and we can restart after.
      if (c.retrieveComponentState() != ClosedState.getInstance()) {
        log.trace("Stop/Close affected component : [{}]", loggingId);
        LifecycleHelper.stopAndClose(c);
        if (c instanceof Channel) {
          ((Channel) c).toggleAvailability(false);
        }
      }
    }
  }

  protected List<Channel> getRegisteredChannels() {
    Collection<StateManagedComponent> listeners = retrieveConnection(AdaptrisConnection.class).retrieveExceptionListeners();
    List<Channel> result = new ArrayList<Channel>();
    for (StateManagedComponent el : listeners) {
      if (el instanceof Channel) {
        result.add((Channel) el);
      }
    }
    return result;
  }
}
