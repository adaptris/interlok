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

package com.adaptris.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.ftp.FtpConnection;
import com.adaptris.core.http.jetty.HttpConnection;
import com.adaptris.core.jdbc.MockJdbcConnection;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.management.Constants;
import com.adaptris.core.management.SystemPropertiesUtil;
import com.adaptris.core.util.JndiHelper;
import com.adaptris.naming.adapter.NamingContext;

public class JndiContextFactoryTest {

  private Properties env = new Properties();
  private Properties bootstrapProps = new Properties();

  @BeforeEach
  public void setUp() throws Exception {
    env.put(Context.INITIAL_CONTEXT_FACTORY, JndiContextFactory.class.getName());

    bootstrapProps.put(Constants.CFG_KEY_JNDI_SERVER, "true");
  }

  @Test
  public void testMergeEnvs() throws Exception {
    Properties src = new Properties();
    Properties dest = new Properties();
    src.put("hello", "world");
    src.put("a", "b");
    src.put("1", "2");
    dest.put("a", "z");
    Hashtable result = JndiContextFactory.merge(src, dest);
    assertTrue(result.containsKey("hello"));
    assertEquals("z", result.get("a").toString());
  }

  @Test
  public void testAddAdapterScheme() throws Exception {
    Hashtable<String, Object> env1 = new Hashtable<>();
    Hashtable<String, Object> result = JndiContextFactory.addAdapterSchemePackage(env1);
    assertTrue(result.containsKey(Context.URL_PKG_PREFIXES));

    Hashtable<String, Object> env2 = new Hashtable<>();
    env2.put(Context.URL_PKG_PREFIXES, "com.adaptris.alt.naming");
    result = JndiContextFactory.addAdapterSchemePackage(env2);
    String pkgs = (String) result.get(Context.URL_PKG_PREFIXES);
    assertEquals("com.adaptris.alt.naming:com.adaptris.naming", pkgs);

    Hashtable<String, Object> env3 = new Hashtable<>();
    env3.put(Context.URL_PKG_PREFIXES, "com.adaptris.naming");
    result = JndiContextFactory.addAdapterSchemePackage(env3);
    pkgs = (String) result.get(Context.URL_PKG_PREFIXES);
    assertEquals("com.adaptris.naming", pkgs);

  }


  @Test
  public void testJndiLookupSingle() throws Exception {
    NullConnection connection = new NullConnection();
    connection.setUniqueId("connection1");
    ArrayList<AdaptrisConnection> connectionList = new ArrayList<AdaptrisConnection>();
    connectionList.add(connection);
    AdaptrisConnectionImp connectionObject = null;
    InitialContext initialContext = new InitialContext(env);
    try {
      JndiHelper.bind(connectionList);
      connectionObject = (AdaptrisConnectionImp) initialContext.lookup("adapter:comp/env/connection1");
      assertTrue(connectionObject instanceof NullConnection);
      assertEquals("connection1", connectionObject.getUniqueId());
    }
    finally {
      JndiHelper.unbindQuietly(initialContext, connectionList, true);
    }

  }

  @Test
  public void testJndiLookupSingleWithJdbcConnection() throws Exception {
    MockJdbcConnection connection = new MockJdbcConnection();
    connection.setConnectionAttempts(1);
    connection.setUniqueId("jdbcConnection");
    ArrayList<AdaptrisConnection> connectionList = new ArrayList<AdaptrisConnection>();
    connectionList.add(connection);

    InitialContext ctx = new InitialContext(env);
    AdaptrisConnectionImp connectionObject = null;
    try {
      JndiHelper.bind(connectionList);

      connectionObject = (AdaptrisConnectionImp) ctx.lookup("adapter:comp/env/jdbcConnection");

      assertTrue(connectionObject instanceof MockJdbcConnection);
      assertEquals("jdbcConnection", connectionObject.getUniqueId());

      DataSource connectionObjectsDataSource = (DataSource) ctx.lookup("adapter:comp/env/jdbc/jdbcConnection");
      assertTrue(connectionObjectsDataSource instanceof DataSource);

    }
    finally {
      JndiHelper.unbindQuietly(ctx, connectionList, false);
    }
  }

  @Test
  public void testJndiLookupFromMultiple() throws Exception {
    // Pump the JNDI context with some connection objects
    NullConnection connection1 = new NullConnection();
    connection1.setUniqueId("connection1");
    JmsConnection connection2 = new JmsConnection();
    connection2.setUniqueId("connection2");
    JmsConnection connection3 = new JmsConnection();
    connection3.setUniqueId("connection3");
    FtpConnection connection4 = new FtpConnection();
    connection4.setUniqueId("connection4");
    HttpConnection connection5 = new HttpConnection();
    connection5.setUniqueId("connection5");

    ArrayList<AdaptrisConnection> connectionList = new ArrayList<AdaptrisConnection>();
    connectionList.add(connection1);
    connectionList.add(connection2);
    connectionList.add(connection3);
    connectionList.add(connection4);
    connectionList.add(connection5);

    AdaptrisConnectionImp connectionObject = null;
    InitialContext ctx = new InitialContext(env);
    try {
      JndiHelper.bind(connectionList);
      connectionObject = (AdaptrisConnectionImp) ctx.lookup("adapter:comp/env/connection1");
      assertTrue(connectionObject instanceof NullConnection);
      assertEquals("connection1", connectionObject.getUniqueId());

      connectionObject = (AdaptrisConnectionImp) ctx.lookup("adapter:comp/env/connection2");
      assertTrue(connectionObject instanceof JmsConnection);
      assertEquals("connection2", connectionObject.getUniqueId());

      connectionObject = (AdaptrisConnectionImp) ctx.lookup("adapter:comp/env/connection3");
      assertTrue(connectionObject instanceof JmsConnection);
      assertEquals("connection3", connectionObject.getUniqueId());

      connectionObject = (AdaptrisConnectionImp) ctx.lookup("adapter:comp/env/connection4");
      assertTrue(connectionObject instanceof FtpConnection);
      assertEquals("connection4", connectionObject.getUniqueId());

      connectionObject = (AdaptrisConnectionImp) ctx.lookup("adapter:comp/env/connection5");
      assertTrue(connectionObject instanceof HttpConnection);
      assertEquals("connection5", connectionObject.getUniqueId());
    }
    finally {
      JndiHelper.unbindQuietly(ctx, connectionList, false);
    }
  }

  @Test
  public void testNotFoundInEmptyJndiContext() throws Exception {
    try {
      InitialContext ctx = new InitialContext(env);
      ctx.lookup("connection1");
      fail();
    }
    catch (NamingException ex) {
      // expected - the object doesn't exist
    }
  }

  @Test
  public void testLoadWithNonUniqueObjectIds() throws Exception {
    NullConnection connection1 = new NullConnection();
    connection1.setUniqueId("connection1");
    JmsConnection connection2 = new JmsConnection();
    connection2.setUniqueId("connection1");
    JmsConnection connection3 = new JmsConnection();
    connection3.setUniqueId("connection1");

    ArrayList<AdaptrisConnection> connectionList = new ArrayList<AdaptrisConnection>();
    connectionList.add(connection1);
    connectionList.add(connection2);
    connectionList.add(connection3);
    InitialContext ctx = new InitialContext(env);
    try {
      JndiHelper.bind(connectionList);
      fail();
    }
    catch (CoreException expected) {

    }
    finally {
      JndiHelper.unbindQuietly(ctx, connectionList, false);
    }
  }

  @Test
  public void testWithFullContextAdapterSchemeDefaultInitialContext() throws Exception {
    SystemPropertiesUtil.addJndiProperties(bootstrapProps);

    NullConnection connection = new NullConnection();
    connection.setUniqueId("adapter:comp/env/connection6");
    ArrayList<AdaptrisConnection> connectionList = new ArrayList<AdaptrisConnection>();
    connectionList.add(connection);

    AdaptrisConnectionImp connectionObject = null;
    InitialContext ctx = new InitialContext();
    try {
      JndiHelper.bind(connectionList);
      connectionObject = (AdaptrisConnectionImp) ctx.lookup("adapter:comp/env/connection6");
      assertTrue(connectionObject instanceof NullConnection);
      assertEquals("adapter:comp/env/connection6", connectionObject.getUniqueId());

    }
    finally {
      JndiHelper.unbindQuietly(ctx, connectionList, false);
    }

  }

  @Test
  public void testWithFullContextAdapterSchemeDefaultInitialContextLookupName() throws Exception {
    SystemPropertiesUtil.addJndiProperties(bootstrapProps);

    NullConnection connection = new NullConnection();
    connection.setUniqueId("connection7");
    ArrayList<AdaptrisConnection> connectionList = new ArrayList<AdaptrisConnection>();
    connectionList.add(connection);

    JndiHelper.bind(connectionList);

    AdaptrisConnectionImp connectionObject = null;
    InitialContext ctx = new InitialContext();
    try {
      connectionObject = (AdaptrisConnectionImp) ctx.lookup("adapter:comp/env/connection7");
      assertTrue(connectionObject instanceof NullConnection);
      assertEquals("connection7", connectionObject.getUniqueId());

    }
    finally {
      JndiHelper.unbindQuietly(ctx, connectionList, false);
    }

  }

  @Test
  public void testWithAdapterSchemeDefaultInitialContext() throws Exception {
    SystemPropertiesUtil.addJndiProperties(bootstrapProps);

    NullConnection connection = new NullConnection();
    connection.setUniqueId("adapter:connection8");
    ArrayList<AdaptrisConnection> connectionList = new ArrayList<AdaptrisConnection>();
    connectionList.add(connection);

    InitialContext ctx = new InitialContext();

    AdaptrisConnectionImp connectionObject = null;
    try {
      JndiHelper.bind(connectionList);
      connectionObject = (AdaptrisConnectionImp) ctx.lookup("adapter:comp/env/connection8");
      assertTrue(connectionObject instanceof NullConnection);
      assertEquals("adapter:connection8", connectionObject.getUniqueId());

    }
    finally {
      JndiHelper.unbindQuietly(ctx, connectionList, false);
    }

  }

  @Test
  public void testWithDefaultInitialContext() throws Exception {
    SystemPropertiesUtil.addJndiProperties(bootstrapProps);

    NullConnection connection = new NullConnection();
    connection.setUniqueId("connection10");
    ArrayList<AdaptrisConnection> connectionList = new ArrayList<AdaptrisConnection>();
    connectionList.add(connection);

    InitialContext ctx = new InitialContext();
    AdaptrisConnectionImp connectionObject = null;
    try {
      JndiHelper.bind(connectionList);
      connectionObject = (AdaptrisConnectionImp) ctx.lookup("adapter:comp/env/connection10");
      assertTrue(connectionObject instanceof NullConnection);
      assertEquals("connection10", connectionObject.getUniqueId());

    }
    finally {
      JndiHelper.unbindQuietly(ctx, connectionList, true);
    }

  }

  @Test
  public void testWithDefaultInitialContextSubcontextLookup() throws Exception {
    SystemPropertiesUtil.addJndiProperties(bootstrapProps);

    NullConnection connection = new NullConnection();
    connection.setUniqueId("connection12");
    ArrayList<AdaptrisConnection> connectionList = new ArrayList<AdaptrisConnection>();
    connectionList.add(connection);

    Context compSubcontext = null;
    Context envSubcontext = null;
    AdaptrisConnectionImp connectionObject = null;
    InitialContext initialContext = new InitialContext();
    try {
      JndiHelper.bind(connectionList);
      compSubcontext = (Context) initialContext.lookup("adapter:comp");
      envSubcontext = (Context) compSubcontext.lookup("env");
      connectionObject = (AdaptrisConnectionImp) envSubcontext.lookup("connection12");
      assertTrue(envSubcontext instanceof NamingContext);
      assertTrue(connectionObject instanceof NullConnection);
      assertEquals("connection12", connectionObject.getUniqueId());

    }
    finally {
      JndiHelper.unbindQuietly(initialContext, connectionList, true);
      // The initial context isn't "ours" it's likely to be Jetty's so let's remove
      // connection12 explicitly from the context.
      envSubcontext.unbind("connection12");
    }
  }

  @Test
  public void testWithoutDefaultInitialContext() throws Exception {
    SystemPropertiesUtil.addJndiProperties(bootstrapProps);
    System.getProperties().remove(Context.INITIAL_CONTEXT_FACTORY);

    NullConnection connection = new NullConnection();
    connection.setUniqueId("connection12");
    ArrayList<AdaptrisConnection> connectionList = new ArrayList<AdaptrisConnection>();
    connectionList.add(connection);
    InitialContext ctx = new InitialContext();
    try {
      JndiHelper.bind(connectionList);

      AdaptrisConnectionImp connectionObject = null;

      connectionObject = (AdaptrisConnectionImp) ctx.lookup("adapter:comp/env/connection12");

      assertTrue(connectionObject instanceof NullConnection);
      assertEquals("connection12", connectionObject.getUniqueId());
    }
    finally {
      JndiHelper.unbindQuietly(ctx, connectionList, true);
    }
  }

}
