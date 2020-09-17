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

import com.adaptris.core.ProduceDestination;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.stubs.DummyMessageProducer;

/**
 * Generating Examples that contain ProduceDestinations.
 */
public abstract class ExampleProduceDestinationCase extends ExampleConfigGenerator {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   *
   */
  public static final String BASE_DIR_KEY = "ProduceDestinationCase.baseDir";

  public ExampleProduceDestinationCase() {
    super();

    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected String createExampleXml(Object object) throws Exception {
    String result = getExampleCommentHeader(object);
    StandaloneProducer w = (StandaloneProducer) object;
    w.getProducer().setMessageFactory(null);
    result = result + configMarshaller.marshal(w);
    return result;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StandaloneProducer(new DummyMessageProducer(createDestinationForExamples()));
  }

  protected abstract ProduceDestination createDestinationForExamples();

  @Override
  protected String getExampleCommentHeader(Object object) {
    return "<!--\n\nThis example simply shows the usage for a particular ProduceDestination;"
        + "\nthe wrapping producer may not be suitable for the destination at all."
        + "\nAs always, check the javadocs for more information." + "\n\n-->\n";
  }

  @Override
  @SuppressWarnings("deprecation")
  protected String createBaseFileName(Object object) {
    DummyMessageProducer p = (DummyMessageProducer) ((StandaloneProducer) object).getProducer();
    return p.getDestination().getClass().getCanonicalName();
  }

}
