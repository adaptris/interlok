package com.adaptris.core.metadata;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.MetadataCollection;

public class RemoveAllMetadataFilterTest {


  @Test
  public void testFilterMessage() throws Exception {
    RemoveAllMetadataFilter filter = new RemoveAllMetadataFilter();
    MetadataCollection c = filter.filter(createMessage());
    assertEquals(0, c.size());
  }

  @Test
  public void testFilterSet() throws Exception {
    RemoveAllMetadataFilter filter = new RemoveAllMetadataFilter();
    MetadataCollection c = filter.filter(createMessage().getMetadata());
    assertEquals(0, c.size());
  }

  @Test
  public void testFilterCollection() throws Exception {
    RemoveAllMetadataFilter filter = new RemoveAllMetadataFilter();
    MetadataCollection c = filter.filter(new MetadataCollection(createMessage().getMetadata()));
    assertEquals(0, c.size());
  }

  private AdaptrisMessage createMessage() {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    message.addMetadata("key1", "value1");
    message.addMetadata("key2", "value2");
    message.addMetadata("key3", "value3");
    message.addMetadata("key4", "value4");
    message.addMetadata("key5", "value5");

    message.addMetadata("someRandomKey", "Some random value");
    message.addMetadata("JackAndJill", "Ran up some hill");
    message.addMetadata("JillAndJack", "Broke their backs");
    return message;
  }
}
