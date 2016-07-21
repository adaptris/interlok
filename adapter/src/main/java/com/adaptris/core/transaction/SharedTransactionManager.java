package com.adaptris.core.transaction;

import javax.jms.XAConnectionFactory;
import javax.transaction.xa.XAResource;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.CoreException;
import com.adaptris.core.SharedComponent;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("shared-transaction-manager")
@AdapterComponent
@ComponentProfile(summary = "A Transaction Manager that refers to another Transaction Manager configured elsewhere", tag = "transactionManager,base")
@DisplayOrder(order = {"lookupName"})
public class SharedTransactionManager extends SharedComponent implements TransactionManager {
  
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
    setProxiedTransactionManager((TransactionManager) triggerJndiLookup(this.getLookupName()));
  }

  @Override
  public void start() throws CoreException {
    // handled by SharedComponents
  }

  @Override
  public void stop() {
    // handled by SharedComponents
  }

  @Override
  public void close() {
    // handled by SharedComponents
  }

  @Override
  public void prepare() throws CoreException {
    // handled by SharedComponents
  }

  @Override
  public String getUniqueId() {
    return getProxiedTransactionManager().getUniqueId();
  }

  @Override
  public void preEnlistXAResource(String name, XAConnectionFactory connectionFactory) throws Exception {
    getProxiedTransactionManager().preEnlistXAResource(name, connectionFactory);
  }

  @Override
  public void enlistXAResource(String name, XAResource xaResource) throws Exception {
    getProxiedTransactionManager().enlistXAResource(name, xaResource);
  }

  @Override
  public void delistXAResource(String name, XAResource xaResource, int status) throws Exception {
    getProxiedTransactionManager().delistXAResource(name, xaResource, status);
  }

  @Override
  public void beginTransaction() throws Exception {
    getProxiedTransactionManager().beginTransaction();
  }

  @Override
  public boolean commit() throws Exception {
    return getProxiedTransactionManager().commit();
  }

  @Override
  public void rollback() throws Exception {
    getProxiedTransactionManager().rollback();
  }

  @Override
  public boolean transactionIsActive() throws Exception {
    return getProxiedTransactionManager().transactionIsActive();
  }

  @Override
  public void setRollbackOnly() throws Exception {
    getProxiedTransactionManager().setRollbackOnly();
  }

  public TransactionManager getProxiedTransactionManager() {
    return proxiedTransactionManager;
  }

  public void setProxiedTransactionManager(TransactionManager proxiedTransactionManager) {
    this.proxiedTransactionManager = proxiedTransactionManager;
  }

}
