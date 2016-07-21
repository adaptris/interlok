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

import static com.adaptris.core.util.JndiHelper.bind;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.GenerateBeanInfo;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.transaction.TransactionManager;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JndiHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A common store for components.
 * 
 * @config shared-components
 * 
 * @author lchan
 * 
 */
@XStreamAlias("shared-components")
@GenerateBeanInfo
@AdapterComponent
@ComponentProfile(summary = "A Collection of Shared Components", tag = "base")
@DisplayOrder(order = {"connections", "lifecycleStrategy", "debug"})
public class SharedComponentList implements AdaptrisComponent, ComponentLifecycleExtension {

  private static final DefaultLifecycleStrategy DEFAULT_STRATEGY = new DefaultLifecycleStrategy();

  @Valid
  @AutoPopulated
  @NotNull
  private List<AdaptrisConnection> connections;
  @Valid
  private TransactionManager transactionManager;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean debug;
  @Valid
  private SharedComponentLifecycleStrategy lifecycleStrategy;
  private transient Set<String> connectionIds;
  private transient Set<String> notYetInJndi;
  private transient InitialContext context = null;

  public SharedComponentList() {
    connections = new ArrayList<AdaptrisConnection>();
    connectionIds = new HashSet<>();
    notYetInJndi = new HashSet<>();
  }

  /**
   * Returns a clone of the connections list.
   * 
   * <p>
   * This method is purely to allow the underlying XML marshaller to do it's thing, you are strongly encouraged to use the
   * corresponding methods {@link #addConnection(AdaptrisConnection)}, {@link #addConnections(Collection)},
   * {@link #removeConnection(String)} instead to manipulate the contents of the list.
   * </p>
   * 
   * @return the list of connections.
   */
  public List<AdaptrisConnection> getConnections() {
    return new ArrayList<AdaptrisConnection>(connections);
  }

  /**
   * Set the connections.
   * 
   * <p>
   * Note that this simply calls {@link Collection#clear()} and {@link #addConnections(Collection)} rather than setting the
   * underlying object directly.
   * </p>
   * 
   * @param l the list to set.
   * @throws IllegalArgumentException if one or more connections had no unique-id, or if one or more connections would be rejected
   *           because of duplication.
   */
  public void setConnections(List<AdaptrisConnection> l) {
    ensureNoDuplicateIds(verifyHasUniqueId(l));
    connections.clear();
    connectionIds.clear();
    doAddConnections(l);
  }


  @Override
  public void init() throws CoreException {
    bindNotYetBound();
    lifecycleStrategy().init(connections);
    if(getTransactionManager() != null)
      LifecycleHelper.init(getTransactionManager());
  }

  @Override
  public void start() throws CoreException {
    bindNotYetBound();
    lifecycleStrategy().start(connections);
    if(getTransactionManager() != null)
      LifecycleHelper.start(getTransactionManager());
  }

  @Override
  public void stop() {
    lifecycleStrategy().stop(connections);
    if(getTransactionManager() != null)
      LifecycleHelper.stop(getTransactionManager());
  }

  @Override
  public void close() {
    lifecycleStrategy().close(connections);
    if(getTransactionManager() != null)
      LifecycleHelper.close(getTransactionManager());
    unbindAll();
  }

  @Override
  public void prepare() throws CoreException {
    bindNotYetBound();
    for (AdaptrisConnection c : connections) {
      c.prepare();
    }
    if(getTransactionManager() != null) {
      if(getTransactionManager().getUniqueId() == null)
        throw new CoreException("Transaction Manager cannot have a null unique-id");
      getTransactionManager().prepare();
    }
  }

  private synchronized void bindNotYetBound() throws CoreException {
    for (String id : notYetInJndi) {
      bindJNDI(id);
    }
    notYetInJndi.clear();
  }
  
  private synchronized void unbindAll() {
    unbindQuietly(connections);
    unbindQuietly(getTransactionManager());
    notYetInJndi.addAll(connectionIds);
  }

  /**
   * Bind a previously added Connection to JNDI.
   * <p>
   * When runtime additions of a connection occurs (generally via
   * {@link com.adaptris.core.runtime.AdapterManagerMBean#addSharedConnection(String)}), they are not automatically bound to the
   * internal JNDI. Use this method after adding a connection to bind the connection to JNDI.
   * </p>
   * 
   * @param connectionId the connection ID of connection to bind.
   * @throws CoreException wrapping other exceptions.
   */
  public void bindJNDI(String connectionId) throws CoreException {
    AdaptrisConnection connectionToRegister = null;
    for (AdaptrisConnection c : connections) {
      if (c.getUniqueId().equals(connectionId)) {
        connectionToRegister = c;
        break;
      }
    }
    if(connectionToRegister != null)
      bind(getContext(), connectionToRegister, isDebug());
    else if(getTransactionManager() != null) {
      if(connectionId.equals(getTransactionManager().getUniqueId()))
        bind(getContext(), getTransactionManager(), isDebug());
    }
  }

  /**
   * Return a list of connection-ids that are registered.
   * 
   * @return a set of connection-ids
   */
  public Collection<String> getConnectionIds() {
    return new ArrayList<String>(connectionIds);
  }

  /**
   * Convenience method to add a connection performing verification.
   * 
   * @param c the connection to add
   * @throws IllegalArgumentException if the connection has no unique-id.
   * @return true if the object was successfully added, false if there was already a connection with that unique-id
   */
  public boolean addConnection(AdaptrisConnection c) {
    verify(c);
    return doAddConnection(c);
  }

  /**
   * Convenience method to add connections performing verification.
   * 
   * @param coll the connections to add
   * @throws IllegalArgumentException if one or more connections has no unique-id.
   * @return a collection of things that were rejected.
   */
  public Collection<AdaptrisConnection> addConnections(Collection<AdaptrisConnection> coll) {
    if (coll == null) throw new IllegalArgumentException("Collection is null");
    verifyHasUniqueId(coll);
    return doAddConnections(coll);
  }

  private boolean doAddConnection(AdaptrisConnection c) {
    if (!containsConnection(c.getUniqueId())) {
      connectionIds.add(c.getUniqueId());
      notYetInJndi.add(c.getUniqueId());
      return connections.add(c);
    }
    return false;
  }

  private Collection<AdaptrisConnection> doAddConnections(Collection<AdaptrisConnection> coll) {
    List<AdaptrisConnection> rejected = new ArrayList<AdaptrisConnection>();
    for (AdaptrisConnection c : coll) {
      if (!doAddConnection(c)) {
        rejected.add(c);
      }
    }
    return rejected;
  }

  /**
   * Remove a connection by it's ID.
   * 
   * @param id the unique-id of the connection to remove.
   * @return a collection of the removed items
   */
  public Collection<AdaptrisConnection> removeConnection(String id) {
    List<AdaptrisConnection> keep = new ArrayList<>();
    List<AdaptrisConnection> remove = new ArrayList<>();
    for (AdaptrisConnection c : connections) {
      if (c.getUniqueId().equals(id)) {
        remove.add(c);
      }
      else {
        keep.add(c);
      }
    }
    setConnections(keep);
    // unbind them from JNDI as well
    unbindQuietly(remove);
    return remove;
  }

  /**
   * Does the underlying connection list contain this id.
   * 
   * @param id the ID to check for.
   * @return true if the ID exists.
   */
  public boolean containsConnection(String id) {
    return connectionIds.contains(id);
  }
  

  private static void verify(AdaptrisConnection c) throws IllegalArgumentException {
    if (c == null) throw new IllegalArgumentException("Connection is null");
    if (isEmpty(c.getUniqueId()))
      throw new IllegalArgumentException("Connection " + c.getClass().getSimpleName() + "has no unique-id");
  }

  private static Collection<AdaptrisConnection> verifyHasUniqueId(Collection<AdaptrisConnection> connections) {
    for (AdaptrisConnection c : connections) {
      verify(c);
    }
    return connections;
  }

  private static Collection<AdaptrisConnection> ensureNoDuplicateIds(Collection<AdaptrisConnection> connections)
      throws IllegalArgumentException {
    Set<String> connectionIds = new HashSet<>();
    for (AdaptrisConnection c : connections) {
      connectionIds.add(c.getUniqueId());
    }
    if (connectionIds.size() != connections.size()) {
      throw new IllegalArgumentException("Shared connections has duplicate IDs; please review config");
    }
    return connections;
  }
  

  Boolean isDebug() {
    return getDebug() != null ? getDebug().booleanValue() : false;
  }

  public Boolean getDebug() {
    return debug;
  }

  public void setDebug(Boolean debug) {
    this.debug = debug;
  }

  public SharedComponentLifecycleStrategy getLifecycleStrategy() {
    return lifecycleStrategy;
  }

  public void setLifecycleStrategy(SharedComponentLifecycleStrategy ls) {
    this.lifecycleStrategy = ls;
  }

  SharedComponentLifecycleStrategy lifecycleStrategy() {
    return getLifecycleStrategy() != null ? getLifecycleStrategy() : DEFAULT_STRATEGY;
  }

  private Context getContext() throws CoreException {
    try {
      if (context == null) {
        Properties contextEnv = new Properties();
        contextEnv.put(Context.INITIAL_CONTEXT_FACTORY, JndiContextFactory.class.getName());
        context = new InitialContext(contextEnv);
      }
    }
    catch (NamingException e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    return context;
  }

  private void unbindQuietly(Collection<AdaptrisConnection> connections) {
    Context ctx = null;
    try {
      ctx = getContext();
    }
    catch (CoreException e) {
      return;
    }
    for (AdaptrisConnection c : connections) {
      JndiHelper.unbindQuietly(ctx, c, isDebug());
    }
  }
  
  private void unbindQuietly(TransactionManager transactionManager) {
    Context ctx = null;
    try {
      ctx = getContext();
    }
    catch (CoreException e) {
      return;
    }
    JndiHelper.unbindQuietly(ctx, transactionManager, isDebug());
  }


  private static class DefaultLifecycleStrategy implements SharedComponentLifecycleStrategy {

    @Override
    public void init(Collection<AdaptrisConnection> conns) throws CoreException {
      for (AdaptrisConnection c : conns) {
        LifecycleHelper.init(c);
      }
    }


    @Override
    public void start(Collection<AdaptrisConnection> conns) throws CoreException {
      for (AdaptrisConnection c : conns) {
        LifecycleHelper.start(c);
      }
    }


    @Override
    public void stop(Collection<AdaptrisConnection> conns) {
      for (AdaptrisConnection c : conns) {
        LifecycleHelper.stop(c);
      }
    }

    @Override
    public void close(Collection<AdaptrisConnection> conns) {
      for (AdaptrisConnection c : conns) {
        LifecycleHelper.close(c);
      }
    }
    
  }


  public TransactionManager getTransactionManager() {
    return transactionManager;
  }

  public void setTransactionManager(TransactionManager transactionManager) {
    this.transactionManager = transactionManager;
    notYetInJndi.add(transactionManager.getUniqueId());
  }

}
