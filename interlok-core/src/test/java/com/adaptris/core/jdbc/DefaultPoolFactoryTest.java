package com.adaptris.core.jdbc;

import static org.junit.Assert.assertEquals;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.TimeInterval;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DefaultPoolFactoryTest {
  private static final GuidGenerator GUID = new GuidGenerator();
  private static final String DRIVER_IMP = "org.apache.derby.jdbc.EmbeddedDriver";

  @Test
  public void testCreate() throws Exception {
    DefaultPoolFactory factory = new DefaultPoolFactory();
    ComboPooledDataSource pooledDS = factory.create();
  }

  @Test
  public void testPoolBuilder() throws Exception {
    DefaultPoolFactory factory = new DefaultPoolFactory();
    PluggableJdbcPooledConnection con = createConnection();
    C3P0PooledDataSource pooledDs = factory.build(con);
    ComboPooledDataSource wrapped = pooledDs.wrapped();
    assertEquals(con.getConnectUrl(), wrapped.getJdbcUrl());
  }


  public static PluggableJdbcPooledConnection createConnection() throws Exception {
    String url = "jdbc:derby:memory:" + GUID.safeUUID() + ";create=true";
    PluggableJdbcPooledConnection con = new PluggableJdbcPooledConnection();
    con.setConnectUrl(url);
    con.setDriverImp(DRIVER_IMP);
    con.setConnectionAttempts(1);
    con.setConnectionRetryInterval(new TimeInterval(10L, TimeUnit.MILLISECONDS.name()));
    return con;
  }
}
