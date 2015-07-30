/*
 * $RCSfile: JdbcConnectionTest.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/02/09 10:53:25 $
 * $Author: lchan $
 */
package com.adaptris.core.jdbc;

import java.util.concurrent.TimeUnit;

import com.adaptris.util.TimeInterval;

public class JdbcConnectionTest extends DatabaseConnectionCase<JdbcConnection> {

  public JdbcConnectionTest(String arg0) {
    super(arg0);
  }

  @Override
  protected JdbcConnection createConnection() {
    return new JdbcConnection();
  }

  @Override
  protected JdbcConnection configure(JdbcConnection conn1) {
    conn1.setConnectUrl(PROPERTIES.getProperty("jdbc.url"));
    conn1.setDriverImp(PROPERTIES.getProperty("jdbc.driver"));
    conn1.setConnectionAttempts(1);
    conn1.setConnectionRetryInterval(new TimeInterval(10L, TimeUnit.MILLISECONDS.name()));
    return conn1;
  }
}
