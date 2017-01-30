package com.adaptris.core.jdbc;

public interface JdbcPoolConfiguration {

  int maxPoolSize();

  int minPoolSize();

  int maxIdleTime();

  int idleConnectionTestPeriod();

  int connectionAcquireWait();

  int acquireIncrement();

  int connectionAttempts();

  boolean alwaysValidateConnection();

  long connectionRetryInterval();
}
