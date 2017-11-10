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
      fail();
    }
    catch (CoreException expected) {
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
