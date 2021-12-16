/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Properties;
import org.junit.Test;
import org.mockito.Mockito;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.jdbc.JdbcConnection;
import com.adaptris.core.jdbc.JdbcConstants;
import com.adaptris.security.exc.PasswordException;

// This is all just a bit of fakery to get 100% (ha ha).
public class JdbcUtilTest extends JdbcUtil {


  @Test
  public void testClose_AutoCloseable() throws Exception {
    closeQuietly((AutoCloseable[]) null);
    AutoCloseable mock = mock(AutoCloseable.class);
    closeQuietly(null, mock);
    doThrow(new IOException("Expected")).when(mock).close();
    closeQuietly(mock);
  }

  @Test
  public void testRollbackSavepointConnection() throws Exception {
    rollback((Savepoint) null, (Connection) null);
    Savepoint savepoint = mock(Savepoint.class);
    Connection c = mock(Connection.class);
    when(c.getAutoCommit()).thenReturn(true);
    rollback(savepoint, c);
    when(c.getAutoCommit()).thenReturn(false);
    rollback(null, c);
    rollback(savepoint, c);
    doThrow(new SQLException("Expected")).when(c).rollback(any(Savepoint.class));
    rollback(savepoint, c);
  }

  @Test
  public void testRollbackConnection() throws Exception {
    rollback(null);
    Connection c = mock(Connection.class);
    when(c.getAutoCommit()).thenReturn(true);
    rollback(c);
    when(c.getAutoCommit()).thenReturn(false);
    rollback(c);
    doThrow(new SQLException("Expected")).when(c).rollback();
    rollback(c);
  }

  @Test
  public void testCommit() throws Exception {
    commit(null);
    Connection c = mock(Connection.class);
    when(c.getAutoCommit()).thenReturn(true);
    commit(c);
    when(c.getAutoCommit()).thenReturn(false);
    commit(c);
    try {
      doThrow(new SQLException("Expected")).when(c).commit();
      commit(c);
      fail();
    } catch (SQLException expected) {

    }
  }

  @Test
  public void testCreateSavepoint() throws Exception {
    assertNull(createSavepoint(null));
    Connection c = mock(Connection.class);
    Savepoint savepoint = mock(Savepoint.class);

    when(c.getAutoCommit()).thenReturn(true);
    assertNull(createSavepoint(c));

    when(c.getAutoCommit()).thenReturn(false);
    when(c.setSavepoint()).thenReturn(savepoint);
    assertNotNull(createSavepoint(c));

    try {
      doThrow(new SQLException("Expected")).when(c).setSavepoint();
      createSavepoint(c);
      fail();
    } catch (SQLException expected) {

    }
  }

  @Test(expected = PasswordException.class)
  public void testMergeProperties() throws Exception {
    Properties p = new Properties();
    mergeConnectionProperties(p, "user", "password");
    assertEquals("user", p.getProperty("user"));
    assertEquals("password", p.getProperty("password"));
    p = new Properties();
    mergeConnectionProperties(p, "", "");
    assertEquals(0, p.size());
    mergeConnectionProperties(p, "", "SEED:ABCDEFGGHJ");
  }

  @Test
  public void testTestConnection() throws Exception {
    Connection c = mock(Connection.class);
    when(c.isValid(anyInt())).thenReturn(Boolean.TRUE);

    testConnection(c, false); // won't fail because of the first isEmptyCheck
    testConnection(c, true);
  }


  @Test
  public void testTestConnection_NotValid() throws Exception {
    Connection c = mock(Connection.class);

    when(c.isValid(anyInt())).thenReturn(Boolean.FALSE);
    testConnection(c, false);
    try {
      testConnection(c, true);
      fail();
    } catch (SQLException expected) {

    }
  }


  @Test
  public void testGetConnection() throws Exception {
    Connection metadata = mock(Connection.class);
    Connection closed = mock(Connection.class);
    Connection configured = mock(Connection.class);
    Mockito.when(configured.getAutoCommit()).thenReturn(true);
    Mockito.when(closed.isClosed()).thenReturn(true);

    JdbcConnection jdbcCon = new MyJdbcConnection(configured);
    try {
      LifecycleHelper.initAndStart(jdbcCon);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

      msg.getObjectHeaders().put(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY, metadata);
      assertEquals(metadata, getConnection(msg, jdbcCon));

      msg.getObjectHeaders().put(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY, closed);
      assertEquals(configured, getConnection(msg, jdbcCon));

      msg.getObjectHeaders().clear();
      assertEquals(configured, getConnection(msg, jdbcCon));
    } finally {
      LifecycleHelper.stopAndClose(jdbcCon);
    }
  }

  @Test
  public void testRollback_Message() throws Exception {
    Connection jdbcCon = mock(Connection.class);
    Mockito.when(jdbcCon.getAutoCommit()).thenReturn(false);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.getObjectHeaders().put(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY, jdbcCon);
    rollback(jdbcCon, msg);
    Mockito.verify(jdbcCon, Mockito.times(0)).rollback();
    msg.getObjectHeaders().clear();
    rollback(jdbcCon, msg);
    Mockito.verify(jdbcCon, Mockito.atLeast(1)).rollback();
  }

  @Test
  public void testCommit_Message() throws Exception {
    Connection jdbcCon = mock(Connection.class);
    Mockito.when(jdbcCon.getAutoCommit()).thenReturn(false);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.getObjectHeaders().put(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY, jdbcCon);
    commit(jdbcCon, msg);
    Mockito.verify(jdbcCon, Mockito.times(0)).commit();
    msg.getObjectHeaders().clear();
    commit(jdbcCon, msg);
    Mockito.verify(jdbcCon, Mockito.atLeast(1)).commit();
  }

  private class MyJdbcConnection extends JdbcConnection {
    private Connection jdbcCon;

    private MyJdbcConnection(Connection c) throws Exception {
      jdbcCon = c;
    }

    @Override
    protected Connection makeConnection() throws SQLException {
      return jdbcCon;
    }
  }
}
