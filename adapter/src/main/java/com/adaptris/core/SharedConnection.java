package com.adaptris.core;

import java.util.Properties;
import java.util.Set;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.InvalidNameException;
import javax.naming.NamingException;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.license.License;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A connection instance that references a connection made available via {@link SharedComponentList}.
 * 
 * @config shared-connection
 * @author amcgrath
 * 
 */
@XStreamAlias("shared-connection")
public class SharedConnection implements AdaptrisConnection {

  @NotBlank
  private String lookupName;

  private transient AdaptrisConnection proxiedConnection;

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  public SharedConnection() {

  }

  public SharedConnection(String lookupName) {
    this();
    setLookupName(lookupName);
  }

  private AdaptrisConnection getProxiedConnection() {
    try {
      if (proxiedConnection == null) {
        proxiedConnection = triggerJndiLookup(getLookupName());
      }
    }
    catch (CoreException e) {
      throw new RuntimeException(e);
    }
    return proxiedConnection;
  }

  private AdaptrisConnection triggerJndiLookup(String jndiName) throws CoreException {
    AdaptrisConnection result = null;
    try {
      InitialContext ctx = createContext();
      String compEnvName = this.checkAndComposeName(jndiName);
      String[] namesToTry =
      {
          compEnvName, jndiName
      };
      for (String name : namesToTry) {
        result = lookupQuietly(ctx, name);
        if (result != null) {
          break;
        }
      }
      if (result == null) {
        throw new CoreException("Failed to find connection: [" + compEnvName + "] or [" + jndiName + "]");
      }
    }
    catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    return result;
  }

  private InitialContext createContext() throws NamingException {
    Properties env = new Properties();
    env.put(Context.INITIAL_CONTEXT_FACTORY, JndiContextFactory.class.getName());
    return new InitialContext(env);
  }

  private AdaptrisConnection lookupQuietly(InitialContext ctx, String name) {
    AdaptrisConnection result = null;
    try {
      result = (AdaptrisConnectionImp) ctx.lookup(name);
    }
    catch (NamingException ex) {
    }
    return result;
  }

  /**
   * If the lookup name doesn't contain any subcontext (e.g. "comp/env/") then lets prepend these sub contexts to the name.
   * If however the lookup name does include at least one sub-context, no matter what that sub-context is, then leave the name alone.
   * @param jndiName
   * @return String jndiName
   * @throws InvalidNameException 
   */
  private String checkAndComposeName(String jndiName) throws InvalidNameException {
    CompositeName compositeName = new CompositeName(jndiName);
    if(compositeName.size() == 1) // no sub contexts
      jndiName = new CompositeName("comp/env/" + jndiName).toString();
    return jndiName;
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return true;
  }

  @Override
  public void init() throws CoreException {
    getProxiedConnection().init();
  }

  @Override
  public void start() throws CoreException {
    getProxiedConnection().start();
  }

  @Override
  public void stop() {
    getProxiedConnection().stop();
  }

  @Override
  public void close() {
    getProxiedConnection().close();
  }

  public void changeState(ComponentState newState) {
    getProxiedConnection().changeState(newState);
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

  @SuppressWarnings("unchecked")
  @Override
  public <T> T retrieveConnection(Class<T> type) {
    return (T) getProxiedConnection();
  }

  public String getLookupName() {
    return lookupName;
  }

  public void setLookupName(String jndiName) {
    this.lookupName = jndiName;
  }

  @Override
  public void prepare() throws CoreException {
    // No preparation required; already done by the underlying classes.
  }
}
