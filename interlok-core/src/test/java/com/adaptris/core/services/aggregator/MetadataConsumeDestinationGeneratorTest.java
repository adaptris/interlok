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

package com.adaptris.core.services.aggregator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.DefaultMessageFactory;

public class MetadataConsumeDestinationGeneratorTest {
  private static final String DEFAULT_FILTER_KEY = "metadataFilterKey";
  private static final String DEFAULT_DESTINATION_KEY = "destinationFilterKey";

  @Test
  public void testSetDestination() {
    ConsumeDestinationFromMetadata dest = new ConsumeDestinationFromMetadata();
    assertNull(dest.getDefaultDestination());
    dest.setDefaultDestination("");
    assertEquals("", dest.getDefaultDestination());
  }

  @Test
  public void testSetDestinationMetadataKey() {
    ConsumeDestinationFromMetadata dest = new ConsumeDestinationFromMetadata();
    assertNull(dest.getDestinationMetadataKey());
    dest.setDestinationMetadataKey("abder");
    assertEquals("abder", dest.getDestinationMetadataKey());
  }

  @Test
  public void testSetFilterExpression() {
    ConsumeDestinationFromMetadata dest = new ConsumeDestinationFromMetadata();
    dest.setDefaultFilterExpression(null);
    assertNull(dest.getDefaultFilterExpression());
    dest.setDefaultFilterExpression("");
    assertEquals("", dest.getDefaultFilterExpression());
  }

  @Test
  public void testSetFilterMetadataKey() {
    ConsumeDestinationFromMetadata dest = new ConsumeDestinationFromMetadata();
    assertNull(dest.getFilterMetadataKey());
    dest.setFilterMetadataKey("abcde");
    assertEquals("abcde", dest.getFilterMetadataKey());
  }

  @Test
  public void testGenerate() {
    ConsumeDestinationFromMetadata dest = new ConsumeDestinationFromMetadata();
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    dest.setDestinationMetadataKey(DEFAULT_DESTINATION_KEY);
    dest.setFilterMetadataKey(DEFAULT_FILTER_KEY);
    msg.addMetadata(DEFAULT_FILTER_KEY, "filterMetadataValue");
    msg.addMetadata(DEFAULT_DESTINATION_KEY, "destinationMetadataValue");
    ConsumeDestination cd = dest.generate(msg);
    assertEquals("filterMetadataValue", cd.getFilterExpression());
    assertEquals("destinationMetadataValue", cd.getDestination());

  }

  @Test
  public void testGetGenerateDefaults() {
    ConsumeDestinationFromMetadata dest = new ConsumeDestinationFromMetadata();
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    msg.addMetadata(DEFAULT_FILTER_KEY, "filterMetadataValue");
    msg.addMetadata(DEFAULT_DESTINATION_KEY, "destinationMetadataValue");

    dest.setFilterMetadataKey("unknownKey");
    dest.setDestinationMetadataKey("unknownKey");
    dest.setDefaultFilterExpression("defaultFilterValue");
    dest.setDefaultDestination("defaultDestinationValue");
    ConsumeDestination cd = dest.generate(msg);
    assertEquals("defaultFilterValue", cd.getFilterExpression());
    assertEquals("defaultDestinationValue", cd.getDestination());
  }

}
