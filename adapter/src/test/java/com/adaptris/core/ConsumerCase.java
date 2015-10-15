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

import java.util.List;

public abstract class ConsumerCase extends ExampleConfigCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "ConsumerCase.baseDir";

  public ConsumerCase(String name) {
    super(name);

    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

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
}
