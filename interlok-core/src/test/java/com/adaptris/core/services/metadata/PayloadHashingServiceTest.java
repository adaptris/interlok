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

package com.adaptris.core.services.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.security.MessageDigest;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.text.Base64ByteTranslator;
import com.adaptris.util.text.ByteTranslator;
import com.adaptris.util.text.HexStringByteTranslator;

@SuppressWarnings("deprecation")
public class PayloadHashingServiceTest extends MetadataServiceExample {

  private static final String METADATA_KEY = "metadata-key";
  private static final String SHA256 = "SHA256";
  private static final String PAYLOAD = "Glib jocks quiz nymph to vex dwarf";

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testInit() throws Exception {
    PayloadHashingService service = new PayloadHashingService();
    try {
      LifecycleHelper.init(service);
      fail();
    }
    catch (CoreException expected) {
    }
    service.setMetadataKey(METADATA_KEY);
    service.setHashAlgorithm(SHA256);
    LifecycleHelper.init(service);
  }

  @Test
  public void testService() throws Exception {
    PayloadHashingService service = new PayloadHashingService(SHA256, METADATA_KEY);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    execute(service, msg);
    assertTrue(msg.containsKey(METADATA_KEY));
    assertEquals(PAYLOAD, msg.getContent());
    assertNotNull(msg.getMetadataValue(METADATA_KEY));
    assertEquals(createHash(new Base64ByteTranslator()), msg.getMetadataValue(METADATA_KEY));
  }

  @Test
  public void testServiceException() throws Exception {
    PayloadHashingService service = new PayloadHashingService(SHA256, METADATA_KEY);
    AdaptrisMessage msg = new DefectiveMessageFactory().newMessage(PAYLOAD);
    try {
    execute(service, msg);
      fail();
    }
    catch (ServiceException e) {
      ;
    }
  }

  @Test
  public void testService_WithByteTranslator() throws Exception {
    PayloadHashingService service = new PayloadHashingService(SHA256, METADATA_KEY);
    service.setByteTranslator(new HexStringByteTranslator());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    execute(service, msg);
    assertTrue(msg.containsKey(METADATA_KEY));
    assertEquals(PAYLOAD, msg.getContent());
    assertNotNull(msg.getMetadataValue(METADATA_KEY));
    assertEquals(createHash(new HexStringByteTranslator()), msg.getMetadataValue(METADATA_KEY));
  }

  private String createHash(ByteTranslator b) throws Exception {
    MessageDigest digest = MessageDigest.getInstance(SHA256);
    return b.translate(digest.digest(PAYLOAD.getBytes()));
  }

  @Override
  protected PayloadHashingService retrieveObjectForSampleConfig() {
    return new PayloadHashingService(SHA256, METADATA_KEY);
  }

}
