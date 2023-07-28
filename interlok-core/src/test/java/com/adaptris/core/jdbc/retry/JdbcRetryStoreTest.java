package com.adaptris.core.jdbc.retry;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.interlok.junit.scaffolding.BaseCase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class JdbcRetryStoreTest extends BaseCase {

  protected static final String JDBC_RETRY_STORE_DRIVER = "jdbc.retrystore.driver";
  protected static final String JDBC_RETRY_STORE_URL = "jdbc.retrystore.url";

  private JdbcRetryStore jdbcRetryStore;
  private Connection sqlConnection;

  public JdbcRetryStoreTest() throws Exception {
    jdbcRetryStore = new JdbcRetryStore();
    jdbcRetryStore.makeConnection(null);
    createConnection();
  }

  @BeforeAll
  public void setUp() throws Exception {
    sqlConnection = createConnection();
    jdbcRetryStore.setConnection((AdaptrisConnection) sqlConnection);
    jdbcRetryStore.init();
  }

  @AfterAll
  public void tearDown() throws Exception {
    sqlConnection.close();
  }

  @Test
  public void testLoadingSqlPropertiesFile() {
    jdbcRetryStore.getSqlPropertiesFile();
  }
  
  protected Connection createConnection() throws Exception {
    Connection c = null;
    Class.forName(PROPERTIES.getProperty(JDBC_RETRY_STORE_DRIVER));
    c = DriverManager.getConnection(PROPERTIES.getProperty(JDBC_RETRY_STORE_URL));
    c.setAutoCommit(true);
    return c;
  }

}
