package com.adaptris.core.services.jdbc;

import com.adaptris.core.AdaptrisMessage;

public interface JdbcStatementCreator {

  public abstract String createStatement(AdaptrisMessage msg);
  
}
