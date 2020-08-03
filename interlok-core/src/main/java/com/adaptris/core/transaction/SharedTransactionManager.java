package com.adaptris.core.transaction;

import javax.jms.XAConnectionFactory;
import javax.transaction.xa.XAResource;
import javax.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.CoreException;
import com.adaptris.core.SharedComponent;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("shared-transaction-manager")
@AdapterComponent
@ComponentProfile(summary = "A Transaction Manager that refers to another Transaction Manager configured elsewhere",
    tag = "transactionManager,base")
@DisplayOrder(order = {"lookupName"})
public class SharedTransactionManager extends SharedComponent implements TransactionManager {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  @NotBlank
  private String lookupName;

  private transient TransactionManager proxiedTransactionManager;

  public SharedTransactionManager() {

  }

  public SharedTransactionManager(String lookupName) {
    this();
    setLookupName(lookupName);
  }



  public String getLookupName() {
    return lookupName;
  }

  public void setLookupName(String lookupName) {
    this.lookupName = lookupName;
  }

  @Override
  public void init() throws CoreException {
    // getProxiedTransactionManager().init();
  }

  @Override
  public void start() throws CoreException {
    // getProxiedTransactionManager().start();
  }

  @Override
  public void stop() {
    // handled by SharedComponents
    // getProxiedTransactionManager().stop();
  }

  @Override
  public void close() {
    // handled by SharedComponents
    // getProxiedTransactionManager().close();
  }

  @Override
  public void prepare() throws CoreException {
    // handled by SharedComponents
    // getProxiedTransactionManager().prepare();
  }

  @Override
  public String getUniqueId() {
    return proxiedTransactionManager().getUniqueId();
  }

  @Override
  public void registerXAResource(String name, XAConnectionFactory connectionFactory) throws Exception {
    proxiedTransactionManager().registerXAResource(name, connectionFactory);
  }

  @Override
  public void deRegisterXAResource(String name, XAConnectionFactory connectionFactory) throws Exception {
    proxiedTransactionManager().deRegisterXAResource(name, connectionFactory);
  }

  @Override
  public void enlistXAResource(String name, XAResource xaResource) throws Exception {
    proxiedTransactionManager().enlistXAResource(name, xaResource);
  }

  @Override
  public void delistXAResource(String name, XAResource xaResource, int status) throws Exception {
    proxiedTransactionManager().delistXAResource(name, xaResource, status);
  }

  @Override
  public void beginTransaction() throws Exception {
    proxiedTransactionManager().beginTransaction();
  }

  @Override
  public boolean commit() throws Exception {
    return proxiedTransactionManager().commit();
  }

  @Override
  public void rollback() throws Exception {
    proxiedTransactionManager().rollback();
  }

  @Override
  public boolean transactionIsActive() throws Exception {
    return proxiedTransactionManager().transactionIsActive();
  }

  @Override
  public void setRollbackOnly() throws Exception {
    proxiedTransactionManager().setRollbackOnly();
  }

  public TransactionManager proxiedTransactionManager() {
    if (proxiedTransactionManager == null)
      try {
        setProxiedTransactionManager((TransactionManager) triggerJndiLookup(getLookupName()));
      } catch (CoreException e) {
        log.error("Cannot look-up shared transaction manager with name: " + getLookupName());
      }
    return proxiedTransactionManager;
  }

  void setProxiedTransactionManager(TransactionManager proxiedTransactionManager) {
    this.proxiedTransactionManager = proxiedTransactionManager;
  }

}
