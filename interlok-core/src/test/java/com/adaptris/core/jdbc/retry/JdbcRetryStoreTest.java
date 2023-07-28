package com.adaptris.core.jdbc.retry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

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
  }

}
