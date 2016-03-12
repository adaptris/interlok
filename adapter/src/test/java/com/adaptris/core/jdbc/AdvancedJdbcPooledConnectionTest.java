package com.adaptris.core.jdbc;

import java.sql.Connection;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.TimeInterval;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class AdvancedJdbcPooledConnectionTest extends DatabaseConnectionCase<AdvancedJdbcPooledConnection>{
  
  private static final GuidGenerator GUID = new GuidGenerator();

  public AdvancedJdbcPooledConnectionTest(String arg0) {
    super(arg0);
  }

  @Override
  protected AdvancedJdbcPooledConnection createConnection() {
    return new AdvancedJdbcPooledConnection();
  }

  @Override
  protected AdvancedJdbcPooledConnection configure(AdvancedJdbcPooledConnection conn1) {
    conn1.setConnectUrl(PROPERTIES.getProperty("jdbc.url"));
    conn1.setDriverImp(PROPERTIES.getProperty("jdbc.driver"));
    conn1.setConnectionAttempts(1);
    conn1.setConnectionRetryInterval(new TimeInterval(10L, TimeUnit.MILLISECONDS.name()));
    conn1.getConnectionPoolProperties().add(new KeyValuePair(PooledConnectionProperties.acquireIncrement.name(), "5"));
    conn1.getConnectionPoolProperties().add(new KeyValuePair(PooledConnectionProperties.minPoolSize.name(), "10"));
    conn1.getConnectionPoolProperties().add(new KeyValuePair(PooledConnectionProperties.maxPoolSize.name(), "50"));
    return conn1;
  }

//INTERLOK-107
 public void testConnectionDataSource_Poolsize() throws Exception {
   String originalThread = Thread.currentThread().getName();
   Thread.currentThread().setName("testConnectionDataSource_Poolsize");    
       
   AdvancedJdbcPooledConnection con = configure(createConnection());
   con.setConnectUrl("jdbc:derby:memory:" + GUID.safeUUID() + ";create=true");
   con.setDriverImp("org.apache.derby.jdbc.EmbeddedDriver");
   con.getConnectionPoolProperties().add(new KeyValuePair(PooledConnectionProperties.acquireIncrement.name(), "5"));
   con.getConnectionPoolProperties().add(new KeyValuePair(PooledConnectionProperties.minPoolSize.name(), "10"));
   con.getConnectionPoolProperties().add(new KeyValuePair(PooledConnectionProperties.maxPoolSize.name(), "50"));
   con.getConnectionPoolProperties().add(new KeyValuePair(PooledConnectionProperties.checkoutTimeout.name(), "30000"));
   
   try {
     LifecycleHelper.init(con);
     LifecycleHelper.start(con);
     ComboPooledDataSource poolDs = (ComboPooledDataSource) con.asDataSource();
     assertEquals(0, poolDs.getNumBusyConnections());
     Connection c1 = null;
     try {
       c1 = poolDs.getConnection();
       log.info("1 get: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections() + ", NumIdleConnections" + poolDs.getNumIdleConnections());
     } finally {
       Thread.sleep(1500);
       c1.close();
     }
     log.info("1 close: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections() + ", NumIdleConnections" + poolDs.getNumIdleConnections());
     c1 = poolDs.getConnection();
     log.info("1 get (again): NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections() + ", NumIdleConnections" + poolDs.getNumIdleConnections());
     Connection c2 = poolDs.getConnection();
     log.info("2 get: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections() + ", NumIdleConnections" + poolDs.getNumIdleConnections());
     Connection c3 = poolDs.getConnection();
     log.info("3 get: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections() + ", NumIdleConnections" + poolDs.getNumIdleConnections());
     Connection c4 = poolDs.getConnection();
     log.info("4 get: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections() + ", NumIdleConnections" + poolDs.getNumIdleConnections());
     Connection c5 = poolDs.getConnection();
     log.info("5 get: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections() + ", NumIdleConnections" + poolDs.getNumIdleConnections());
     Connection c6 = poolDs.getConnection();
     log.info("6 get: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections() + ", NumIdleConnections" + poolDs.getNumIdleConnections());

     Connection c7 = poolDs.getConnection();
     log.info("7 get: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections() + ", NumIdleConnections" + poolDs.getNumIdleConnections());

     assertEquals(7, poolDs.getNumBusyConnections());
     c1.close();
     c2.close();
     c3.close();
     c4.close();
     c5.close();
     c6.close();
     c7.close();
     Thread.sleep(2000);
     log.info("closed: NumConnections=" + poolDs.getNumConnections() + ", NumBusyConnnections=" + poolDs.getNumBusyConnections() + ", NumIdleConnections" + poolDs.getNumIdleConnections());

     assertEquals(0, poolDs.getNumBusyConnections());
   }
   finally {
     Thread.currentThread().setName(originalThread);   
     LifecycleHelper.stop(con);
     LifecycleHelper.close(con);
   }

 }
}
