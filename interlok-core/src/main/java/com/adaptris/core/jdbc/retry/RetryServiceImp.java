package com.adaptris.core.jdbc.retry;

import java.sql.SQLException;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.ServiceException;
import com.adaptris.core.http.jetty.retry.RetryStore;
import com.adaptris.core.jdbc.DatabaseConnection;
import com.adaptris.core.jdbc.JdbcService;
import com.adaptris.core.util.ExceptionHelper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;


/**
 * <p>
 * Partial implementation of behaviour common to retry services.
 * </p>
 */
@NoArgsConstructor
public abstract class RetryServiceImp extends JdbcService {
  
  protected static AdaptrisMarshaller marshaller;

  static { // only create marshaller once
    marshaller = DefaultMarshaller.getDefaultMarshaller();
  }
  
  @Getter
  @Setter
  private AdaptrisConnection connection;
  
  @NotNull
  @NonNull
  private RetryStore retryStore;

  @InputFieldDefault(value = "true")
  private boolean pruneAcknowledged;

  /**
   * <p>
   * Creates a new instance. Defaults to <code>JdbcRetryStore</code>.
   * </p>
   */


  /** @see com.adaptris.core.AdaptrisComponent#init() */
  @Override
  protected void initJdbcService() throws CoreException {
    if (getConnection() == null) {
      throw new CoreException("DatabaseConnection is null in service");
    }
    
    getConnection().init();
    getRetryStore().makeConnection(connection);
    getRetryStore().init();
  }

  /** @see com.adaptris.core.ServiceImp#start() */
  @Override
  public void start() throws CoreException {
    getConnection().start();
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  protected void closeJdbcService() {
    getConnection().close();
  }

  public final void doService(AdaptrisMessage msg) throws ServiceException {
    pruneAcknowledged();
    performService(msg);
  }

  protected abstract void performService(AdaptrisMessage msg)
      throws ServiceException;

  private void pruneAcknowledged() {
    try {
      if (isPruneAcknowledged()) {
        log.debug("Pruning Previously Acknowledged Messages");
        getRetryStore().deleteAcknowledged();
      }
    }
    catch (Exception e) {
      log.warn("Ignoring exception while pruning acknowledged messages["
          + e.getMessage() + "]");
    }
  }

  // properties

  /**
   * <p>
   * Returns the <code>RetryStore</code> to use.
   * </p>
   *
   * @return the <code>RetryStore</code> to use
   */
  public final RetryStore getRetryStore() {
    return retryStore;
  }

  /**
   * <p>
   * Sets the <code>RetryStore</code> to use. May not be null.
   * </p>
   *
   * @param r the <code>RetryStore</code> to use
   */
  public final void setRetryStore(RetryStore r) {
    if (r == null) {
      throw new IllegalArgumentException("null param");
    }
    this.retryStore = r;
  }


  /**
   * @return the pruneAcknowledged
   */
  public boolean getPruneAcknowledged() {
    return pruneAcknowledged;
  }

  /**
   * Specify whether to delete messages from the underlying store if they have
   * already been acknowledged.
   *
   * @param b the pruneAcknowledged to set
   */
  public void setPruneAcknowledged(boolean b) {
    this.pruneAcknowledged = b;
  }
  
  private boolean isPruneAcknowledged() {
    return BooleanUtils.toBooleanDefaultIfNull(getPruneAcknowledged(), false);
  }
  
  @Override
  protected void prepareService() throws CoreException {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected void startService() throws CoreException {
    // TODO Auto-generated method stub
    
  }

}
