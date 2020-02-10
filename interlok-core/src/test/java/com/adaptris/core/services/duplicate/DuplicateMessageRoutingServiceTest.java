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

package com.adaptris.core.services.duplicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.services.SyntaxRoutingServiceExample;
import com.adaptris.util.GuidGenerator;

public class DuplicateMessageRoutingServiceTest extends SyntaxRoutingServiceExample {
  private static final String CHECK_KEY = "checkKey";
  private static final String DESTINATION_KEY = "destinationKey";
  private static final String DUPLICATE_KEY = "duplicateKey";
  private static final String UNIQUE_KEY = "uniqueKey";
  private static final String KEY_DUPLICATE_STORE = "DuplicateMessageRoutingService.store";

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }


  @Before
  public void setUp() throws Exception {

    File f = FsHelper.toFile(PROPERTIES.getProperty(KEY_DUPLICATE_STORE));
    if (f.exists()) {
      f.delete();
    }
  }

  @Test
  public void testService() throws Exception {
    DuplicateMessageRoutingService service = createService();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("ASDF");
    String key = new GuidGenerator().getUUID();
    msg.addMetadata(CHECK_KEY, key);
    execute(service, msg);
    assertEquals(UNIQUE_KEY, msg.getMetadataValue(DESTINATION_KEY));
    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("ASDF");
    msg.addMetadata(CHECK_KEY, key);
    execute(service, msg);
    assertEquals(DUPLICATE_KEY, msg.getMetadataValue(DESTINATION_KEY));
    assertConfigStoreExists();
  }

  @Test
  public void testServiceHistory() throws Exception {
    DuplicateMessageRoutingService service = createService();
    start(service);

    GuidGenerator guid = new GuidGenerator();
    String key = guid.getUUID();
    AdaptrisMessage msg = create(key);
    service.doService(msg);
    for (int i = 0; i < 101; i++) {
      AdaptrisMessage m = create(guid.getUUID());
      service.doService(m);
      assertEquals(UNIQUE_KEY, m.getMetadataValue(DESTINATION_KEY));
    }
    msg = create(key);
    service.doService(msg);
    // Should be unique again.
    assertEquals(UNIQUE_KEY, msg.getMetadataValue(DESTINATION_KEY));
    stop(service);
    assertConfigStoreExists();
  }

  @Test
  public void testStaticMethods() throws Exception {
    
    DuplicateMessageRoutingService.tryAndLog("hello", () -> {
    });
    DuplicateMessageRoutingService.tryAndLog("hello", () -> {
      throw new RuntimeException();
    });
    DuplicateMessageRoutingService.tryAndWrap(() -> {
    });
    try {
      DuplicateMessageRoutingService.tryAndWrap(() -> {
        throw new RuntimeException();
      });
      fail();
    } catch (CoreException expected) {

    }
  }


  private void assertConfigStoreExists() throws Exception {
    File f = FsHelper.toFile(PROPERTIES.getProperty(KEY_DUPLICATE_STORE));
    assertTrue("ConfigLocation [" + f.getCanonicalPath()
        + "] exists after shutdown", f.exists());
  }

  private DuplicateMessageRoutingService createService() {
    DuplicateMessageRoutingService service = new DuplicateMessageRoutingService();
    service.setConfigLocation(PROPERTIES.getProperty(KEY_DUPLICATE_STORE));
    service.setUniqueDestination(UNIQUE_KEY);
    service.setDuplicateDestination(DUPLICATE_KEY);
    service.setDestinationKey(DESTINATION_KEY);
    service.setKeyToCheck(CHECK_KEY);
    return service;
  }

  private AdaptrisMessage create(String key) {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage(key);
    msg.addMetadata(CHECK_KEY, key);
    return msg;
  }

  @Override
  protected DuplicateMessageRoutingService retrieveObjectForCastorRoundTrip() {
    return createService();
  }

  @Override
  protected DuplicateMessageRoutingService retrieveObjectForSampleConfig() {
    DuplicateMessageRoutingService service = new DuplicateMessageRoutingService();
    service.setDestinationKey("destination-key");
    service.setUniqueDestination("destination if unique");
    service.setDuplicateDestination("destination if duplicate");
    service.setConfigLocation("file:////filepath/to/message-id-store");
    return service;
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "<!--" + "\nThis service is unsed in conjunction with StoreMetadataValueService. "
        + "\nThe file locations for both services should be the same."
        + "\nStoreMetadataValueService stores keys and values that should be unique, "
        + "\nand DuplicateMessageRoutingService checks the file for any duplicates." + "\n-->\n";
  }

}
