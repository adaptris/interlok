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
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import com.adaptris.core.util.LifecycleHelper;

public abstract class ConsumerCase extends ExampleConfigCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "ConsumerCase.baseDir";

  public ConsumerCase() {
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Test
  public void testMessageEventGeneratorCreateName() throws Exception {
    Object input = retrieveObjectForCastorRoundTrip();
    if (input != null) {
      assertCreateName((StandaloneConsumer) input);
    }
    else {
      List l = retrieveObjectsForSampleConfig();
      for (Object o : retrieveObjectsForSampleConfig()) {
        assertCreateName((StandaloneConsumer) o);
      }
    }
  }

  private void assertCreateName(StandaloneConsumer p) {
    assertEquals(p.getConsumer().getClass().getName(), p.createName());
    assertEquals(p.getConsumer().getClass().getName(), p.getConsumer().createName());
  }

  @Override
  protected String createExampleXml(Object object) throws Exception {
    String result = getExampleCommentHeader(object);

    StandaloneConsumer consumer = (StandaloneConsumer) object;

    result = result + configMarshaller.marshal(consumer);

    result = result.replaceAll("com\\.adaptris\\.core\\.Standalone-consumer",
        "Dummy-Root-Element");
    return result;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return ((StandaloneConsumer) object).getConsumer().getClass().getName();
  }

  protected static long waitForPollCallback(AtomicBoolean pollFired) throws Exception {
    long totalWaitTime = 0;
    while (!pollFired.get() && totalWaitTime < MAX_WAIT) {
      long wait = (long) new Random().nextInt(100) + 1;
      LifecycleHelper.waitQuietly(wait);
      totalWaitTime += wait;
    }
    return totalWaitTime;
  }

}
