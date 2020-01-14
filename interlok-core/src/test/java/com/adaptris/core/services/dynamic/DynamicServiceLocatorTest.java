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

package com.adaptris.core.services.dynamic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ClosedState;
import com.adaptris.core.ConfiguredTradingRelationshipCreator;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.EventHandler;
import com.adaptris.core.MetadataTradingRelationshipCreator;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceList;
import com.adaptris.core.TradingRelationship;
import com.adaptris.core.TradingRelationshipCreator;
import com.adaptris.core.XpathTradingRelationshipCreator;
import com.adaptris.core.services.dynamic.DynamicFailingService.WhenToFail;
import com.adaptris.core.util.LifecycleHelper;

@SuppressWarnings("deprecation")
public class DynamicServiceLocatorTest extends DynamicServiceExample {

  private static final String DEFAULT_DEST = "TheDestination";
  private static final String DEFAULT_SRC = "TheSource";
  private static final String DEFAULT_TYPE = "TheType";

  private static final ServiceNameProvider[] providers =
  {
      new ConfiguredServiceNameProvider(new HashSet(Arrays.asList(new ServiceNameMapper[]
      {
          new ServiceNameMapper("source", "dest", "type", "The_Name_Of_A_File_Containing_A_ServiceList"),
          new ServiceNameMapper(DEFAULT_SRC, DEFAULT_DEST, DEFAULT_TYPE, "The_Name_Of_A_File_Containing_A_ServiceList"),
          new ServiceNameMapper("*", "*", "*", "The_Name_Of_A_File_Containing_A_ServiceList")
      }))), new DefaultServiceNameProvider(), new SafeServiceNameProvider()
  };

  private static final ServiceStore[] serviceStores;
  private static final TradingRelationshipCreator[] relationships = {
    new ConfiguredTradingRelationshipCreator(DEFAULT_SRC, DEFAULT_DEST, DEFAULT_TYPE),
    new MetadataTradingRelationshipCreator("source-metadata-key", "destination-metadata-key", "type-metadata-key"),
      new XpathTradingRelationshipCreator("/source/xpath", "/destination/xpath", "type/xpath")
  };


  static {
    try {
      serviceStores = new ServiceStore[]
      {
          new LocalMarshallServiceStore("file:///path/to/directory/where/you/can/find/services", "", ".xml", "default-filename"),
          new RemoteMarshallServiceStore("http://myserver.com/location/where/xml/files/are", "", ".xml", "default-filename")
      };
    }
    catch (CoreException e) {
      throw new RuntimeException(e);
    }
  };

  private File tempDir;

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }


  @Before
  public void setUp() throws Exception {
    tempDir = File.createTempFile("DSL", null);
    tempDir.delete();
    tempDir.mkdirs();
  }

  @After
  public void tearDown() throws Exception {
    FileUtils.cleanDirectory(tempDir);
    FileUtils.deleteQuietly(tempDir);
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    List<DynamicServiceLocator> result = new ArrayList();
    for (int i = 0; i < providers.length; i++) {
      for (int j=0; j < serviceStores.length; j++) {
        for (int k = 0; k < relationships.length; k++) {
          DynamicServiceLocator dsl = new DynamicServiceLocator();
          dsl.setServiceNameProvider(providers[i]);
          dsl.setServiceStore(serviceStores[j]);
          dsl.setTradingRelationshipCreator(relationships[k]);
          result.add(dsl);
        }
      }
    }
    return result;
  }

  @Override
  protected String createBaseFileName(Object object) {
    DynamicServiceLocator obj = (DynamicServiceLocator) object;
    return super.createBaseFileName(obj) + "-" + obj.getTradingRelationshipCreator().getClass().getSimpleName() + "-"
        + obj.getServiceNameProvider().getClass().getSimpleName() + "-" + obj.getServiceStore().getClass().getSimpleName();
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    // DynamicServiceLocator service = new DynamicServiceLocator();
    // try {
    // service = createLocator();
    //
    // ConfiguredServiceNameProvider p = new ConfiguredServiceNameProvider();
    // p.addServiceNameMapper(new ServiceNameMapper("source", "dest", "type",
    // "The_Name_Of_A_File_Containing_A_ServiceList"));
    // p.addServiceNameMapper(new ServiceNameMapper("TheSource",
    // "TheDestination", "TheType",
    // "The_Name_Of_A_File_Containing_A_ServiceList"));
    // p.addServiceNameMapper(new ServiceNameMapper("*", "*", "*",
    // "The_Name_Of_A_File_Containing_A_ServiceList"));
    // service.setServiceNameProvider(p);
    // ((LocalCastorServiceStore)
    // service.getServiceStore()).setBaseDirUrl("file:///path/to/directory/where/you/can/find/services");
    // }
    // catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // return service;
    return null;
  }

  private DynamicServiceLocator createLocator(EventHandler eventHandler) throws Exception {
    DynamicServiceLocator service = new DynamicServiceLocator();
    ConfiguredTradingRelationshipCreator t = new ConfiguredTradingRelationshipCreator(DEFAULT_SRC, DEFAULT_DEST, DEFAULT_TYPE);
    service.setTradingRelationshipCreator(t);
    service.setServiceNameProvider(new DefaultServiceNameProvider());
    LocalMarshallServiceStore store = new LocalMarshallServiceStore();
    store.setBaseDirUrl(tempDir.toURI().toURL().toString());
    store.setFileNameSuffix(".xml");
    service.setServiceStore(store);
    service.registerEventHandler(eventHandler);
    return service;
  }

  private DynamicServiceLocator createLocator() throws Exception {
    return createLocator(createandStartDummyEventHandler());
  }

  @Test
  public void testSetMatchingStrategy() throws Exception {
    DynamicServiceLocator service = new DynamicServiceLocator();
    try {
      service.setMatchingStrategy(null);
      fail("null matching strategy allowed");
    }
    catch (IllegalArgumentException expected) {

    }
    service.setMatchingStrategy(new ExactMatchingStrategy());
    assertNotNull(service.getMatchingStrategy());
  }

  @Test
  public void testSetServiceStore() throws Exception {
    DynamicServiceLocator service = new DynamicServiceLocator();
    try {
      service.setServiceStore(null);
      fail("null setServiceStore allowed");
    }
    catch (IllegalArgumentException expected) {

    }
    service.setServiceStore(new LocalMarshallServiceStore());
    assertNotNull(service.getServiceStore());
  }

  @Test
  public void testSetServiceNameProvider() throws Exception {
    DynamicServiceLocator service = new DynamicServiceLocator();
    try {
      service.setServiceNameProvider(null);
      fail("null setServiceNameProvider allowed");
    }
    catch (IllegalArgumentException expected) {

    }
    service.setServiceNameProvider(new DefaultServiceNameProvider());
    assertNotNull(service.getServiceNameProvider());
  }

  @Test
  public void testSetTradingRelationshipCreator() throws Exception {
    DynamicServiceLocator service = new DynamicServiceLocator();
    try {
      service.setTradingRelationshipCreator(null);
      fail("null setTradingRelationshipCreator allowed");
    }
    catch (IllegalArgumentException expected) {

    }
    service.setTradingRelationshipCreator(new ConfiguredTradingRelationshipCreator());
    assertNotNull(service.getTradingRelationshipCreator());
  }

  @Test
  public void testDoServiceWithWildcardRelationship() throws Exception {
    DynamicServiceLocator service = createLocator();
    service.setTradingRelationshipCreator(new ConfiguredTradingRelationshipCreator("*", DEFAULT_DEST, DEFAULT_TYPE));
    start(service);
    try {
      service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());
      fail("Expected ServiceException");
    }
    catch (ServiceException e) {
      assertTrue(e.getMessage().matches(".*contains wild cards and is invalid.*"));
    }
    finally {
      stop(service);
    }
  }

  @Test
  public void testInit() throws Exception {
    DynamicServiceLocator service = new DynamicServiceLocator();
    try {
      LifecycleHelper.init(service);
    }
    catch (CoreException expected) {
    }
    service.setTradingRelationshipCreator(new ConfiguredTradingRelationshipCreator("*", DEFAULT_DEST, DEFAULT_TYPE));
    try {
      LifecycleHelper.init(service);
    }
    catch (CoreException expected) {
    }
    service.setServiceNameProvider(new DefaultServiceNameProvider());
    try {
      LifecycleHelper.init(service);
    }
    catch (CoreException expected) {
    }
    LocalMarshallServiceStore store = new LocalMarshallServiceStore();
    store.setBaseDirUrl(tempDir.toURI().toURL().toString());
    store.setFileNameSuffix(".xml");
    service.setServiceStore(store);
    LifecycleHelper.init(service);
  }

  @Test
  public void testServiceNotFoundNotOk() throws Exception {
    DynamicServiceLocator service = createLocator();
    service.setTreatNotFoundAsError(true);
    service.setTradingRelationshipCreator(new ConfiguredTradingRelationshipCreator("ABCDEFG", DEFAULT_DEST, DEFAULT_TYPE));
    start(service);
    try {
      service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());
      fail("Expected ServiceException");
    }
    catch (ServiceException e) {
      assertTrue(e.getMessage().matches(".*was not found.*"));
    }
    finally {
      stop(service);
    }
  }

  @Test
  public void testServiceNotFoundIsOk() throws Exception {
    DynamicServiceLocator service = createLocator();
    service.setTreatNotFoundAsError(false);
    service.setTradingRelationshipCreator(new ConfiguredTradingRelationshipCreator("ABCDEFG", DEFAULT_DEST, DEFAULT_TYPE));
    execute(service, AdaptrisMessageFactory.getDefaultInstance().newMessage());
  }

  @Test
  public void testServicePrepareFails() throws Exception {
    writeServiceList(new DynamicFailingService(WhenToFail.ON_PREPARE),
        new TradingRelationship(DEFAULT_SRC, DEFAULT_DEST, DEFAULT_TYPE), tempDir);
    DynamicServiceLocator service = createLocator();
    service.setServiceNameProvider(new DefaultServiceNameProvider());

    start(service);
    try {
      service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());
      fail("Expected ServiceException");
    } catch (ServiceException e) {
      assertTrue(e.getMessage().matches(".*ON_PREPARE.*"));
    } finally {
      stop(service);
    }
  }

  @Test
  public void testServiceFailOnInit() throws Exception {
    DynamicFailingService dynamicFailingService = new DynamicFailingService(WhenToFail.ON_INIT);
    writeServiceList(dynamicFailingService,
        new TradingRelationship(DEFAULT_SRC, DEFAULT_DEST, DEFAULT_TYPE), tempDir);
    DynamicServiceLocator service = createLocator();
    service.setServiceNameProvider(new DefaultServiceNameProvider());

    start(service);
    try {
      service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());
      fail("Expected ServiceException");
    }
    catch (ServiceException e) {
      assertTrue(e.getCause() != null);
      assertEquals(CoreException.class, e.getCause().getClass());
      assertTrue(e.getCause().getMessage().matches(".*ON_INIT failure specified.*"));
    }
    finally {
      stop(service);
    }
    assertEquals(ClosedState.getInstance(), service.retrieveComponentState());
    assertEquals(ClosedState.getInstance(), dynamicFailingService.retrieveComponentState());
    
//    assertEquals(DynamicService.State.CLOSE, DynamicService.currentState(DynamicFailingService.class));
  }

  @Test
  public void testServiceFailOnStart() throws Exception {
    writeServiceList(new DynamicFailingService(WhenToFail.ON_START), new TradingRelationship(DEFAULT_SRC, DEFAULT_DEST,
        DEFAULT_TYPE), tempDir);
    DynamicServiceLocator service = createLocator();
    service.setServiceNameProvider(new DefaultServiceNameProvider());

    start(service);
    try {
      service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());
      fail("Expected ServiceException");
    }
    catch (ServiceException e) {
      assertTrue(e.getCause() != null);
      assertEquals(CoreException.class, e.getCause().getClass());
      assertTrue(e.getCause().getMessage().matches(".*ON_START failure specified.*"));
    }
    finally {
      stop(service);
    }
    assertEquals(DynamicService.State.CLOSE, DynamicService.currentState(DynamicFailingService.class));
  }

  @Test
  public void testDoSuccessfulService() throws Exception {
    writeServiceList(new DynamicService(), new TradingRelationship(DEFAULT_SRC, DEFAULT_DEST, DEFAULT_TYPE), tempDir);

    DynamicServiceLocator service = createLocator();
    service.setTreatNotFoundAsError(true);
    service.setServiceNameProvider(new DefaultServiceNameProvider());
    execute(service, AdaptrisMessageFactory.getDefaultInstance().newMessage());
    assertEquals(DynamicService.State.CLOSE, DynamicService.currentState(DynamicService.class));
  }

  @Test
  public void testBug887WithFailingService() throws Exception {
    writeServiceList(new DynamicFailingService(), new TradingRelationship(DEFAULT_SRC, DEFAULT_DEST, DEFAULT_TYPE), tempDir);
    DynamicServiceLocator service = createLocator();
    service.setTreatNotFoundAsError(true);
    service.setServiceNameProvider(new DefaultServiceNameProvider());
    start(service);
    try {
      service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());
      fail("Expected ServiceException");
    }
    catch (ServiceException e) {
      ; // expected
    }
    finally {
      stop(service);
    }
    assertEquals(DynamicService.State.CLOSE, DynamicService.currentState(DynamicFailingService.class));
  }

  @Test
  public void testEventHandlerAwareNotServicelist() throws Exception {
    writeService(new DynamicEventHandlerAwareService(), new TradingRelationship(DEFAULT_SRC, DEFAULT_DEST, DEFAULT_TYPE),
        tempDir);
    EventHandler eventHandler = createandStartDummyEventHandler();
    DynamicServiceLocator service = createLocator(eventHandler);
    service.setTreatNotFoundAsError(true);
    service.setServiceNameProvider(new DefaultServiceNameProvider());
    execute(service, AdaptrisMessageFactory.getDefaultInstance().newMessage());
    assertEquals(eventHandler, DynamicEventHandlerAwareService.registeredEventHandler());
  }

  @Test
  public void testNotEventHandlerAwareNotServicelist() throws Exception {
    writeService(new DynamicService(), new TradingRelationship(DEFAULT_SRC, DEFAULT_DEST, DEFAULT_TYPE), tempDir);
    EventHandler eventHandler = createandStartDummyEventHandler();
    DynamicServiceLocator service = createLocator(eventHandler);
    service.setTreatNotFoundAsError(true);
    service.setServiceNameProvider(new DefaultServiceNameProvider());
    execute(service, AdaptrisMessageFactory.getDefaultInstance().newMessage());
  }

  private void writeServiceList(Service s, TradingRelationship t, File dir) throws Exception {
    ServiceList sl = new ServiceList();
    sl.addService(s);
    writeService(sl, t, dir);
  }

  private void writeService(Service s, TradingRelationship t, File dir) throws Exception {
    DefaultServiceNameProvider dsnp = new DefaultServiceNameProvider();
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    System.out.println("Writing to temp file - " + new File(dir, dsnp.obtain(t) + ".xml").getAbsolutePath());
    m.marshal(s, new File(dir, dsnp.obtain(t) + ".xml"));
  }
}
