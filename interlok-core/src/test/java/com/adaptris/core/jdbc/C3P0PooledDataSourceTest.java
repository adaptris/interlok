package com.adaptris.core.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.sql.DataSource;
import org.junit.Test;
import com.adaptris.util.GuidGenerator;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class C3P0PooledDataSourceTest {
  private static final GuidGenerator GUID = new GuidGenerator();
  private static final String DRIVER_IMP = "org.apache.derby.jdbc.EmbeddedDriver";

  @Test
  public void testGetConnection() throws Exception {
    try (C3P0PooledDataSource ds = new C3P0PooledDataSource(create())) {
      assertNotNull(ds.getConnection());
    }
  }

  @Test
  public void testGetConnection_Username() throws Exception {
    try (C3P0PooledDataSource ds = new C3P0PooledDataSource(create())) {
      assertNotNull(ds.getConnection("username", "password"));
    }
  }


  @Test
  public void testLogWriter() throws Exception {
    PrintWriter pw = new PrintWriter(new StringWriter());
    try (C3P0PooledDataSource ds = new C3P0PooledDataSource(create())) {
      ds.getParentLogger();
      ds.setLogWriter(pw);
      assertEquals(pw, ds.getLogWriter());
    }
  }

  @Test
  public void testWrapper() throws Exception {
    PrintWriter pw = new PrintWriter(new StringWriter());
    try (C3P0PooledDataSource ds = new C3P0PooledDataSource(create())) {
      assertTrue(ds.isWrapperFor(DataSource.class));
      ds.unwrap(DataSource.class);
    }
  }


  @Test
  public void testLoginTimeout() throws Exception {
    try (C3P0PooledDataSource ds = new C3P0PooledDataSource(create())) {
      ds.setLoginTimeout(10);
      assertEquals(10, ds.getLoginTimeout());
    }
  }


  private ComboPooledDataSource create() throws Exception {
    ComboPooledDataSource pool = new ComboPooledDataSource();
    String url = "jdbc:derby:memory:" + GUID.safeUUID() + ";create=true";
    pool.setDriverClass(DRIVER_IMP);
    pool.setJdbcUrl(url);
    return pool;
  }

}
