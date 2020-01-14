/*
 * Copyright 2018 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.adaptris.jdbc.connection;

import static com.adaptris.jdbc.connection.FailoverConfig.JDBC_ALWAYS_VERIFY;
import static com.adaptris.jdbc.connection.FailoverConfig.JDBC_AUTO_COMMIT;
import static com.adaptris.jdbc.connection.FailoverConfig.JDBC_DEBUG;
import static com.adaptris.jdbc.connection.FailoverConfig.JDBC_DRIVER;
import static com.adaptris.jdbc.connection.FailoverConfig.JDBC_TEST_STATEMENT;
import static com.adaptris.jdbc.connection.FailoverConfig.JDBC_URL_ROOT;
import static com.adaptris.jdbc.connection.FailoverConnectionTest.createTables;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.Executors;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.junit.Test;
import com.adaptris.core.util.JdbcUtil;

public class FailoverDatasourceTest extends FailoverDataSource {

  public FailoverDatasourceTest() {
    super(createProperties());
  }

  @Test(expected = RuntimeException.class)
  public void testProperties() throws Exception {
    FailoverDataSource fds = new FailoverDataSource(null);
  }

  @Test(expected = RuntimeException.class)
  public void testProperties_Empty() throws Exception {
    FailoverDataSource fds = new FailoverDataSource(new Properties());
  }

  @Test
  public void testDataSource() throws Exception {
    FailoverDataSource fds = new FailoverDataSource(createProperties());
    fds.setLoginTimeout(fds.getLoginTimeout());
    fds.setLogWriter(fds.getLogWriter());
    Connection con = fds.getConnection();
    try {
      assertFalse(con.isClosed());
      createTables(con);
    } finally {
      JdbcUtil.closeQuietly(con);
      fds.destroy();
    }
  }

  @Test
  public void testDataSourceWithPassword() throws Exception {
    FailoverDataSource fds = new FailoverDataSource(createProperties());
    Connection con = fds.getConnection("MyUser", "MyPassword");
    try {
      assertFalse(con.isClosed());
      createTables(con);
    } finally {
      JdbcUtil.closeQuietly(con);
      fds.destroy();
    }
  }

  @Test(expected = SQLException.class)
  public void testUnwrap() throws Exception {
    FailoverDataSource fds = new FailoverDataSource(createProperties());
    assertFalse(fds.isWrapperFor(Connection.class));
    fds.unwrap(Connection.class);
  }

  @Test
  public void testParentLogger() throws Exception {
    FailoverDataSource fds = new FailoverDataSource(createProperties());
    assertNull(fds.getParentLogger());
  }

  @Test
  public void testPoolAttendant() throws Exception {
    FailoverConfig cfg = new FailoverConfig(createProperties());
    PoolAttendant p = new PoolAttendant(cfg);
    assertFalse(p.validateObject(null));
    assertFalse(p.validateObject(new FailingProxy()));
    p.destroyObject(new Object());
  }

  @Test(expected = SQLException.class)
  public void testBorrow() throws Exception {
    GenericObjectPool objPool = new GenericObjectPool(new UselessLifeguard(config(), true, false), maxPoolSize(),
        GenericObjectPool.WHEN_EXHAUSTED_BLOCK,
        timeToWait());
    objPool.setTestOnBorrow(true);
    objPool.setTestWhileIdle(true);
    overrideObjectPool(objPool);
    getConnection();
  }

  @Test
  public void testExceptionWrappers() {
    assertEquals(SQLClientInfoException.class, wrapSQLClientInfoException(new SQLClientInfoException()).getClass());
    assertEquals(SQLClientInfoException.class, wrapSQLClientInfoException(new Exception()).getClass());
    assertEquals(SQLException.class, wrapSQLException(new Exception()).getClass());
    assertEquals(SQLException.class, wrapSQLException(new SQLException()).getClass());
  }

  @Test
  public void testStatements() throws Exception {
    Connection conn = new MyProxy();
    try {
      try {
        conn.nativeSQL("SELECT * FROM SEQUENCES");
      } catch (Exception e) {

      }
      try {
        conn.createStatement();
      } catch (Exception e) {

      }
      try {
        conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
      } catch (Exception e) {

      }
      try {
        conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
      } catch (Exception e) {

      }
      try {
        conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE, ResultSet.CLOSE_CURSORS_AT_COMMIT);
      } catch (Exception e) {
        
      }
      try {
        conn.prepareStatement("SELECT * FROM SEQUENCES");
      } catch (Exception e) {

      }
      try {
        conn.prepareStatement("SELECT * FROM SEQUENCES", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      } catch (Exception e) {

      }
      try {
        conn.prepareStatement("SELECT * FROM SEQUENCES", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY,
            ResultSet.CLOSE_CURSORS_AT_COMMIT);
      } catch (Exception e) {

      }
      try {
        conn.prepareStatement("INSERT INTO sequences (id, seq_number) values ('id', 2)", Statement.NO_GENERATED_KEYS);
      } catch (Exception e) {
      }
      try {
        conn.prepareStatement("INSERT INTO sequences (id, seq_number) values ('id', 2)", new int[0]);
      } catch (Exception e) {
      }
      try {
        conn.prepareStatement("INSERT INTO sequences (id, seq_number) values ('id', 2)", new String[0]);
      } catch (Exception e) {

      }

      try {
        conn.prepareCall("SELECT * FROM SEQUENCES");
      } catch (Exception e) {

      }
      try {
        conn.prepareCall("SELECT * FROM SEQUENCES", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      } catch (Exception e) {

      }
      try {
        conn.prepareCall("SELECT * FROM SEQUENCES", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY,
            ResultSet.CLOSE_CURSORS_AT_COMMIT);
      } catch (Exception e) {

      }
    } finally {
      JdbcUtil.closeQuietly(conn);

    }
  }

  @Test
  public void testTypes() throws Exception {
    Connection conn = new MyProxy();

    try {
      try {
        conn.createBlob();
      } catch (Exception e) {

      }
      try {
        conn.createClob();
      } catch (Exception e) {

      }
      try {
        conn.createNClob();
      } catch (Exception e) {

      }
      try {
        conn.createSQLXML();
      } catch (Exception e) {

      }
      try {
        conn.createStruct("java.lang.String", new String[]
        {
            "hello"

        });
      } catch (Exception e) {
      }
      try {
        conn.createArrayOf("java.lang.String", new String[]
        {
            "hello", "world"
        });
      } catch (Exception e) {
      }
    } finally {
      JdbcUtil.closeQuietly(conn);
    }
  }

  @Test
  public void testClientInfo() throws Exception {
    Connection conn = new MyProxy();

    try {
      try {
        conn.setClientInfo(conn.getClientInfo());
      } catch (SQLException e) {

      }
      try {
        conn.setClientInfo("hello", "world");
        conn.getClientInfo("hello");
      } catch (SQLException e) {

      }
    } finally {
      JdbcUtil.closeQuietly(conn);
    }

    conn = new FailingProxy();
    try {
      try {
        conn.setClientInfo(conn.getClientInfo());
      } catch (SQLException e) {

      }
      try {
        conn.setClientInfo("hello", "world");
        conn.getClientInfo("hello");
      } catch (SQLException e) {

      }
    } finally {
      JdbcUtil.closeQuietly(conn);
    }
  }

  @Test
  public void testWrapper() throws Exception {
    Connection conn = new MyProxy();

    try {
      try {
        conn.isWrapperFor(Connection.class);
      } catch (SQLException e) {

      }
      try {
        conn.unwrap(Connection.class);
      } catch (SQLException e) {

      }
    } finally {
      JdbcUtil.closeQuietly(conn);
    }
  }

  @Test
  public void testTypeMap() throws Exception {
    Connection conn = new MyProxy();

    try {
      try {
        conn.setTypeMap(conn.getTypeMap());
      } catch (SQLException e) {

      }
    } finally {
      JdbcUtil.closeQuietly(conn);
    }
  }

  @Test
  public void testInfo() throws Exception {
    Connection conn = new MyProxy();

    try {
      try {
        conn.getMetaData();
      } catch (SQLException e) {

      }
      try {
        conn.setCatalog(conn.getCatalog());
      } catch (SQLException e) {

      }
      try {
        conn.setReadOnly(conn.isReadOnly());
      } catch (SQLException e) {

      }
      try {
        conn.setTransactionIsolation(conn.getTransactionIsolation());
      } catch (SQLException e) {

      }
      try {
        conn.setTransactionIsolation(conn.getTransactionIsolation());
      } catch (SQLException e) {

      }
      try {
        conn.getWarnings();
      } catch (SQLException e) {

      }
      try {
        conn.clearWarnings();
      } catch (SQLException e) {

      }
      try {
        conn.setHoldability(conn.getHoldability());
      } catch (SQLException e) {

      }

      try {
        conn.setSchema(conn.getSchema());
      } catch (SQLException e) {

      }

    } finally {
      JdbcUtil.closeQuietly(conn);
    }

  }

  @Test
  public void testCommitRollback() throws Exception {
    Connection conn = new MyProxy();

    try {
      try {
        conn.setAutoCommit(conn.getAutoCommit());
      } catch (SQLException e) {

      }
      try {
        conn.setAutoCommit(false);
        conn.commit();
      } catch (SQLException e) {

      }

      try {
        conn.setAutoCommit(false);
        conn.rollback();
      } catch (SQLException e) {

      }

      try {
        conn.setAutoCommit(false);
        conn.rollback(conn.setSavepoint());
      } catch (SQLException e) {

      }
      try {
        conn.setAutoCommit(false);
        conn.rollback(conn.setSavepoint("test"));
      } catch (SQLException e) {

      }
      try {
        conn.setAutoCommit(false);
        conn.releaseSavepoint(conn.setSavepoint("test2"));
      } catch (SQLException e) {

      }
    } finally {
      JdbcUtil.closeQuietly(conn);

    }
  }

  @Test
  public void testIsValid() throws Exception {
    Connection conn = new MyProxy();

    try {
      try {
        conn.isValid(1);
      } catch (SQLException e) {

      }
    } finally {
      JdbcUtil.closeQuietly(conn);
    }
  }

  @Test
  public void testAbort() throws Exception {
    Connection conn = new MyProxy();

    try {
      try {
        conn.abort(Executors.newSingleThreadExecutor());
      } catch (SQLException e) {

      }
    } finally {
      JdbcUtil.closeQuietly(conn);
    }
  }

  @Test
  public void testNetworkTimeout() throws Exception {
    Connection conn = new MyProxy();

    try {
      try {
        conn.getNetworkTimeout();
      } catch (SQLException e) {
        // Feature isn't supported by Derby
      }
      try {
        conn.setNetworkTimeout(Executors.newSingleThreadExecutor(), 1000);
      } catch (SQLException e) {
        // Feature isn't supported by Derby
      }
    } finally {
      JdbcUtil.closeQuietly(conn);
    }
  }

  private class FailingProxy extends ConnectionProxy {

    public FailingProxy() throws SQLException {
      super(new FailoverConfig(createProperties()));
    }

    @Override
    Connection getWrappedConnection() throws SQLException {
      throw new SQLException();
    }
  }

  private class MyProxy extends ConnectionProxy {
    private Connection conn;

    public MyProxy() throws Exception {
      super(new FailoverConfig(createProperties()));
      conn = FailoverConnectionTest.connect();
    }

    @Override
    Connection getWrappedConnection() {
      return conn;
    }
  }

  protected static Properties createProperties() {
    Properties p = new Properties();
    p.setProperty(JDBC_DRIVER, "org.apache.derby.jdbc.EmbeddedDriver");
    p.setProperty(JDBC_AUTO_COMMIT, "true");
    p.setProperty(JDBC_DEBUG, "true");
    p.setProperty(JDBC_ALWAYS_VERIFY, "true");
    p.setProperty(JDBC_TEST_STATEMENT, "VALUES CURRENT_TIMESTAMP");
    p.setProperty(JDBC_URL_ROOT + ".1", "jdbc:derby:memory:jdbc-failover-ds-1;create=true");
    p.setProperty(JDBC_URL_ROOT + ".2", "jdbc:derby:memory:jdbc-failover-ds-2;create=true");
    return p;
  }

  
  private class UselessLifeguard extends PoolAttendant {

    private boolean throwOnMake, throwOnPassivate;

    public UselessLifeguard(FailoverConfig cfg, boolean throwOnMake, boolean throwOnPassivate) {
      super(cfg);
      this.throwOnMake = throwOnMake;
      this.throwOnPassivate = throwOnPassivate;
    }

    @Override
    public void passivateObject(Object arg0) throws Exception {
      if (throwOnPassivate) {
        throw new Exception("I'm useless you know");
      }
      super.passivateObject(arg0);
    }

    @Override
    public Object makeObject() throws Exception {
      if (throwOnMake) {
        throw new Exception("I'm useless you know");
      }
      return super.makeObject();
    }
  }
}
