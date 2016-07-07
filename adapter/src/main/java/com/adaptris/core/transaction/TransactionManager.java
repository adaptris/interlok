package com.adaptris.core.transaction;

import javax.jms.XAConnectionFactory;
import javax.transaction.xa.XAResource;

import com.adaptris.core.AdaptrisComponent;

public interface TransactionManager extends AdaptrisComponent {
  
  void preEnlistXAResource(String name, XAConnectionFactory connectionFactory) throws Exception;
  
  void enlistXAResource(String name, XAResource xaResource) throws Exception;
  
  void delistXAResource(String name, XAResource xaResource, int status) throws Exception;
  
  void beginTransaction() throws Exception;
  
  boolean commit() throws Exception;
  
  void rollback() throws Exception ;
  
  boolean transactionIsActive() throws Exception;

  void setRollbackOnly() throws Exception;
  
}
