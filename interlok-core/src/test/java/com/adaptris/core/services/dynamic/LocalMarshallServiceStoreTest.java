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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import org.junit.Test;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceList;
import com.adaptris.core.stubs.TempFileUtils;

@SuppressWarnings("deprecation")
public class LocalMarshallServiceStoreTest extends MarshallServiceStoreCase {

  @Test
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

  @Test
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
      assertTrue(e.getMessage().matches(".*Does not exist.*"));
    }
    tmpDir.mkdirs();
    store.validate();
  }

  @Test
  public void testNullBaseDir() throws Exception {
    LocalMarshallServiceStore store = createServiceStore();
    ServiceList service = (ServiceList) store.obtain("whatever");
    assertEquals(service, null);
  }

  @Test
  public void testSuccessfulObtain() throws Exception {
    File tmpDir = writeOutTheService("service");

    LocalMarshallServiceStore store = new LocalMarshallServiceStore();
    store.setBaseDirUrl("file:///" + tmpDir.getAbsolutePath());

    ServiceList service = (ServiceList) store.obtain("service");
    assertTrue(service.getClass() == ServiceList.class);
    assertTrue(service.getServices().size() == 1);
  }

  @Test
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

  @Test
  public void testMissingService() throws Exception {
    File tmpDir = createAndTrackTempDir();

    LocalMarshallServiceStore store = createServiceStore();
    store.setBaseDirUrl("file:///" + tmpDir.getAbsolutePath());

    ServiceList service = (ServiceList) store.obtain("not-there");
    assertEquals(service, null);
  }

  @Test
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
