package com.adaptris.core.jdbc;

@FunctionalInterface
public interface ConnectionPoolBuilder {

  /**
   * Build a {@link PooledDataSource}.
   * 
   */
  PooledDataSource build(PluggableJdbcPooledConnection conn) throws Exception;
}
