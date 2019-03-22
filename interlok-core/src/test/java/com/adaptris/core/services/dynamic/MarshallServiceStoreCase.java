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
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.FileDeleteStrategy;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.BaseCase;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceList;
import com.adaptris.core.services.WaitService;
import com.adaptris.util.TimeInterval;

@SuppressWarnings("deprecation")
public abstract class MarshallServiceStoreCase extends BaseCase {

  private static FileCleaningTracker cleaner = new FileCleaningTracker();
  private Object markerObject = new Object();

  public MarshallServiceStoreCase(String s) {
    super(s);
  }

  protected abstract MarshallServiceStore createServiceStore() throws Exception;

  public void testSetFilenamePrefix() throws Exception {
    LocalMarshallServiceStore store = new LocalMarshallServiceStore();
    try {
      store.setFileNamePrefix(null);
      fail("null setFilenamePrefix");
    }
    catch (IllegalArgumentException expected) {
    }
  }

  public void testSetFilenameSuffix() throws Exception {
    LocalMarshallServiceStore store = new LocalMarshallServiceStore();
    try {
      store.setFileNameSuffix(null);
      fail("null setFileNameSuffix");
    }
    catch (IllegalArgumentException expected) {
    }
  }

  protected void markForDeath(File f) {
    cleaner.track(f, markerObject, FileDeleteStrategy.FORCE);
  }

  protected File writeOutTheService(String name) throws Exception {
    File dir = createAndTrackTempDir();
    ServiceList list = createServiceList();
    System.err.println("Marshalling to " + new File(dir, name).getCanonicalPath());
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    m.marshal(list, new File(dir, name));
    return dir;
  }

  protected File createAndTrackTempDir() throws Exception {
    File tmpDir = File.createTempFile(this.getClass().getSimpleName(), null);
    tmpDir.delete();
    tmpDir.mkdirs();
    cleaner.track(tmpDir, markerObject, FileDeleteStrategy.FORCE);
    return tmpDir;
  }

  protected ServiceList createServiceList() {
    return new ServiceList(new Service[]
    {
      new WaitService(new TimeInterval(25L, TimeUnit.SECONDS))
    });
  }

}
