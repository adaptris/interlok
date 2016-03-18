package com.adaptris.jdbc;

import java.io.Closeable;

public interface JdbcResultSet extends Closeable {

  public abstract Iterable<JdbcResultRow> getRows();
  
}