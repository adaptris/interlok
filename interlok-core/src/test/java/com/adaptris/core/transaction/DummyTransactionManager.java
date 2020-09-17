package com.adaptris.core.transaction;

import javax.jms.XAConnectionFactory;
import javax.transaction.xa.XAResource;
import com.adaptris.core.CoreException;

public class DummyTransactionManager implements TransactionManager {

  private String uniqueId;

  private enum TransactionState {
    NOT_IN_TRANSACTION,
    IN_TRANSACTION,
    COMMIT,
    ROLLBACK;
  }

  private transient TransactionState state = TransactionState.NOT_IN_TRANSACTION;

  public DummyTransactionManager() {
  }


  public DummyTransactionManager(String uniqueId) {
    setUniqueId(uniqueId);
  }

  @Override
  public String getUniqueId() {
    return uniqueId;
  }

  @Override
  public void registerXAResource(String name, XAConnectionFactory connectionFactory) throws Exception {
  }

  @Override
  public void enlistXAResource(String name, XAResource xaResource) throws Exception {
  }

  @Override
  public void delistXAResource(String name, XAResource xaResource, int status) throws Exception {
  }

  @Override
  public void beginTransaction() throws Exception {
    state = TransactionState.IN_TRANSACTION;
  }

  @Override
  public boolean commit() throws Exception {
    state = TransactionState.COMMIT;
    return true;
  }

  @Override
  public void rollback() throws Exception {
    state = TransactionState.ROLLBACK;
  }

  @Override
  public boolean transactionIsActive() throws Exception {
    return state == TransactionState.IN_TRANSACTION;
  }

  @Override
  public void setRollbackOnly() throws Exception {
  }

  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }


  @Override
  public void deRegisterXAResource(String name, XAConnectionFactory connectionFactory) throws Exception {
  }


  @Override
  public void prepare() throws CoreException {}

}
