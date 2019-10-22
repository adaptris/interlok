package com.adaptris.core.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;

public abstract class PooledDataSourceImpl<T extends DataSource> implements PooledDataSource {

  protected transient T wrapped;

  public PooledDataSourceImpl(T wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public Connection getConnection() throws SQLException {
    return wrapped.getConnection();
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return wrapped.getConnection(username, password);
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    return wrapped.getLogWriter();
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    wrapped.setLogWriter(out);
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    wrapped.setLoginTimeout(seconds);
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    return wrapped.getLoginTimeout();
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return wrapped.getParentLogger();
  }

  @Override
  public <S> S unwrap(Class<S> iface) throws SQLException {
    return wrapped.unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return wrapped.isWrapperFor(iface);
  }

}
