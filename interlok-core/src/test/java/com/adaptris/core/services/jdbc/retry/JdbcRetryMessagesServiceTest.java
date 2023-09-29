package com.adaptris.core.services.jdbc.retry;

public class JdbcRetryMessagesServiceTest extends JdbcRetryServiceCase {
  
  @Override
  protected JdbcRetryMessagesService createService() {
    return new JdbcRetryMessagesService();
  }

}