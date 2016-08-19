package com.adaptris.core.transaction;

import javax.jms.XAConnectionFactory;
import javax.transaction.xa.XAResource;

import com.adaptris.core.CoreException;

public class DummyTransactionManager implements TransactionManager {
  
  private String uniqueId;
  
  private String lookupName;
  
  public DummyTransactionManager() {
  }
  
  public DummyTransactionManager(String uniqueId, String lookupName) {
    this.setLookupName(lookupName);
    this.setUniqueId(uniqueId);
  }

  @Override
  public void init() throws CoreException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void start() throws CoreException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void stop() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void close() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void prepare() throws CoreException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public String getLookupName() {
    return lookupName;
  }

  @Override
  public String getUniqueId() {
    return uniqueId;
  }

  @Override
  public void registerXAResource(String name, XAConnectionFactory connectionFactory) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void enlistXAResource(String name, XAResource xaResource) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void delistXAResource(String name, XAResource xaResource, int status) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void beginTransaction() throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean commit() throws Exception {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void rollback() throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean transactionIsActive() throws Exception {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void setRollbackOnly() throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  public void setLookupName(String lookupName) {
    this.lookupName = lookupName;
  }

  @Override
  public void deRegisterXAResource(String name, XAConnectionFactory connectionFactory) throws Exception {
    // TODO Auto-generated method stub
    
  }

}
