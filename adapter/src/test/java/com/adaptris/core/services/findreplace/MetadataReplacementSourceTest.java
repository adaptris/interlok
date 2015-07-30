package com.adaptris.core.services.findreplace;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class MetadataReplacementSourceTest {

  private AdaptrisMessage createMessage() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("");
    msg.addMetadata("key", "val");
    return msg;
  }

  @Test
  public void testNullMessage() throws Exception {
    MetadataReplacementSource source = new MetadataReplacementSource();
    try {
      source.obtainValue(null);
      fail("null did not throw Exception handled correctly");
    }
    catch (NullPointerException e) {
      // expected
    }
  }

  @Test
  public void testMissingKey() throws Exception {
    MetadataReplacementSource source = new MetadataReplacementSource();
    source.setValue("not-there");
    
    String replaceWith = source.obtainValue(createMessage());
    assertTrue(null == replaceWith);
  }

  @Test
  public void testValidKey() throws Exception {
    MetadataReplacementSource source = new MetadataReplacementSource();
    source.setValue("key");
    String replaceWith = source.obtainValue(createMessage());
    assertTrue("val".equals(replaceWith));
  }
}
