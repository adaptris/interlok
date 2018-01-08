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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

import org.junit.Test;

// This is all just a bit of fakery to get 100% (ha ha).
public class JdbcUtilTest extends JdbcUtil {


  @Test
  public void testCloseStatement() throws Exception {
    closeQuietly((Statement) null);
    Statement mock = mock(Statement.class);
    closeQuietly(mock);
    doThrow(new SQLException("Expected")).when(mock).close();
    closeQuietly(mock);
  }


  @Test
  public void testCloseResultSet() throws Exception {
    closeQuietly((ResultSet) null);
    ResultSet mock = mock(ResultSet.class);
    closeQuietly(mock);
    doThrow(new SQLException("Expected")).when(mock).close();
    closeQuietly(mock);
  }

  @Test
  public void testCloseCloseable() throws Exception {
    closeQuietly((Closeable) null);
    Closeable mock = mock(Closeable.class);
    closeQuietly(mock);
    doThrow(new IOException("Expected")).when(mock).close();
    closeQuietly(mock);
  }

  @Test
  public void testCloseConnection() throws Exception {
    closeQuietly((Connection) null);
    Connection mock = mock(Connection.class);
    closeQuietly(mock);
    doThrow(new SQLException("Expected")).when(mock).close();
    closeQuietly(mock);
  }

  @Test
  public void testRollbackSavepointConnection() throws Exception {
    rollback(null, null);
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
}
