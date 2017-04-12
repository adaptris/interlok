package com.adaptris.core.transaction;

import javax.jms.XAConnectionFactory;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.JndiBindable;

public interface TransactionManager extends AdaptrisComponent, JndiBindable {
  
  void registerXAResource(String name, XAConnectionFactory connectionFactory) throws Exception;
  
  void deRegisterXAResource(String name, XAConnectionFactory connectionFactory) throws Exception;
  
  void enlistXAResource(String name, XAResource xaResource) throws Exception;
  
  void delistXAResource(String name, XAResource xaResource, int status) throws Exception;
  
  void beginTransaction() throws SystemException, NotSupportedException;
  
  Transaction getTransaction() throws SystemException;
  
  String currentTransactionId();
  
  boolean commit() throws Exception;
  
  void rollback() throws Exception;
  
  boolean transactionIsActive() throws Exception;

  void setRollbackOnly() throws Exception;
  
}
