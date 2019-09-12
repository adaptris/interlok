package com.adaptris.core.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.security.password.Password;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Constructs a C3P0 connection pool for use with {@link JdbcPooledConnection} and
 * {@link PluggableJdbcPooledConnection}.
 * 
 * @config jdbc-default-pool-factory
 *
 */
@XStreamAlias("jdbc-default-pool-factory")
@ComponentProfile(summary = "Build a connection pool using C3P0")
public class DefaultPoolFactory implements JdbcPoolFactory, ConnectionPoolBuilder {

  protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  public ComboPooledDataSource create() throws Exception {
    return new ComboPooledDataSource();
  }

  @Override
  public C3P0PooledDataSource build(PluggableJdbcPooledConnection conn) throws Exception {
    ComboPooledDataSource pool = create();
    pool.setProperties(conn.connectionProperties());
    pool.setDriverClass(conn.getDriverImp());
    pool.setJdbcUrl(conn.getConnectUrl());
    pool.setUser(conn.getUsername());
    pool.setPassword(Password.decode(ExternalResolver.resolve(conn.getPassword())));
    PooledConnectionProperties.apply(conn.poolProperties(), pool);
    return new C3P0PooledDataSource(pool);
  }

}
