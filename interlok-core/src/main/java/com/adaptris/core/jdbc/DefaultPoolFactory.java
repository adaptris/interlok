package com.adaptris.core.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.CoreException;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * @config jdbc-default-pool-factory
 *
 */
@XStreamAlias("jdbc-default-pool-factory")
public class DefaultPoolFactory implements JdbcPoolFactory {

  protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  public ComboPooledDataSource create(JdbcPoolConfiguration cfg) throws CoreException {
    ComboPooledDataSource result = new ComboPooledDataSource();
    try {

      result.setMinPoolSize(cfg.minPoolSize());
      result.setAcquireIncrement(cfg.acquireIncrement());
      result.setMaxPoolSize(cfg.maxPoolSize());
      // Milliseconds
      result.setCheckoutTimeout(cfg.connectionAcquireWait());
      // Milliseconds
      result.setAcquireRetryDelay(Long.valueOf(cfg.connectionRetryInterval()).intValue());
      result.setAcquireRetryAttempts(cfg.connectionAttempts());
      result.setTestConnectionOnCheckin(cfg.alwaysValidateConnection());
      result.setTestConnectionOnCheckout(cfg.alwaysValidateConnection());
      // Seconds
      result.setIdleConnectionTestPeriod(cfg.idleConnectionTestPeriod());
      // Seconds
      result.setMaxIdleTime(cfg.maxIdleTime());
    } catch (Exception ex) {
      throw new CoreException(ex);
    }
    return result;
  }

}
