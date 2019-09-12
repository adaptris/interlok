package com.adaptris.core.jdbc;

import com.mchange.v2.c3p0.ComboPooledDataSource;

@FunctionalInterface
public interface JdbcPoolFactory {

  ComboPooledDataSource create() throws Exception;
}
