package com.adaptris.core.metadata;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.MetadataCollection;

/**
 * @author mwarman
 */
public class OrderedItemMetadataFilterTest {

  @Test
  public void testFilter(){
    OrderedItemMetadataFilter filter = new OrderedItemMetadataFilter();
    filter.setMetadataKeys(Arrays.asList("key1", "key2", "key3"));
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    message.addMetadata("Key1", "value1");
    message.addMetadata("key3", "value3");
    MetadataCollection collection = filter.filter(message);
    assertEquals(3,collection.size());
    assertEquals("key1", collection.get(0).getKey());
    assertEquals("", collection.get(0).getValue());
    assertEquals("key2", collection.get(1).getKey());
    assertEquals("", collection.get(1).getValue());
    assertEquals("key3", collection.get(2).getKey());
    assertEquals("value3", collection.get(2).getValue());
  }

  @Test
  public void testFilterIgnoreCase(){
    OrderedItemMetadataFilter filter = new OrderedItemMetadataFilter();
    filter.setIgnoreCase(true);
    filter.setMetadataKeys(Arrays.asList("key1", "key2", "key3"));
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    message.addMetadata("Key1", "value1");
    message.addMetadata("key3", "value3");
    MetadataCollection collection = filter.filter(message);
    assertEquals(3,collection.size());
    assertEquals("key1", collection.get(0).getKey());
    assertEquals("value1", collection.get(0).getValue());
    assertEquals("key2", collection.get(1).getKey());
    assertEquals("", collection.get(1).getValue());
    assertEquals("key3", collection.get(2).getKey());
    assertEquals("value3", collection.get(2).getValue());
  }
}