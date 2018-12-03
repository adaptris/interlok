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

public class ConfiguredProduceDestinationTest extends ExampleProduceDestinationCase {

  public ConfiguredProduceDestinationTest(java.lang.String testName) {
    super(testName);
  }

  public void testGetDestination() {
    ConfiguredProduceDestination dest1 = new ConfiguredProduceDestination("1");
    assertEquals("1", dest1.getDestination());
  }

  public void testSetDestination() {
    ConfiguredProduceDestination dest1 = new ConfiguredProduceDestination();
    dest1.setDestination("3");
    assertEquals("3", dest1.getDestination());
    try {
      dest1.setDestination(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
  }

  @Override
  protected ProduceDestination createDestinationForExamples() {
    return new ConfiguredProduceDestination("The_Destination_String");
  }

  @Override
  protected String getExampleCommentHeader(Object object) {
    return super.getExampleCommentHeader(object) + "<!--\n\nThis is the simplest ProduceDestination implementation"
        + "\nSimply configure a string that has some meaning for the producer in question and it will be used." + "\n\n-->\n";
  }

}
