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

package com.adaptris.interlok.junit.scaffolding;

import static org.junit.Assert.assertEquals;
import java.util.List;
import org.junit.Test;
import com.adaptris.core.StandaloneProducer;

public abstract class ExampleProducerCase extends ExampleConfigGenerator {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   *
   */
  public static final String BASE_DIR_KEY = "ProducerCase.baseDir";

  public ExampleProducerCase() {
    super();
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected String createExampleXml(Object object) throws Exception {
    String result = getExampleCommentHeader(object);


    StandaloneProducer producer = (StandaloneProducer) object;

    result = result + configMarshaller.marshal(producer);

    return result;
  }

  @Test
  public void testMessageEventGeneratorCreateName() throws Exception {
    Object input = retrieveObjectForCastorRoundTrip();
    if (input != null) {
      assertCreateName((StandaloneProducer) input);
    }
    else {
      List l = retrieveObjectsForSampleConfig();
      for (Object o : retrieveObjectsForSampleConfig()) {
        assertCreateName((StandaloneProducer) o);
      }
    }
  }

  private void assertCreateName(StandaloneProducer p) {
    assertEquals(p.getProducer().getClass().getName(), p.createName());
    assertEquals(p.getProducer().createName(), p.createName());
  }

  @Override
  protected String createBaseFileName(Object object) {
    return ((StandaloneProducer) object).getProducer().getClass().getName();
  }
}
