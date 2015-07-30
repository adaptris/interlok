package com.adaptris.core.services.dynamic;

import java.io.File;

import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceList;

public class RemoteCastorserviceStoreTest extends MarshallServiceStoreCase {

  public RemoteCastorserviceStoreTest(String s) {
    super(s);
  }

  @Override
  protected RemoteMarshallServiceStore createServiceStore() throws Exception {
    return new RemoteMarshallServiceStore();
  }

  public void testSetBaseUrl() throws Exception {
    RemoteMarshallServiceStore store = createServiceStore();
    try {
      store.setBaseUrl(null);
      fail("null setBaseUrl");
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      store.setBaseUrl("");
      fail("'' setBaseUrl");
    }
    catch (IllegalArgumentException expected) {

    }
  }

  public void testValidate() throws Exception {
    RemoteMarshallServiceStore store = createServiceStore();

    try {
      store.validate();
    }
    catch (CoreException e) {
      assertTrue(e.getMessage().matches(".*baseUrl.*is null.*"));
    }
    store.setBaseUrl("file:///fred");
    store.validate();
  }

  public void testObtainFileNotFound() throws Exception {
    File tmpDir = createAndTrackTempDir();
    RemoteMarshallServiceStore store = createServiceStore();
    store.setBaseUrl("file:///" + tmpDir.getAbsolutePath());
    assertNull(store.obtain("service"));
    markForDeath(tmpDir);
  }

  public void testSuccessfulObtain() throws Exception {
    File tmpDir = writeOutTheService("service");

    RemoteMarshallServiceStore store = createServiceStore();
    store.setBaseUrl("file:///" + tmpDir.getAbsolutePath());

    ServiceList service = (ServiceList) store.obtain("service");
    assertTrue(service.getClass() == ServiceList.class);
    assertTrue(service.getServices().size() == 1);
  }

  public void testSuccessfulObtainPrefixSuffix() throws Exception {
    File tmpDir = writeOutTheService("prefix-service.suffix");

    RemoteMarshallServiceStore store = createServiceStore();
    store.setBaseUrl("file:///" + tmpDir.getAbsolutePath());
    store.setFileNamePrefix("prefix-");
    store.setFileNameSuffix(".suffix");

    ServiceList service = (ServiceList) store.obtain("service");
    assertTrue(service.getClass() == ServiceList.class);
    assertTrue(service.getServices().size() == 1);
  }

}
