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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.text.Base64ByteTranslator;
import com.adaptris.util.text.HexStringByteTranslator;

public class MetadataHashingTest extends MetadataServiceExample {

  private static final String METADATA_KEY = "key";
  private static final String METADATA_VALUE = "2104913203";
  private static final String METADATA_HASH_MD5 = "fff9f3d8d4ec2726e0b2422116b20dd2";
  
  public MetadataHashingTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }


  private AdaptrisMessage createMessage(String encoding) throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("asdfghjk", encoding);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    msg.addMetadata("yetAnotherKey", "");
    return msg;
  }

  public void testSetHashAlgorithm() throws Exception {
    MetadataHashingService service = new MetadataHashingService();
    assertEquals("SHA1", service.getHashAlgorithm());
    service.setHashAlgorithm("MD5");
    assertEquals("MD5", service.getHashAlgorithm());
    LifecycleHelper.init(service);
    LifecycleHelper.close(service);
    try {
      service.setHashAlgorithm(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    service.setHashAlgorithm("Something Unexpected");
    try {
      LifecycleHelper.init(service);
      fail();
    }
    catch (CoreException expected) {

    }
  }

  public void testSetByteTranslator() throws Exception {
    MetadataHashingService service = new MetadataHashingService();
    assertEquals(Base64ByteTranslator.class, service.getByteTranslator().getClass());
    service.setByteTranslator(new HexStringByteTranslator());
    assertEquals(HexStringByteTranslator.class, service.getByteTranslator().getClass());
    LifecycleHelper.init(service);
    LifecycleHelper.close(service);
    try {
      service.setByteTranslator(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
  }

  public void testService() throws Exception {
    MetadataHashingService service = new MetadataHashingService(METADATA_KEY);
    AdaptrisMessage msg = createMessage(null);
    execute(service, msg);
    assertNotSame(METADATA_VALUE, msg.getMetadataValue(METADATA_KEY));
  }

  public void testService_KnownHash() throws Exception {
    MetadataHashingService service = new MetadataHashingService(METADATA_KEY, "MD5", new HexStringByteTranslator());
    AdaptrisMessage msg = createMessage(null);
    execute(service, msg);
    assertEquals(METADATA_HASH_MD5, msg.getMetadataValue(METADATA_KEY));
  }

  public void testService_Encoding() throws Exception {
    MetadataHashingService service = new MetadataHashingService(METADATA_KEY, "MD5", new HexStringByteTranslator());
    AdaptrisMessage msg = createMessage("UTF-8");
    execute(service, msg);
    assertEquals(METADATA_HASH_MD5, msg.getMetadataValue(METADATA_KEY));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    MetadataHashingService service = new MetadataHashingService();
    service.setMetadataKeyRegexp(".*MetadataKeyRegularExpression.*");
    return service;
  }
}
