package com.adaptris.core;

import static com.adaptris.core.util.LoggingHelper.friendlyName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    if (connection == null) {
      throw new IllegalArgumentException("Registered connection may not be null");
    }
    adpConnection = connection;
  }

  // @Override
  // public AdaptrisConnection retrieveConnection() {
  // return adpConnection;
  // }

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
    Set<StateManagedComponent> listeners = connection.retrieveExceptionListeners();
    // Close all component first.
    // By stopping all components first, we ensure that the connection isn't continually restarted.
    // As connections might be shared across multiple components.
    Set<StateManagedComponent> toRestart = stopAndClose(listeners);
    // If the connection is a normal connection and part of a channel, it should already be stopped
    // So StateManagedComp will ignore it.
    // If it's part of a SharedConnnection list, then it won't be stopped, so let's stop it.
    stop(connection);
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
        log.trace("Initialising affected component : [" + loggingId + "]");
        LifecycleHelper.init(c);
      }
      catch (CoreException e) {
        log.error("Exception initialising component", e);
        log.error("component [" + loggingId + "] cannot be restarted.");

      }
    }
  }

  private void stop(AdaptrisConnection connection) {
    String loggingId = friendlyName(connection);
    log.trace("Stopping Connection : [{}]", loggingId);
    LifecycleHelper.stop(connection);
    LifecycleHelper.close(connection);
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

  private Set<StateManagedComponent> stopAndClose(Set<StateManagedComponent> list) {
    Set<StateManagedComponent> result = new LinkedHashSet<>();
    for (StateManagedComponent c : list) {
      String loggingId = friendlyName(c);
      // ID 167 - Let's check if the component is not in a closed state
      // If it's not then let's stop/close it, and we can restart after.
      if (c.retrieveComponentState() != ClosedState.getInstance()) {
        ComponentState previousState = c.retrieveComponentState();
        log.trace("Stop/Close affected component : [{}]", loggingId);
        LifecycleHelper.stop(c);
        LifecycleHelper.close(c);
        if (c instanceof Channel) {
          ((Channel) c).toggleAvailability(false);
        }
        // If the previous state was "started", then the component should be restarted.
        if (previousState == StartedState.getInstance()) {
          log.trace("Component : [{}] will be restarted", loggingId);
          result.add(c);
        }
        else {
          log.info("Component : [{}] was not started when recovery attempted, it will not be restarted", loggingId);
        }
      }
      else {
        log.trace("Component : [{}] currently closed, ignoring", loggingId);
      }
    }
    return result;
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
