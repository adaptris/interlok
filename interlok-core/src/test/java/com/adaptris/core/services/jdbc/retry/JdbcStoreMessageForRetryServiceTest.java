package com.adaptris.core.services.jdbc.retry;

import com.adaptris.core.jdbc.JdbcService;

public class JdbcStoreMessageForRetryServiceTest extends JdbcRetryServiceCase {

  @Override
  protected JdbcService createService() {
    return new JdbcStoreMessageForRetryService();
  }

}