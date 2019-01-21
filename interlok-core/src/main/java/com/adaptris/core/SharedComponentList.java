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
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.Removal;
import com.adaptris.core.transaction.TransactionManager;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JndiHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A common store for components.
 * 
 * @config shared-components
 * 
 */
@XStreamAlias("shared-components")
@AdapterComponent
@ComponentProfile(summary = "A Collection of Shared Components", tag = "base")
@DisplayOrder(order = {"connections", "services", "lifecycleStrategy", "debug"})
public class SharedComponentList implements ComponentLifecycle, ComponentLifecycleExtension {

  private static final DefaultLifecycleStrategy DEFAULT_STRATEGY = new DefaultLifecycleStrategy();

  @Valid
  @AutoPopulated
  @NotNull
  private List<AdaptrisConnection> connections;
  @Valid
  @AutoPopulated
  @NotNull
  private List<Service> services;
  @Valid
  private TransactionManager transactionManager;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean debug;
  @Valid
  @AdvancedConfig
  private SharedComponentLifecycleStrategy lifecycleStrategy;
  
  @Deprecated
  @Removal(version = "3.9.0")
  private String uniqueId;
  private transient Object lock = new Object();
  private transient InitialContext context = null;

  public SharedComponentList() {
    connections = new ArrayList<AdaptrisConnection>();
    services = new ArrayList<Service>();
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
    return new ArrayList<>(connections);
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
    doAddConnections(l);
  }


  /**
   * Returns a clone of the shared-services
   * 
   * <p>
   * This method is purely to allow the underlying XML marshaller to do it's thing, you are strongly encouraged to use the
   * corresponding methods {@link #addService(Service)}, {@link #addServices(Collection)}, {@link #removeService(String)} instead to
   * manipulate the contents of the list.
   * </p>
   * 
   * @return the list of connections.
   */
  public List<Service> getServices() {
    return new ArrayList<>(services);
  }

  public void setServices(List<Service> serviceLists) {
    ensureNoDuplicateIds(verifyHasUniqueId(serviceLists));
    services.clear();
    doAddServices(serviceLists);
  }

  @Override
  public void init() throws CoreException {
    verifyLookupNames();
    bindNotYetBound();
    lifecycleStrategy().init(getConnections());
    LifecycleHelper.init(getTransactionManager());
  }

  @Override
  public void start() throws CoreException {
    bindNotYetBound();
    lifecycleStrategy().start(getConnections());
    LifecycleHelper.start(getTransactionManager());
  }

  @Override
  public void stop() {
    lifecycleStrategy().stop(getConnections());
    LifecycleHelper.stop(getTransactionManager());
  }

  @Override
  public void close() {
    lifecycleStrategy().close(getConnections());
    LifecycleHelper.close(getTransactionManager());
    unbindAll();
  }

  @Override
  public void prepare() throws CoreException {
    verifyLookupNames();
    bindNotYetBound();
    for (AdaptrisConnection c : connections) {
      LifecycleHelper.prepare(c);
    }
    LifecycleHelper.prepare(getTransactionManager());
  }

  private synchronized void bindNotYetBound() throws CoreException {
    Collection<AdaptrisComponent> comps = all();
    synchronized (lock) {
      for (AdaptrisComponent c : comps) {
        if (!existsInJndi(c)) {
          bind(getContext(), c, isDebug());
        }
      }
    }
  }
  
  private boolean existsInJndi(AdaptrisComponent c) throws CoreException {
    boolean rc = false;
    try {
      rc = getContext().lookup(JndiHelper.createJndiName(c)) != null;
    }
    catch (NamingException e) {
      rc = false;
    }
    return rc;
  }

  private void unbindAll() {
    synchronized (lock) {
      unbindQuietly(connections);
      unbindQuietly(services);
      unbindQuietly(getTransactionManager());
    }
  }

  private Collection<AdaptrisComponent> all() {
    Collection<AdaptrisComponent> comps = new IgnoreNulls<AdaptrisComponent>();
    comps.addAll(getServices());
    comps.addAll(getConnections());
    comps.add(getTransactionManager());
    return comps;
  }

  private void verifyLookupNames() throws CoreException {
    try {
      ensureNoDuplicateIds(verifyHasUniqueId(all()));
    }
    catch (IllegalArgumentException e) {
      throw new CoreException(e);
    }
  }

  /**
   * Bind a previously added Connection to JNDI.
   * <p>
   * When runtime additions of a connection occurs (generally via
   * {@link com.adaptris.core.runtime.AdapterManagerMBean#addSharedConnection(String)}), they are not automatically bound to the
   * internal JNDI. Use this method after adding a connection to bind the connection to JNDI.
   * </p>
   * 
   * @param componentId the connection ID of connection to bind.
   * @throws CoreException wrapping other exceptions.
   */
  public void bindJNDI(String componentId) throws CoreException {
    Collection<AdaptrisComponent> comps = all();
    String id = Args.notNull(componentId, "componentId");
    for (AdaptrisComponent c : comps) {
      if (id.equals(c.getUniqueId())) {
        bind(getContext(), c, isDebug());
        break;
      }
    }
  }

  /**
   * Return a list of connection-ids that are registered.
   * 
   * @return a set of connection-ids
   */
  public Collection<String> getConnectionIds() {
    return idList(getConnections());
  }

  /**
   * Return a list of service-ids that are registered.
   * 
   * @return a set of service-ids
   */
  public Collection<String> getServiceIds() {
    return idList(getServices());
  }

  /**
   * Convenience method to add a connection performing verification.
   * 
   * @param c the connection to add
   * @throws IllegalArgumentException if the connection has no unique-id.
   * @return true if the object was successfully added, false if there was already a connection with that unique-id
   */
  public boolean addService(Service c) {
    verify(c);
    return doAddService(c);
  }

  /**
   * Convenience method to add service collections performing verification.
   * 
   * @param coll the service collections to add
   * @throws IllegalArgumentException if one or more service collections has no unique-id.
   * @return a collection of things that were rejected.
   */
  public Collection<Service> addServices(Collection<Service> coll) {
    verifyHasUniqueId(Args.notNull(coll, "services"));
    return doAddServices(coll);
  }
  
  /**
   * Convenience method to add a service collection performing verification.
   * 
   * @param c the service collection to add
   * @throws IllegalArgumentException if the service collection has no unique-id.
   * @return true if the object was successfully added, false if there was already a service collection with that unique-id
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
    verifyHasUniqueId(Args.notNull(coll, "connections"));
    return doAddConnections(coll);
  }

  private boolean doAddService(Service serviceCollection) {
    if (!containsService(serviceCollection.getUniqueId())) {
      return services.add(serviceCollection);
    }
    return false;
  }
  
  private Collection<Service> doAddServices(Collection<Service> coll) {
    List<Service> rejected = new ArrayList<Service>();
    for (Service c : coll) {
      if (!doAddService(c)) {
        rejected.add(c);
      }
    }
    return rejected;
  }
  
  private boolean doAddConnection(AdaptrisConnection c) {
    if (!containsConnection(c.getUniqueId())) {
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
   * Remove a service collection by it's ID.
   * 
   * @param id the unique-id of the service collection to remove.
   * @return a collection of the removed items
   */
  public Collection<Service> removeService(String id) {
    List<Service> keep = new ArrayList<>();
    List<Service> remove = new ArrayList<>();
    for (Service c : services) {
      if (c.getUniqueId().equals(id)) {
        remove.add(c);
      }
      else {
        keep.add(c);
      }
    }
    setServices(keep);
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
    return getConnectionIds().contains(id);
  }
  
  /**
   * Does the underlying service collection list contain this id.
   * 
   * @param id the ID to check for.
   * @return true if the ID exists.
   */
  public boolean containsService(String id) {
    return getServiceIds().contains(id);
  }
  

  private static List<String> idList(List<? extends AdaptrisComponent> comps) {
    List<String> result = new IgnoreNulls<String>();
    for (AdaptrisComponent c : comps) {
      result.add(c.getUniqueId());
    }
    return result;
  }

  private static void verify(AdaptrisComponent c) throws IllegalArgumentException {
    Args.notNull(c, "component");
    if (isEmpty(c.getUniqueId()))
      throw new IllegalArgumentException("Component " + c.getClass().getSimpleName() + "has no unique-id");
  }

  private static Collection<? extends AdaptrisComponent> verifyHasUniqueId(Collection<? extends AdaptrisComponent> components) {
    for (AdaptrisComponent c : components) {
      verify(c);
    }
    return components;
  }

  private static Collection<? extends AdaptrisComponent> ensureNoDuplicateIds(Collection<? extends AdaptrisComponent> components)
      throws IllegalArgumentException {
    Set<String> componentIds = new HashSet<>();
    for (AdaptrisComponent c : components) {
      componentIds.add(c.getUniqueId());
    }
    if (componentIds.size() != components.size()) {
      throw new IllegalArgumentException("Found shared components with duplicate IDs; please review config");
    }
    return components;
  }
  

  boolean isDebug() {
    return BooleanUtils.toBooleanDefaultIfNull(getDebug(), false);
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
      throw ExceptionHelper.wrapCoreException(e);
    }
    return context;
  }

  private void unbindQuietly(Collection<? extends AdaptrisComponent> components) {
    for (AdaptrisComponent c : components) {
      unbindQuietly(c);
    }
  }
  
  private void unbindQuietly(AdaptrisComponent component) {
    try {
      Context ctx = getContext();
      JndiHelper.unbindQuietly(ctx, component, isDebug());
    }
    catch (CoreException e) {
    }
  }

  public TransactionManager getTransactionManager() {
    return transactionManager;
  }

  public void setTransactionManager(TransactionManager tm) {
    this.transactionManager = tm;
  }

  /**
   * Not required as this component doesn't need to extend {@link AdaptrisComponent}
   * 
   * @deprecated since 3.6.3
   */
  @Deprecated
  @Removal(version = "3.9.0")
  public String getUniqueId() {
    return uniqueId;
  }

  /**
   * Not required as this component doesn't need to extend {@link AdaptrisComponent}
   * 
   * @deprecated since 3.6.3
   */
  @Deprecated
  @Removal(version = "3.9.0")
  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
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

  // Sadly PredicatedList throws an IllegalArgumentException if we fail the predicate; we just want to ignore.
  private static class IgnoreNulls<E> extends ArrayList<E> {
    @Override
    public boolean add(E e) {
      if (e == null) {
        return false;
      }
      return super.add(e);
    }
  }

}
