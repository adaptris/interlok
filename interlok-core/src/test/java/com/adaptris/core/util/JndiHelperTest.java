/*
 * Copyright 2015 Adaptris Ltd.
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.CoreException;
import com.adaptris.core.JndiContextFactory;
import com.adaptris.core.NullConnection;
import com.adaptris.core.jdbc.JdbcPooledConnection;
import com.adaptris.core.transaction.DummyTransactionManager;
import com.adaptris.core.transaction.TransactionManager;
import com.adaptris.util.GuidGenerator;

public class JndiHelperTest extends com.adaptris.interlok.junit.scaffolding.BaseCase {
  private Properties env = new Properties();

  @BeforeEach
  public void setUp() throws Exception {
    env.put(Context.INITIAL_CONTEXT_FACTORY, JndiContextFactory.class.getName());
  }

  @Test
  public void testBindCollection() throws Exception {
    NullConnection connection = new NullConnection();
    connection.setUniqueId(getName());
    ArrayList<AdaptrisConnection> connectionList = new ArrayList<AdaptrisConnection>();
    connectionList.add(connection);
    InitialContext initialContext = new InitialContext(env);
    try {
      JndiHelper.bind(connectionList);
      NullConnection lookedup = (NullConnection) initialContext.lookup("adapter:comp/env/" + getName());
      assertNotNull(lookedup);
      assertTrue(lookedup instanceof NullConnection);
      assertEquals(getName(), lookedup.getUniqueId());
    }
    finally {
      JndiHelper.unbindQuietly(initialContext, connectionList, true);
    }
  }

  @Test
  public void testBindCollection_Debug() throws Exception {
    NullConnection connection = new NullConnection();
    connection.setUniqueId(getName());
    ArrayList<AdaptrisConnection> connectionList = new ArrayList<AdaptrisConnection>();
    connectionList.add(connection);
    InitialContext initialContext = new InitialContext(env);
    try {
      JndiHelper.bind(connectionList, true);
      NullConnection lookedup = (NullConnection) initialContext.lookup("adapter:comp/env/" + getName());
      assertNotNull(lookedup);
      assertTrue(lookedup instanceof NullConnection);
      assertEquals(getName(), lookedup.getUniqueId());
    }
    finally {
      JndiHelper.unbindQuietly(initialContext, connectionList, true);
    }
  }

  @Test
  public void testBindCollection_WithContext() throws Exception {
    NullConnection connection = new NullConnection();
    connection.setUniqueId(getName());
    ArrayList<AdaptrisConnection> connectionList = new ArrayList<AdaptrisConnection>();
    connectionList.add(connection);
    InitialContext initialContext = new InitialContext(env);
    try {
      JndiHelper.bind(initialContext, connectionList, true);
      NullConnection lookedup = (NullConnection) initialContext.lookup("adapter:comp/env/" + getName());
      assertNotNull(lookedup);
      assertTrue(lookedup instanceof NullConnection);
      assertEquals(getName(), lookedup.getUniqueId());
    }
    finally {
      JndiHelper.unbindQuietly(initialContext, connectionList, true);
    }
  }

  @Test
  public void testBindObject() throws Exception {
    NullConnection connection = new NullConnection();
    connection.setUniqueId(getName());
    InitialContext initialContext = new InitialContext(env);
    try {
      JndiHelper.bind(connection, false);
      NullConnection lookedup = (NullConnection) initialContext.lookup("adapter:comp/env/" + getName());
      assertNotNull(lookedup);
      assertEquals(getName(), lookedup.getUniqueId());
    }
    finally {
      JndiHelper.unbindQuietly(initialContext, connection, false);
    }
  }

  @Test
  public void testBindObject_WithContext() throws Exception {
    NullConnection connection = new NullConnection();
    connection.setUniqueId(getName());
    InitialContext initialContext = new InitialContext(env);
    try {
      JndiHelper.bind(initialContext, connection, false);
      NullConnection lookedup = (NullConnection) initialContext.lookup("adapter:comp/env/" + getName());
      assertNotNull(lookedup);
      assertEquals(getName(), lookedup.getUniqueId());
      // won't fail, should just get ignored.
      JndiHelper.bind(initialContext, (AdaptrisConnection) null, true);
      JndiHelper.bind(initialContext, (AdaptrisConnection) null, false);
    }
    finally {
      JndiHelper.unbindQuietly(initialContext, connection, false);
    }
  }

  @Test
  public void testBindObject_WithScheme() throws Exception {
    NullConnection connection = new NullConnection();
    connection.setUniqueId("adapter:" + getName());
    InitialContext initialContext = new InitialContext(env);
    try {
      JndiHelper.bind(initialContext, connection, false);
      NullConnection lookedup = (NullConnection) initialContext.lookup("adapter:comp/env/" + getName());
      assertNotNull(lookedup);
      assertEquals("adapter:" + getName(), lookedup.getUniqueId());
    }
    finally {
      JndiHelper.unbindQuietly(initialContext, connection, false);
    }
  }


  @Test
  public void testBindObject_AlreadyBound() throws Exception {
    NullConnection connection = new NullConnection();
    connection.setUniqueId(getName());
    InitialContext initialContext = new InitialContext(env);
    JndiHelper.bind(initialContext, connection, false);
    try {
      JndiHelper.bind(initialContext, connection, true);
      fail();
    }
    catch (CoreException expected) {
      ;
    }
    try {
      JndiHelper.bind(initialContext, connection, false);
      fail();
    }
    catch (CoreException expected) {
      ;
    }
    JndiHelper.unbindQuietly(initialContext, connection, false);
  }

  @Test
  public void testBindTransactionManager_AlreadyBound() throws Exception {
    TransactionManager transactionManager = new DummyTransactionManager(getName());
    InitialContext initialContext = new InitialContext(env);
    JndiHelper.bind(initialContext, transactionManager, false);
    try {
      JndiHelper.bind(initialContext, transactionManager, true);
      fail();
    }
    catch (CoreException expected) {
      ;
    }
    try {
      JndiHelper.bind(initialContext, transactionManager, false);
      fail();
    }
    catch (CoreException expected) {
      ;
    }
    JndiHelper.unbindQuietly(initialContext, transactionManager, false);
  }

  @Test
  public void testBindNullTransactionManager() throws Exception {
    TransactionManager transactionManager = null;
    InitialContext initialContext = new InitialContext(env);
    try {
      JndiHelper.bind(initialContext, transactionManager, false);
    }
    catch (CoreException expected) {
      fail("Should not error, just ignore.");
    }
  }

  @Test
  public void testBindNullTransactionManager_Debug() throws Exception {
    TransactionManager transactionManager = null;
    InitialContext initialContext = new InitialContext(env);
    try {
      JndiHelper.bind(initialContext, transactionManager, true);
    }
    catch (CoreException expected) {
      fail("Should not error, just ignore.");
    }
  }

  @Test
  public void testUnbindNullTransactionManager() throws Exception {
    TransactionManager transactionManager = null;
    try {
      JndiHelper.unbind(transactionManager, false);
    }
    catch (CoreException expected) {
      fail("Should not error, just ignore.");
    }
  }

  @Test
  public void testUnbindNullTransactionManager_Debug() throws Exception {
    TransactionManager transactionManager = null;
    try {
      JndiHelper.unbind(transactionManager, true);
    }
    catch (CoreException expected) {
      fail("Should not error, just ignore.");
    }
  }

  @Test
  public void testUnbindUnboundTransactionManager() throws Exception {
    TransactionManager transactionManager = new DummyTransactionManager(getName());
    try {
      JndiHelper.unbind(transactionManager, false);
      fail();
    }
    catch (CoreException expected) {
      // not previously bound, so should error.
    }
  }

  @Test
  public void testUnbindUnboundTransactionManager_Debug() throws Exception {
    TransactionManager transactionManager = new DummyTransactionManager(getName());
    try {
      JndiHelper.unbind(transactionManager, true);
      fail();
    }
    catch (CoreException expected) {
      // not previously bound, so should error.
    }
  }

  @Test
  public void testBindJdbcConnection() throws Exception {
    JdbcPooledConnection connection = new JdbcPooledConnection();
    connection.setConnectUrl("jdbc:derby:memory:" + new GuidGenerator().safeUUID() + ";create=true");
    connection.setDriverImp("org.apache.derby.jdbc.EmbeddedDriver");
    connection.setMinimumPoolSize(1);
    connection.setAcquireIncrement(1);
    connection.setMaximumPoolSize(7);
    connection.setUniqueId(getName());
    InitialContext initialContext = new InitialContext(env);
    try {
      JndiHelper.bind(initialContext, connection, true);
      JdbcPooledConnection lookedup = (JdbcPooledConnection) initialContext.lookup("adapter:comp/env/" + getName());
      assertEquals(getName(), lookedup.getUniqueId());
      assertNotNull(initialContext.lookup("adapter:comp/env/jdbc/" + getName()));
    }
    finally {
      JndiHelper.unbindQuietly(initialContext, connection, true);
    }
    try {
      JndiHelper.bind(initialContext, connection, false);
      JdbcPooledConnection lookedup = (JdbcPooledConnection) initialContext.lookup("adapter:comp/env/" + getName());
      assertEquals(getName(), lookedup.getUniqueId());
      assertNotNull(initialContext.lookup("adapter:comp/env/jdbc/" + getName()));
    }
    finally {
      JndiHelper.unbindQuietly(initialContext, connection, false);
    }
  }

  @Test
  public void testUnbindCollection() throws Exception {
    NullConnection connection = new NullConnection();
    connection.setUniqueId(getName());
    ArrayList<AdaptrisConnection> connectionList = new ArrayList<AdaptrisConnection>();
    connectionList.add(connection);
    JndiHelper.bind(connectionList);
    JndiHelper.unbind(connectionList, false);
    try {
      JndiHelper.unbind(connectionList, false);
      fail();
    }
    catch (CoreException expected) {

    }
  }

  @Test
  public void testUnbindCollection_Debug() throws Exception {
    NullConnection connection = new NullConnection();
    connection.setUniqueId(getName());
    ArrayList<AdaptrisConnection> connectionList = new ArrayList<AdaptrisConnection>();
    connectionList.add(connection);
    InitialContext initialContext = new InitialContext(env);
    JndiHelper.bind(connectionList);
    JndiHelper.unbind(connectionList, true);
    try {
      JndiHelper.unbind(connectionList, true);
      fail();
    }
    catch (CoreException expected) {

    }
  }

  @Test
  public void testUnbindCollection_WithContext() throws Exception {
    NullConnection connection = new NullConnection();
    connection.setUniqueId(getName());
    ArrayList<AdaptrisConnection> connectionList = new ArrayList<AdaptrisConnection>();
    connectionList.add(connection);
    InitialContext initialContext = new InitialContext(env);
    JndiHelper.bind(initialContext, connectionList, true);
    JndiHelper.unbind(initialContext, connectionList, true);
    try {
      JndiHelper.unbind(initialContext, connectionList, true);
      fail();
    }
    catch (CoreException expected) {

    }
  }

  @Test
  public void testUnbindObject() throws Exception {
    NullConnection connection = new NullConnection();
    connection.setUniqueId(getName());
    InitialContext initialContext = new InitialContext(env);
    JndiHelper.bind(connection, false);
    JndiHelper.unbind(connection, false);
  }

  @Test
  public void testUnbindObject_WithContext() throws Exception {
    NullConnection connection = new NullConnection();
    connection.setUniqueId(getName());
    InitialContext initialContext = new InitialContext(env);
    JndiHelper.bind(initialContext, connection, false);
    JndiHelper.unbind(initialContext, (AdaptrisConnection) null, true);
    JndiHelper.unbind(initialContext, (AdaptrisConnection) null, false);
    JndiHelper.unbind(initialContext, connection, false);
  }

  @Test
  public void testUnbindJdbcConnection() throws Exception {
    JdbcPooledConnection connection = new JdbcPooledConnection();
    connection.setConnectUrl("jdbc:derby:memory:" + new GuidGenerator().safeUUID() + ";create=true");
    connection.setDriverImp("org.apache.derby.jdbc.EmbeddedDriver");
    connection.setMinimumPoolSize(1);
    connection.setAcquireIncrement(1);
    connection.setMaximumPoolSize(7);
    connection.setUniqueId(getName());
    InitialContext initialContext = new InitialContext(env);
    JndiHelper.bind(initialContext, connection, true);
    JndiHelper.unbind(initialContext, connection, true);
    JndiHelper.bind(initialContext, connection, false);
    JndiHelper.unbind(initialContext, connection, false);
  }

}
