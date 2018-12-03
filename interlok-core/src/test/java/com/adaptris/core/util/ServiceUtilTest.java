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
package com.adaptris.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import com.adaptris.core.AllowsRetriesConnection;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.NullConnection;
import com.adaptris.core.NullMessageProducer;
import com.adaptris.core.NullService;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.services.StatelessServiceWrapper;
import com.adaptris.core.services.jdbc.JdbcServiceList;
import com.adaptris.core.services.metadata.AddMetadataService;
import com.adaptris.core.stubs.MockAllowsRetriesConnection;

public class ServiceUtilTest extends ServiceUtil {

  @Test
  public void testDiscardNull() throws Exception {
    assertEquals(2, discardNulls(new NullService(), new NullService()).length);
    assertEquals(1, discardNulls(new NullService(), null, null).length);
    assertEquals(0, discardNulls().length);
  }

  @Test
  public void testRewriteForTesting_NoConnection() throws Exception {
    NullService nullService = new NullService();
    JdbcServiceList jdbcService = new JdbcServiceList();
    StatelessServiceWrapper stateless = new StatelessServiceWrapper(new StandaloneProducer());
    assertTrue(nullService == rewriteConnectionsForTesting(nullService));
    assertTrue(jdbcService == rewriteConnectionsForTesting(jdbcService));
    assertTrue(stateless == rewriteConnectionsForTesting(stateless));
    ServiceList nestedList = new ServiceList();
    ServiceList list = new ServiceList();
    list.add(nestedList);
    assertTrue(list == rewriteConnectionsForTesting(list));
  }

  @Test
  public void testRewriteForTesting_Connection() throws Exception {
    NullConnection conn = new NullConnection();
    StandaloneProducer service = new StandaloneProducer(conn, new NullMessageProducer());
    StandaloneProducer rewritten = ((StandaloneProducer) rewriteConnectionsForTesting(service));
    assertNotSame(conn, rewritten.getConnection());
  }

  @Test
  public void testRewriteForTesting_RetriesConnection() throws Exception {
    MockAllowsRetriesConnection conn = new MockAllowsRetriesConnection(55);
    StandaloneProducer service = new StandaloneProducer(conn, new NullMessageProducer());
    StandaloneProducer rewritten = ((StandaloneProducer) rewriteConnectionsForTesting(service));
    assertNotSame(conn, rewritten.getConnection());
    assertNotSame(55, ((AllowsRetriesConnection) rewritten.getConnection()).getConnectionAttempts());
  }

  private String createConnectedServices() throws Exception {
    ServiceList nestedList = new ServiceList();
    nestedList.add(new StandaloneProducer(new MockAllowsRetriesConnection(6), new NullMessageProducer()));
    nestedList.add(new StatelessServiceWrapper(new StandaloneProducer()));
    nestedList.add(new AddMetadataService(new ArrayList(Arrays.asList(new MetadataElement[]
    {
        new MetadataElement("key", "value")
    }))));
    ServiceList list = new ServiceList();
    list.add(nestedList);
    list.add(new JdbcServiceList());
    return DefaultMarshaller.getDefaultMarshaller().marshal(list);
  }

}
