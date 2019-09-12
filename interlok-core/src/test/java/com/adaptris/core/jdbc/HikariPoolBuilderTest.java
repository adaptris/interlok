package com.adaptris.core.jdbc;

import static com.adaptris.core.jdbc.DefaultPoolFactoryTest.createConnection;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.adaptris.core.jdbc.HikariPoolBuilder.HikariDataSourceWrapper;
import com.zaxxer.hikari.HikariDataSource;

public class HikariPoolBuilderTest {

  @Test
  public void testPoolBuilder() throws Exception {
    HikariPoolBuilder factory = new HikariPoolBuilder();
    PluggableJdbcPooledConnection con = createConnection();
    HikariDataSourceWrapper pooledDs = (HikariDataSourceWrapper) factory.build(con);
    HikariDataSource wrapped = pooledDs.wrapped();
    assertEquals(con.getConnectUrl(), wrapped.getJdbcUrl());
  }

}
