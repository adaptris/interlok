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
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.fs.FsMessageProducerTest;
import com.adaptris.core.fs.FsProducer;

public class ProduceOnlyProducerImpTest extends BaseCase {

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }


  private ProduceOnlyProducerImp producer;

  @Before
  public void setUp() throws Exception {
    String destinationString = "/tgt";
    String baseString = PROPERTIES.getProperty(FsMessageProducerTest.BASE_KEY);
    // create producer
    producer = new FsProducer().withBaseDirectoryUrl(baseString + destinationString);
    ((FsProducer) producer).setCreateDirs(true);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testRequestThrowsException() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage("dummy");
    start(producer);
    try {
      try {
        producer.request(msg);
        fail("Request reply succeeded");
      }
      catch (UnsupportedOperationException expected) {
        assertEquals("Request Reply is not supported", expected.getMessage());
      }
      try {
        producer.request(msg, 10000L);
        fail("Request reply succeeded");
      }
      catch (UnsupportedOperationException expected) {
        assertEquals("Request Reply is not supported", expected.getMessage());
      }
      try {
        producer.request(msg, new ConfiguredProduceDestination(PROPERTIES
            .getProperty(FsMessageProducerTest.BASE_KEY)));
        fail("Request reply succeeded");
      }
      catch (UnsupportedOperationException expected) {
        assertEquals("Request Reply is not supported", expected.getMessage());
      }
      try {
        producer.request(msg, new ConfiguredProduceDestination(PROPERTIES
            .getProperty(FsMessageProducerTest.BASE_KEY)), 10000L);
        fail("Request reply succeeded");
      }
      catch (UnsupportedOperationException expected) {
        assertEquals("Request Reply is not supported", expected.getMessage());
      }
    }
    finally {
      stop(producer);
    }

  }
}
