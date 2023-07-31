package com.adaptris.core.jdbc.retry;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.adaptris.interlok.junit.scaffolding.BaseCase;

import java.sql.Connection;
import java.sql.DriverManager;

@TestInstance(Lifecycle.PER_CLASS)
public class JdbcRetryStoreTest extends BaseCase {

  protected static final String JDBC_RETRY_STORE_DRIVER = "jdbc.retrystore.driver";
  protected static final String JDBC_RETRY_STORE_URL = "jdbc.retrystore.url";
  protected static final String JDBC_RETRY_STORE_PROPERTIES = "jdbc.RetryStore.properties.file.destination";

  private JdbcRetryStore jdbcRetryStore;
  private Connection sqlConnection;

}