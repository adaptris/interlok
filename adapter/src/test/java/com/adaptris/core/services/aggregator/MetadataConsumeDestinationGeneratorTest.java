package com.adaptris.core.services.aggregator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.services.aggregator.ConsumeDestinationFromMetadata;

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
