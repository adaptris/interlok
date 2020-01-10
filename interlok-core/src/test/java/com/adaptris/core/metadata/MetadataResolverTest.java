package com.adaptris.core.metadata;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;

public class MetadataResolverTest {
  
  private AdaptrisMessage message;
    
  @Before
  public void setUp() throws Exception {
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
  }
  
  @Test
  public void testGetNormal() {
    message.addMessageHeader("key1", "value1");
    message.addMessageHeader("key2", "value2");
    message.addMessageHeader("key3", "value3");
    
    assertEquals("key1", MetadataResolver.resolveKey(message, "key1"));
    assertEquals("key2", MetadataResolver.resolveKey(message, "key2"));
    assertEquals("key3", MetadataResolver.resolveKey(message, "key3"));
  }

  @Test
  public void testGetReferenced() {
    message.addMessageHeader("key1", "value1");
    message.addMessageHeader("key2", "value2");
    message.addMessageHeader("key3", "value3");
    
    assertEquals("value1", MetadataResolver.resolveKey(message, "$$key1"));
    assertEquals("value2", MetadataResolver.resolveKey(message, "$$key2"));
    assertEquals("value3", MetadataResolver.resolveKey(message, "$$key3"));
  }
  
}
