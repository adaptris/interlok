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

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.LifecycleHelper;

/**
 * <p>
 * Contains behaviour common to implementations of <code>AdaptrisConnection</code>.
 * </p>
 */
public abstract class AdaptrisConnectionImp implements AdaptrisConnection, StateManagedComponent {
  // protected transient Log log = LogFactory.getLog(this.getClass().getName());

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  @AdvancedConfig
  private String lookupName;
  @AdvancedConfig
  @Valid
  private ConnectionErrorHandler connectionErrorHandler;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean workersFirstOnShutdown;
  private String uniqueId;

  private transient Object lock = new Object();
  private transient Set<StateManagedComponent> listeners; // back ref to parent Channel
  private transient Set<AdaptrisMessageConsumer> consumers;
  private transient Set<AdaptrisMessageProducer> producers;
  private transient ComponentState state;
  private transient boolean prepared = false;
  /**
   * <p>
   * Created a new instance.
   * </p>
   */
  public AdaptrisConnectionImp() {
    consumers = Collections.newSetFromMap(new WeakHashMap<AdaptrisMessageConsumer, Boolean>());
    producers = Collections.newSetFromMap(new WeakHashMap<AdaptrisMessageProducer, Boolean>());
    listeners = Collections.newSetFromMap(new WeakHashMap<StateManagedComponent, Boolean>());
    state = ClosedState.getInstance();
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  @Override
  public final void init() throws CoreException {
    synchronized (lock) {
      if (!prepared) {
        prepare();
      }
      initConnection();
      // Intentionly after initialising the connection as the connection error
      // handler will almost certainly use it.
      if (connectionErrorHandler != null) {
        LifecycleHelper.init(connectionErrorHandler);
      }
    }
  }

  @Override
  public final void prepare() throws CoreException {
    if (connectionErrorHandler != null) {
      connectionErrorHandler.registerConnection(this);
    }
    prepareConnection();
    prepared = true;
  }

  protected abstract void prepareConnection() throws CoreException;
  /**
   * 
   * @see com.adaptris.core.AdaptrisComponent#start()
   */
  @Override
  public final void start() throws CoreException {
    synchronized (lock) {
      startConnection();
      if (connectionErrorHandler != null) {
        LifecycleHelper.start(connectionErrorHandler);
      }
    }
  }

  /**
   * 
   * @see com.adaptris.core.AdaptrisComponent#close()
   */
  @Override
  public final void close() {
    synchronized (lock) {

      if (workersFirstOnShutdown()) {
        log.trace("Closing registered message consumers prior to connection closure");
        for (AdaptrisMessageConsumer o : retrieveMessageConsumers()) {
          LifecycleHelper.close(o);
        }
        log.trace("Closing registered message producers prior to connection closure");
        for (AdaptrisMessageProducer o : retrieveMessageProducers()) {
          LifecycleHelper.close(o);
        }
      }
      if (connectionErrorHandler != null) {
        LifecycleHelper.close(connectionErrorHandler);
      }
      closeConnection();
    }
  }

  /**
   * 
   * @see com.adaptris.core.AdaptrisComponent#stop()
   */
  @Override
  public final void stop() {
    synchronized (lock) {
      if (workersFirstOnShutdown()) {
        log.trace("Stopping registered message consumers prior to connection stop");
        for (AdaptrisMessageConsumer o : retrieveMessageConsumers()) {
          LifecycleHelper.stop(o);
        }
        log.trace("Stopping registered message producers prior to connection stop");
        for (AdaptrisMessageProducer o : retrieveMessageProducers()) {
          LifecycleHelper.stop(o);
        }
      }
      if (connectionErrorHandler != null) {
        LifecycleHelper.stop(connectionErrorHandler);
      }
      stopConnection();
    }
  }

  /**
   * Initialise the underlying connection.
   * 
   * @throws CoreException wrapping any exception.
   */
  protected abstract void initConnection() throws CoreException;

  /**
   * Start the underlying connection.
   * 
   * @throws CoreException wrapping any exception.
   */
  protected abstract void startConnection() throws CoreException;

  /**
   * Stop the underlying connection.
   * 
   */
  protected abstract void stopConnection();

  /**
   * Close the underlying connection.
   * 
   */
  protected abstract void closeConnection();

  @Override
  public void addExceptionListener(StateManagedComponent c) {
    listeners.add(c);
  }

  @Override
  public Set<StateManagedComponent> retrieveExceptionListeners() {
    return listeners;
  }

  /**
   * @see com.adaptris.core.AdaptrisConnection #addMessageProducer(com.adaptris.core.AdaptrisMessageProducer)
   */
  @Override
  public void addMessageProducer(AdaptrisMessageProducer producer) throws CoreException {

    if (producer != null && !producers.contains(producer)) {
      producers.add(producer);
      producer.registerConnection(this); // set back ref
    }
  }

  /** @see com.adaptris.core.AdaptrisConnection#retrieveMessageProducers() */
  @Override
  public Set<AdaptrisMessageProducer> retrieveMessageProducers() {
    return producers;
  }

  /**
   * @see com.adaptris.core.AdaptrisConnection #addMessageConsumer(com.adaptris.core.AdaptrisMessageConsumer)
   */
  @Override
  public void addMessageConsumer(AdaptrisMessageConsumer consumer) throws CoreException {

    if (consumer != null && !consumers.contains(consumer)) {
      consumers.add(consumer);
      consumer.registerConnection(this); // set back ref
    }
  }

  /** @see com.adaptris.core.AdaptrisConnection#retrieveMessageConsumers() */
  @Override
  public Set<AdaptrisMessageConsumer> retrieveMessageConsumers() {
    return consumers;
  }

  /**
   * @see com.adaptris.core.AdaptrisConnection #setConnectionErrorHandler(com.adaptris.core.ConnectionErrorHandler)
   */
  @Override
  public void setConnectionErrorHandler(ConnectionErrorHandler handler) {
    connectionErrorHandler = handler;
    // connectionErrorHandler.registerConnection(this);
  }

  /** @see com.adaptris.core.AdaptrisConnection#getConnectionErrorHandler() */
  @Override
  public ConnectionErrorHandler getConnectionErrorHandler() {
    return connectionErrorHandler;
  }

  /**
   * @return the workerLifecycleFirstOnShutdown
   */
  public Boolean getWorkersFirstOnShutdown() {
    return workersFirstOnShutdown;
  }

  /**
   * @return the workerLifecycleFirstOnShutdown
   */
  public boolean workersFirstOnShutdown() {
    return workersFirstOnShutdown != null ? workersFirstOnShutdown.booleanValue() : false;
  }

  /**
   * Perform consumer and producer lifecycle shutdown prior to connection shutdown.
   * <p>
   * When set to true, this method forces the AdaptrisConnection to stop and close any registered MessageConsumer and
   * MessageProducer implementations before attempting to stop and close the underlying Connection. This is useful in cases where
   * shutting down the Connection blocks until all attached workers are manually closed (WebsphereMQ)
   * </p>
   * <p>
   * When set to false, existing behaviour is preserved, and the consumers and producers are shutdown as part of the normal workflow
   * lifecycle.
   * </p>
   * 
   * @param b true to shutdown workers prior to connection shutdown, default is false.
   */
  public void setWorkersFirstOnShutdown(Boolean b) {
    workersFirstOnShutdown = b;
  }

  /**
   * Return the connection as represented by this connection
   * 
   * @param type the type of connection
   * @return the connection
   */
  @Override
  public <T> T retrieveConnection(Class<T> type) {
    return (T) this;
  }

  @Override
  public String getUniqueId() {
    return uniqueId;
  }

  public void setUniqueId(String s) {
    uniqueId = s;
  }

  public void changeState(ComponentState s) {
    state = s;
  }

  /** @see com.adaptris.core.StateManagedComponent#requestInit() */
  public void requestInit() throws CoreException {
    state.requestInit(this);
  }

  /** @see com.adaptris.core.StateManagedComponent#requestStart() */
  public void requestStart() throws CoreException {
    state.requestStart(this);
  }

  /** @see com.adaptris.core.StateManagedComponent#requestStop() */
  public void requestStop() {
    state.requestStop(this);
  }

  /** @see com.adaptris.core.StateManagedComponent#requestClose() */
  public void requestClose() {
    state.requestClose(this);
  }

  public ComponentState retrieveComponentState() {
    return state;
  }

  public String getLookupName() {
    return lookupName;
  }

  public void setLookupName(String jndiName) {
    this.lookupName = jndiName;
  }

}
