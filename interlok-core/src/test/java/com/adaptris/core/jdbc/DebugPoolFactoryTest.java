package com.adaptris.core.jdbc;

import static com.adaptris.core.jdbc.DefaultPoolFactoryTest.createConnection;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.adaptris.util.TimeInterval;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DebugPoolFactoryTest {
  @Test
  public void testCreate() throws Exception {
    DebugPoolFactory factory = new DebugPoolFactory().withDebugUnreturnedConnectionStackTraces(true)
        .withUnreturnedConnectionTimeout(new TimeInterval(10L, TimeUnit.SECONDS));;
    ComboPooledDataSource pooledDS = factory.create();
    assertTrue(pooledDS.isDebugUnreturnedConnectionStackTraces());
    assertEquals(10, pooledDS.getUnreturnedConnectionTimeout());

  }

  @Test
  public void testPoolBuilder() throws Exception {
    DebugPoolFactory factory = new DebugPoolFactory().withDebugUnreturnedConnectionStackTraces(true)
        .withUnreturnedConnectionTimeout(new TimeInterval(10L, TimeUnit.SECONDS));;
    PluggableJdbcPooledConnection con = createConnection();
    C3P0PooledDataSource pooledDs = factory.build(con);
    ComboPooledDataSource wrapped = pooledDs.wrapped();
    assertEquals(con.getConnectUrl(), wrapped.getJdbcUrl());
    assertTrue(wrapped.isDebugUnreturnedConnectionStackTraces());
    assertEquals(10, wrapped.getUnreturnedConnectionTimeout());
  }
}
