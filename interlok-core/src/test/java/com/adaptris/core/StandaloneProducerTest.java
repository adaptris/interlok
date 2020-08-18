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

package com.adaptris.core;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.fs.FsProducer;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.LifecycleHelper;

public class StandaloneProducerTest extends GeneralServiceExample {


  @Test
  public void testDoService() throws Exception {
    MockMessageProducer m = new MockMessageProducer();
    StandaloneProducer service = new StandaloneProducer(m);
    service.setProducer(m);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("XYZ");
    execute(service, msg);
    assertEquals(1, m.getMessages().size());
  }

  @Test
  public void testDoProduce() throws Exception {
    MockMessageProducer m = new MockMessageProducer();
    StandaloneProducer service = new StandaloneProducer(m);
    service.setProducer(m);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("XYZ");
    try {
      start(service);
      service.produce(msg);
      assertEquals(1, m.getMessages().size());
    }
    finally {
      stop(service);
    }
  }

  @Test
  public void testDoProduceWithDest() throws Exception {
    MockMessageProducer m = new MockMessageProducer();
    StandaloneProducer service = new StandaloneProducer(m);
    service.setProducer(m);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("XYZ");
    try {
      start(service);
      service.produce(msg, new ConfiguredProduceDestination("ThisIsTheDest"));
      assertEquals(1, m.getMessages().size());
    }
    finally {
      stop(service);
    }
  }

  @Test
  public void testCreateName() throws Exception {
    StandaloneProducer service = new StandaloneProducer(new MockMessageProducer());
    assertEquals(MockMessageProducer.class.getName(), service.createName());
    assertEquals(service.getProducer().createName(), service.createName());
  }

  @Test
  public void testCreateQualifier() throws Exception {
    FsProducer mp = new FsProducer();
    mp.setUniqueId("abc");
    StandaloneProducer service = new StandaloneProducer(mp);
    assertEquals("abc", service.createQualifier());
    assertEquals(service.getProducer().createQualifier(), service.createQualifier());
    mp.setUniqueId("");
    service.setUniqueId(StandaloneProducer.class.getSimpleName());
    assertEquals(StandaloneProducer.class.getSimpleName(), service.createQualifier());
    assertNotSame(service.getProducer().createQualifier(), service.createQualifier());
  }

  @Test
  public void testSetConnection() throws Exception {
    StandaloneProducer service = new StandaloneProducer();
    try {
      service.setConnection(null);
      fail("Connection allows nulls");
    }
    catch (IllegalArgumentException e) {
      ;
    }
  }

  @Test
  public void testSetProducer() throws Exception {
    StandaloneProducer service = new StandaloneProducer();
    try {
      service.setProducer(null);
      fail("Producer allows nulls");
    }
    catch (IllegalArgumentException e) {
      ;
    }
  }

  @Test
  public void testBackReferences() throws Exception {
    StandaloneProducer producer = new StandaloneProducer();
    NullConnection conn = new NullConnection();
    producer.setConnection(conn);
    assertEquals(conn, producer.getConnection());
    // No longer true because of redmineID #4452
    // assertEquals(1, conn.retrieveExceptionListeners().size());
    // assertTrue(producer == conn.retrieveExceptionListeners().toArray()[0]);
    LifecycleHelper.init(producer);
    // Now it's true again.
    assertEquals(1, conn.retrieveExceptionListeners().size());
    assertTrue(producer == conn.retrieveExceptionListeners().toArray()[0]);

    // Now marshall and see if it's the same.
    XStreamMarshaller m = new XStreamMarshaller();
    String xml = m.marshal(producer);
    StandaloneProducer producer2 = (StandaloneProducer) m.unmarshal(xml);
    // If the setter has been used, then these two will be "true"
    assertNotNull(producer2.getConnection());
    LifecycleHelper.init(producer2);
    assertEquals(1, producer2.getConnection().retrieveExceptionListeners().size());
    assertTrue(producer2 == producer2.getConnection().retrieveExceptionListeners().toArray()[0]);

  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StandaloneProducer();
  }
}
