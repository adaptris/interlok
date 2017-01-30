package com.adaptris.core.jdbc;

import com.adaptris.core.CoreException;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public interface JdbcPoolFactory {

  ComboPooledDataSource create(JdbcPoolConfiguration cfg) throws CoreException;
}
