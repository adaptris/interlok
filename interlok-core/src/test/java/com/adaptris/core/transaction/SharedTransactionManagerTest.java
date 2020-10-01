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
package com.adaptris.core.transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import javax.jms.XAConnectionFactory;
import javax.transaction.xa.XAResource;
import org.junit.Test;
import org.mockito.Mockito;
import com.adaptris.core.Adapter;

public class SharedTransactionManagerTest extends com.adaptris.interlok.junit.scaffolding.BaseCase {


  @Test
  public void testSharedTransactionManager_StandardLookup() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    SharedTransactionManager mgr = new SharedTransactionManager(getName());
    adapter.getSharedComponents().setTransactionManager(new DummyTransactionManager(getName()));
    try {
      start(adapter);
      start(mgr);
      assertNotNull(mgr.proxiedTransactionManager());
      assertEquals(getName(), mgr.getUniqueId());
    } finally {
      stop(adapter);
      stop(mgr);
    }
  }

  @Test
  public void testSharedTransactionManager_NoName() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    SharedTransactionManager mgr = new SharedTransactionManager("comp/env/blah/" + getName());
    adapter.getSharedComponents().setTransactionManager(new DummyTransactionManager(getName()));
    try {
      start(adapter);
      start(mgr);
      assertNull(mgr.proxiedTransactionManager());
    } finally {
      stop(adapter);
      stop(mgr);
    }
  }

  @Test
  public void testSharedTransactionManager_transactions() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    SharedTransactionManager mgr = new SharedTransactionManager(getName());
    adapter.getSharedComponents().setTransactionManager(new DummyTransactionManager(getName()));
    try {
      start(adapter);
      start(mgr);
      mgr.beginTransaction();
      assertTrue(mgr.transactionIsActive());
      mgr.commit();
      assertFalse(mgr.transactionIsActive());
      mgr.beginTransaction();
      mgr.setRollbackOnly();
      mgr.rollback();
      assertFalse(mgr.transactionIsActive());
    } finally {
      stop(adapter);
      stop(mgr);
    }
  }

  @Test
  public void testSharedTransactionManager_XAResources() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    SharedTransactionManager mgr = new SharedTransactionManager(getName());
    adapter.getSharedComponents().setTransactionManager(new DummyTransactionManager(getName()));
    try {
      start(adapter);
      start(mgr);
      XAResource mockResource = Mockito.mock(XAResource.class);
      mgr.enlistXAResource(getName(), mockResource);
      mgr.delistXAResource(getName(), mockResource, XAResource.TMSUCCESS);

      XAConnectionFactory mockFac = Mockito.mock(XAConnectionFactory.class);
      mgr.registerXAResource(getName(), mockFac);
      mgr.deRegisterXAResource(getName(), mockFac);
    } finally {
      stop(adapter);
      stop(mgr);
    }
  }

}
