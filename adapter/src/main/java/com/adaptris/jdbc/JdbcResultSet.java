package com.adaptris.jdbc;

public interface JdbcResultSet {

  public abstract Iterable<JdbcResultRow> getRows();

}