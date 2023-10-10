package com.adaptris.core.services.jdbc.retry;

public class JdbcAcknowledgeServiceTest extends JdbcRetryServiceCase {
  
  @Override
  protected JdbcAcknowledgeService createService() {
    return new JdbcAcknowledgeService();
  }

}