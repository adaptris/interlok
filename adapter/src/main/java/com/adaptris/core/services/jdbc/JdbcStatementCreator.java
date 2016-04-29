package com.adaptris.core.services.jdbc;

import com.adaptris.core.AdaptrisMessage;

public interface JdbcStatementCreator {

  String createStatement(AdaptrisMessage msg);
  
}
