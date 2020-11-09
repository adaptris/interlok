package com.adaptris.core.jdbc;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.security.password.Password;
import com.adaptris.util.SimpleBeanUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Synchronized;

/**
 * Constructs a HikariCP connection pool for use with {@link PluggableJdbcPooledConnection}.
 *
 * @config jdbc-hikari-pool-builder
 *
 */
@XStreamAlias("jdbc-hikari-pool-builder")
@ComponentProfile(summary = "Build a connection pool using HikariCP", since = "3.9.2")
public class HikariPoolBuilder implements ConnectionPoolBuilder {

  private transient Logger slf4jLogger = LoggerFactory.getLogger(this.getClass());

  public HikariPoolBuilder() {

  }

  @Override
  public PooledDataSource build(PluggableJdbcPooledConnection conn) throws Exception {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(conn.getConnectUrl());
    config.setUsername(conn.getUsername());
    config.setPassword(Password.decode(ExternalResolver.resolve(conn.getPassword())));
    config.setDriverClassName(conn.getDriverImp());
    config.setDataSourceProperties(conn.connectionProperties());
    config.setAutoCommit(conn.autoCommit());
    // Call all the setters
    conn.poolProperties().stream().forEach((kvp) -> {
      SimpleBeanUtil.callSetter(config, "set" + kvp.getKey(), kvp.getValue());
    });
    return new HikariDataSourceWrapper(config);
  }


  // Building the HikariDataSource means that we try to make the connection, there's nothing
  // in the HikariDataSource configuration that suggests we can be lazy about this
  // So, do it when we actually try to access something...
  // INTERLOK-3463
  public class HikariDataSourceWrapper extends PooledDataSourceImpl<HikariDataSource> {

    private HikariConfig config;
    private HikariDataSource wrapped = null;
    private volatile boolean built = false;
    private Object lock = new Object();

    public HikariDataSourceWrapper(HikariConfig config) {
      super();
      this.config = config;
    }

    @Override
    public HikariDataSource wrapped() {
      if (!built) {
        build();
      }
      return wrapped;
    }

    @Synchronized(value = "lock")
    private void build() {
      wrapped = new HikariDataSource(config);
      built = true;
    }

    @Override
    public void close() throws IOException {
      wrapped().close();
    }

  }
}
