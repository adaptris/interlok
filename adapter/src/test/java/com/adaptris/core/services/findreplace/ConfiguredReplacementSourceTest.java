package com.adaptris.core.services.findreplace;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessageFactory;

public class ConfiguredReplacementSourceTest {

  @Test
  public void testObtainReplaceWith() throws Exception {
    ConfiguredReplacementSource source = new ConfiguredReplacementSource();
    source.setValue("value");
    String replaceWith = source.obtainValue(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    assertEquals("value", replaceWith);
  }

  @Test
  public void testObtainReplaceWith_NullMessage() throws Exception {
    ConfiguredReplacementSource source = new ConfiguredReplacementSource();
    source.setValue("value");
    String replaceWith = source.obtainValue(null);
    assertEquals("value", replaceWith);
  }

}
