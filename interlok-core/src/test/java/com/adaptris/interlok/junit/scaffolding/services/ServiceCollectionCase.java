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

package com.adaptris.interlok.junit.scaffolding.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import org.junit.Test;
import com.adaptris.core.DefaultEventHandler;
import com.adaptris.core.MessageEventGenerator;
import com.adaptris.core.NullService;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceCollectionImp;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.services.WaitService;
import com.adaptris.core.stubs.ConfigCommentHelper;
import com.adaptris.core.stubs.EventHandlerAwareService;
import com.adaptris.core.util.LifecycleHelper;

public abstract class ServiceCollectionCase extends ExampleServiceCollection {


  public abstract ServiceCollectionImp createServiceCollection();

  public abstract ServiceCollectionImp createServiceCollection(Collection<Service> services);

  @Test
  public void testComments() throws Exception {
    ConfigCommentHelper.testComments(createServiceCollection());
  }

  @Test
  public void testCollectionConstructor() {
    ServiceCollectionImp sc = createServiceCollection();
    sc.addService(new NullService(UUID.randomUUID().toString()));
    sc.addService(new NullService(UUID.randomUUID().toString()));
    ServiceCollectionImp sc2 = createServiceCollection(sc);
    assertEquals(sc.size(), sc2.size());
  }

  @Test
  public void testAdd() throws Exception {
    ServiceCollectionImp sc = createServiceCollection();
    assertTrue(sc.add(new NullService(UUID.randomUUID().toString())));
    try {
      sc.add(null);
      fail();
    }
    catch (IllegalArgumentException e) {
    }
    assertEquals(1, sc.size());
  }

  @Override
  public void testMessageEventGenerator() throws Exception {
    Object input = retrieveObjectForCastorRoundTrip();
    if (input != null) {
      if (input instanceof ServiceImp) {
        assertMessageEventGenerator((ServiceCollectionImp) input);
      }
    }
    else {
      List l = retrieveObjectsForSampleConfig();
      for (Object o : retrieveObjectsForSampleConfig()) {
        if (o instanceof MessageEventGenerator) {
          assertMessageEventGenerator((ServiceCollectionImp) o);
        }
      }
    }
  }

  public static void assertMessageEventGenerator(ServiceCollectionImp meg) {
    meg.setIsTrackingEndpoint(null);
    assertNull(meg.getIsTrackingEndpoint());
    assertFalse(meg.isTrackingEndpoint());

    meg.setIsTrackingEndpoint(Boolean.TRUE);
    assertNotNull(meg.getIsTrackingEndpoint());
    assertTrue(meg.isTrackingEndpoint());
  }

  @Test
  public void testCreateName() {
    ServiceCollectionImp sc = createServiceCollection();
    assertEquals(sc.getClass().getName(), sc.createName());
  }

  @Test
  public void testContinueOnFail() throws Exception {
    ServiceCollectionImp sc = createServiceCollection();
    assertNull(sc.getContinueOnFail());
    assertEquals(false, sc.continueOnFailure());
    sc.setContinueOnFail(Boolean.TRUE);
    assertEquals(Boolean.TRUE, sc.getContinueOnFail());
    assertEquals(true, sc.continueOnFailure());
    sc.setContinueOnFail(Boolean.FALSE);
    assertEquals(false, sc.continueOnFailure());
    assertEquals(Boolean.FALSE, sc.getContinueOnFail());
  }

  @Test
  public void testInitWithEventHandlerAware() throws Exception {
    EventHandlerAwareService s = new EventHandlerAwareService(UUID.randomUUID().toString());
    ServiceCollectionImp sc = createServiceCollection();
    DefaultEventHandler eh = new DefaultEventHandler();
    sc.registerEventHandler(eh);
    sc.addService(new NullService(UUID.randomUUID().toString()));
    sc.addService(s);
    LifecycleHelper.init(sc);
    assertNotNull(s.retrieveEventHandler());
    assertEquals(eh, s.retrieveEventHandler());
  }

  @Test
  public void testAddService() throws Exception {
    ServiceCollectionImp sc = createServiceCollection();
    sc.addService(new NullService(UUID.randomUUID().toString()));
    assertEquals(1, sc.size());
    try {
      sc.addService(null);
      fail();
    }
    catch (IllegalArgumentException e) {
    }
    assertEquals(1, sc.size());
  }

  @Test
  public void testSize() throws Exception {
    ServiceCollectionImp sc = createServiceCollection();
    sc.add(new NullService(UUID.randomUUID().toString()));
    assertEquals(1, sc.size());
  }

  @Test
  public void testIterator() throws Exception {
    ServiceCollectionImp sc = createServiceCollection();
    sc.addAll(Arrays.asList(new Service[]
    {
        new NullService(UUID.randomUUID().toString()), new NullService(UUID.randomUUID().toString())
    }));
    assertEquals(2, sc.size());
    assertNotNull(sc.iterator());
    int count = 0;
    for (Iterator<Service> i = sc.iterator(); i.hasNext();) {
      assertNotNull(i.next());
      count++;
    }
    assertEquals(2, count);

    assertNotNull(sc.listIterator(0));
    count = 0;
    for (ListIterator<Service> i = sc.listIterator(0); i.hasNext();) {
      assertNotNull(i.next());
      count++;
    }
    assertEquals(2, count);
    assertNotNull(sc.listIterator());
    count = 0;
    for (ListIterator<Service> i = sc.listIterator(); i.hasNext();) {
      assertNotNull(i.next());
      count++;
    }
    assertEquals(2, count);

  }

  @Test
  public void testSetServices() throws Exception {
    ServiceCollectionImp sc = createServiceCollection();
    List<Service> services = Arrays.asList(new Service[]
    {
        new NullService(UUID.randomUUID().toString()), new NullService(UUID.randomUUID().toString())
    });

    sc.setServices(services);
    assertEquals(2, sc.size());
    assertEquals(services, sc.getServices());
    try {
      sc.setServices(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals(2, sc.size());
    assertEquals(services, sc.getServices());
  }

  @Test
  public void testAddAtPosition() throws Exception {
    ServiceCollectionImp sc = createServiceCollection();
    sc.addService(new NullService(UUID.randomUUID().toString()));
    sc.addService(new NullService(UUID.randomUUID().toString()));
    sc.add(1, new WaitService(UUID.randomUUID().toString()));
    assertEquals(3, sc.size());
    assertEquals(WaitService.class, sc.get(1).getClass());
  }

  @Test
  public void testAddAllAtPosition() throws Exception {
    ServiceCollectionImp sc = createServiceCollection();
    sc.addService(new NullService(UUID.randomUUID().toString()));
    sc.addService(new NullService(UUID.randomUUID().toString()));
    ServiceCollectionImp sc2 = createServiceCollection();
    sc2.addService(new WaitService(UUID.randomUUID().toString()));
    sc2.addService(new WaitService(UUID.randomUUID().toString()));
    sc.addAll(1, sc2);
    assertEquals(4, sc.size());
    assertEquals(WaitService.class, sc.get(1).getClass());
    assertEquals(WaitService.class, sc.get(2).getClass());
  }

  @Test
  public void testGet() throws Exception {
    ServiceCollectionImp sc = createServiceCollection();
    sc.addService(new NullService(UUID.randomUUID().toString()));
    sc.addService(new WaitService(UUID.randomUUID().toString()));
    sc.addService(new NullService(UUID.randomUUID().toString()));
    assertEquals(WaitService.class, sc.get(1).getClass());
  }

  @Test
  public void testIndexOf() throws Exception {
    ServiceCollectionImp sc = createServiceCollection();
    sc.addService(new NullService(UUID.randomUUID().toString()));
    WaitService s = new WaitService(UUID.randomUUID().toString());
    sc.addService(s);
    sc.addService(new NullService(UUID.randomUUID().toString()));
    assertEquals(1, sc.indexOf(s));
  }

  @Test
  public void testLastIndexOf() throws Exception {
    ServiceCollectionImp sc = createServiceCollection();
    sc.addService(new NullService(UUID.randomUUID().toString()));
    WaitService s = new WaitService(UUID.randomUUID().toString());
    sc.addService(new NullService(UUID.randomUUID().toString()));
    sc.addService(s);
    assertEquals(2, sc.lastIndexOf(s));
  }

  @Test
  public void testRemove() throws Exception {
    ServiceCollectionImp sc = createServiceCollection();
    sc.addService(new NullService(UUID.randomUUID().toString()));
    WaitService wait = new WaitService(UUID.randomUUID().toString());
    sc.addService(wait);
    sc.addService(new NullService(UUID.randomUUID().toString()));
    Service s = sc.remove(1);
    assertEquals(WaitService.class, s.getClass());
    assertEquals(wait, s);
    assertEquals(2, sc.size());
    assertFalse(sc.contains(wait));
  }

  @Test
  public void testSetAtPosition() throws Exception {
    ServiceCollectionImp sc = createServiceCollection();
    sc.addService(new NullService(UUID.randomUUID().toString()));
    WaitService wait = new WaitService(UUID.randomUUID().toString());
    sc.addService(wait);
    sc.addService(new NullService(UUID.randomUUID().toString()));
    Service s = sc.set(1, new NullService(UUID.randomUUID().toString()));
    assertEquals(WaitService.class, s.getClass());
    assertEquals(wait, s);
    assertEquals(3, sc.size());
    assertFalse(sc.contains(wait));
  }

  @Test
  public void testSubList() {
    ServiceCollectionImp list = createServiceCollection();
    list.add(new NullService(UUID.randomUUID().toString()));
    list.add(new NullService(UUID.randomUUID().toString()));
    list.add(new NullService(UUID.randomUUID().toString()));
    list.add(new NullService(UUID.randomUUID().toString()));
    assertNotNull(list.subList(0, 2));
    assertEquals(2, list.subList(0, 2).size());
  }

  @Test
  public void testClear() {
    ServiceCollectionImp list = createServiceCollection();
    list.add(new NullService(UUID.randomUUID().toString()));
    list.add(new NullService(UUID.randomUUID().toString()));
    list.add(new NullService(UUID.randomUUID().toString()));
    list.add(new NullService(UUID.randomUUID().toString()));
    assertEquals(4, list.size());
    list.clear();
    assertEquals(0, list.size());
  }
}
