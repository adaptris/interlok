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

import java.util.Set;
import javax.validation.constraints.NotBlank;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A connection instance that references a connection made available via {@link SharedComponentList}.
 * 
 * @config shared-connection
 * @author amcgrath
 * 
 */
@XStreamAlias("shared-connection")
@AdapterComponent
@ComponentProfile(summary = "A Connection that refers to another connection configured elsewhere", tag = "connections,base")
@DisplayOrder(order = {"lookupName"})
public class SharedConnection extends SharedComponent implements AdaptrisConnection {

  @NotBlank
  private String lookupName;

  private transient AdaptrisConnection proxiedConnection;

  public SharedConnection() {

  }

  public SharedConnection(String lookupName) {
    this();
    setLookupName(lookupName);
  }

  private AdaptrisConnection getProxiedConnection() {
    try {
      if (proxiedConnection == null) {
        proxiedConnection = (AdaptrisConnection) triggerJndiLookup(getLookupName());
      }
    }
    catch (CoreException e) {
      throw new RuntimeException(e);
    }
    return proxiedConnection;
  }

  @Override
  public void init() throws CoreException {
    // No-Op as requestXXX invokes the underlying connection
  }

  @Override
  public void start() throws CoreException {
    // No-Op as requestXXX invokes the underlying connection
  }

  @Override
  public void stop() {
    // No-Op as requestXXX invokes the underlying connection
  }

  @Override
  public void close() {
    // No-Op as requestXXX invokes the underlying connection
  }

  @Override
  public void changeState(ComponentState newState) {
    // No-Op as requestXXX invokes the underlying connection
  }
  
  @Override
  public ComponentState retrieveComponentState() {
    return getProxiedConnection().retrieveComponentState();
  }

  @Override
  public void requestInit() throws CoreException {
    getProxiedConnection().requestInit();
  }

  @Override
  public void requestStart() throws CoreException {
    getProxiedConnection().requestStart();
  }

  @Override
  public void requestStop() {
    // Don't close, the adapter will stop the connection.
    // getProxiedConnection().requestStop();
  }

  @Override
  public void requestClose() {
    // Don't close, the adapter will close the connection.
    // getProxiedConnection().requestClose();
  }

  @Override
  public String getUniqueId() {
    return getProxiedConnection().getUniqueId();
  }

  @Override
  public Set<StateManagedComponent> retrieveExceptionListeners() {
    return getProxiedConnection().retrieveExceptionListeners();
  }

  @Override
  public void addExceptionListener(StateManagedComponent comp) {
    getProxiedConnection().addExceptionListener(comp);
  }

  @Override
  public void addMessageProducer(AdaptrisMessageProducer producer) throws CoreException {
    getProxiedConnection().addMessageProducer(producer);
  }

  @Override
  public Set<AdaptrisMessageProducer> retrieveMessageProducers() {
    return getProxiedConnection().retrieveMessageProducers();
  }

  @Override
  public void addMessageConsumer(AdaptrisMessageConsumer consumer) throws CoreException {
    getProxiedConnection().addMessageConsumer(consumer);
  }

  @Override
  public Set<AdaptrisMessageConsumer> retrieveMessageConsumers() {
    return getProxiedConnection().retrieveMessageConsumers();
  }

  @Override
  public void setConnectionErrorHandler(ConnectionErrorHandler handler) {
    getProxiedConnection().setConnectionErrorHandler(handler);
  }

  @Override
  public ConnectionErrorHandler getConnectionErrorHandler() {
    return getProxiedConnection().getConnectionErrorHandler();
  }

  @Override
  public ConnectionErrorHandler connectionErrorHandler() {
    return getProxiedConnection().connectionErrorHandler();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T retrieveConnection(Class<T> type) {
    return (T) getProxiedConnection();
  }

  @Override
  public String getLookupName() {
    return lookupName;
  }

  /**
   * Set the unique-id of the connection that we will lookup.
   * 
   * @param jndiName the name
   */
  public void setLookupName(String jndiName) {
    this.lookupName = jndiName;
  }

  @Override
  public void prepare() throws CoreException {
    // No preparation required; already done by the underlying classes.
  }

  @Override
  public AdaptrisConnection cloneForTesting() throws CoreException {
    return getProxiedConnection().cloneForTesting();
  }
}
