package com.adaptris.core.services.dynamic;

import java.io.File;

import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceList;
import com.adaptris.core.stubs.TempFileUtils;

public class LocalMarshallServiceStoreTest extends MarshallServiceStoreCase {

  public LocalMarshallServiceStoreTest(String s) {
    super(s);
  }

  public void testBaseDirUrl() throws Exception {
    LocalMarshallServiceStore store = new LocalMarshallServiceStore();
    try {
      store.setBaseDirUrl(null);
      fail("null setBaseDirUrl");
    }
    catch (IllegalArgumentException expected) {
    }
    try {
      store.setBaseDirUrl("");
      fail("'' setBaseDirUrl");
    }
    catch (IllegalArgumentException expected) {
    }
  }

  @Override
  protected LocalMarshallServiceStore createServiceStore() throws Exception {
    return new LocalMarshallServiceStore();
  }

  public void testValidate() throws Exception {
    LocalMarshallServiceStore store = createServiceStore();
    File tmpDir = TempFileUtils.createTrackedDir(getClass().getSimpleName(), null, null, store);
    store.setBaseDirUrl("file:///" + tmpDir.getAbsolutePath());
    store.validate();
    tmpDir.delete();
    try {
      store.validate();
    }
    catch (CoreException e) {
      assertTrue(e.getMessage().matches(".*does not exist.*"));
    }
    tmpDir.mkdirs();
    store.validate();
  }

  public void testNullBaseDir() throws Exception {
    LocalMarshallServiceStore store = createServiceStore();
    ServiceList service = (ServiceList) store.obtain("whatever");
    assertEquals(service, null);
  }

  public void testSuccessfulObtain() throws Exception {
    File tmpDir = writeOutTheService("service");

    LocalMarshallServiceStore store = new LocalMarshallServiceStore();
    store.setBaseDirUrl("file:///" + tmpDir.getAbsolutePath());

    ServiceList service = (ServiceList) store.obtain("service");
    assertTrue(service.getClass() == ServiceList.class);
    assertTrue(service.getServices().size() == 1);
  }

  public void testSuccessfulObtainPrefixSuffix() throws Exception {
    File tmpDir = writeOutTheService("prefix-service.suffix");

    LocalMarshallServiceStore store = createServiceStore();
    store.setBaseDirUrl("file:///" + tmpDir.getAbsolutePath());
    store.setFileNamePrefix("prefix-");
    store.setFileNameSuffix(".suffix");

    ServiceList service = (ServiceList) store.obtain("service");
    assertTrue(service.getClass() == ServiceList.class);
    assertTrue(service.getServices().size() == 1);
  }

  public void testMissingService() throws Exception {
    File tmpDir = createAndTrackTempDir();

    LocalMarshallServiceStore store = createServiceStore();
    store.setBaseDirUrl("file:///" + tmpDir.getAbsolutePath());

    ServiceList service = (ServiceList) store.obtain("not-there");
    assertEquals(service, null);
  }

  public void testDefaultService() throws Exception {
    File tmpDir = writeOutTheService("service");
    LocalMarshallServiceStore store = createServiceStore();
    store.setBaseDirUrl("file:///" + tmpDir.getAbsolutePath());
    store.setDefaultFileName("service");
    ServiceList service = (ServiceList) store.obtain("not-there");
    assertTrue(service.getClass() == ServiceList.class);
    assertTrue(service.getServices().size() == 1);
  }
}
