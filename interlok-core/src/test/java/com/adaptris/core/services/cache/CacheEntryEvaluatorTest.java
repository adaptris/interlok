package com.adaptris.core.services.cache;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.cache.CacheEntryEvaluator.NullCacheValueTranslator;
import com.adaptris.core.services.cache.translators.MetadataCacheValueTranslator;
import com.adaptris.core.services.cache.translators.StaticCacheValueTranslator;

public class CacheEntryEvaluatorTest extends com.adaptris.interlok.junit.scaffolding.BaseCase {
  private static final String DEFAULT_VALUE = "value";
  private static final String DEFAULT_METADATA_KEY = "key";

  public CacheEntryEvaluatorTest() {
  }

  @Test
  public void testSetKeyTranslator() throws Exception {
    CacheEntryEvaluator eval = new CacheEntryEvaluator();
    assertNull(eval.getKeyTranslator());
    assertNotNull(eval.keyTranslator());
    MetadataCacheValueTranslator translator = new MetadataCacheValueTranslator(DEFAULT_METADATA_KEY);
    eval.setKeyTranslator(translator);
    assertEquals(translator, eval.getKeyTranslator());
    assertEquals(translator, eval.keyTranslator());

    try {
      eval.setKeyTranslator(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals(translator, eval.getKeyTranslator());
    assertEquals(translator, eval.keyTranslator());
  }

  @Test
  public void testSetValueTranslator() throws Exception {
    CacheEntryEvaluator eval = new CacheEntryEvaluator();
    assertNull(eval.getValueTranslator());
    assertNotNull(eval.valueTranslator());
    MetadataCacheValueTranslator translator = new MetadataCacheValueTranslator(DEFAULT_METADATA_KEY);
    eval.setValueTranslator(translator);
    assertEquals(translator, eval.getValueTranslator());
    assertEquals(translator, eval.valueTranslator());

    try {
      eval.setValueTranslator(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals(translator, eval.getValueTranslator());
    assertEquals(translator, eval.valueTranslator());

  }

  @Test
  public void testSetErrorOnEmptyKey() throws Exception {
    CacheEntryEvaluator eval = new CacheEntryEvaluator();
    assertNull(eval.getErrorOnEmptyKey());
    assertEquals(true, eval.errorOnEmptyKey());

    eval.setErrorOnEmptyKey(Boolean.FALSE);
    assertEquals(Boolean.FALSE, eval.getErrorOnEmptyKey());
    assertEquals(false, eval.errorOnEmptyKey());

    eval.setErrorOnEmptyKey(null);
    assertNull(eval.getErrorOnEmptyKey());
    assertEquals(true, eval.errorOnEmptyKey());

  }

  @Test
  public void testSetErrorOnEmptyValue() throws Exception {
    CacheEntryEvaluator eval = new CacheEntryEvaluator();
    assertNull(eval.getErrorOnEmptyValue());
    assertEquals(true, eval.errorOnEmptyValue());

    eval.setErrorOnEmptyValue(Boolean.FALSE);
    assertEquals(Boolean.FALSE, eval.getErrorOnEmptyValue());
    assertEquals(false, eval.errorOnEmptyValue());

    eval.setErrorOnEmptyValue(null);
    assertNull(eval.getErrorOnEmptyValue());
    assertEquals(true, eval.errorOnEmptyValue());
  }

  @Test
  public void testSetFriendlyName() throws Exception {
    CacheEntryEvaluator eval = new CacheEntryEvaluator();
    assertNull(eval.getFriendlyName());
    assertEquals(CacheEntryEvaluator.class.getSimpleName(), eval.friendlyName());

    eval.setFriendlyName("XXX");
    assertEquals("XXX", eval.getFriendlyName());
    assertEquals("XXX", eval.friendlyName());
    eval.setFriendlyName(null);
    assertNull(eval.getFriendlyName());
    assertEquals(CacheEntryEvaluator.class.getSimpleName(), eval.friendlyName());

  }

  @Test
  public void testEvaluateNoTranslators() throws Exception {
    CacheEntryEvaluator eval = new CacheEntryEvaluator();
    AdaptrisMessage msg = createMessage(new ArrayList<MetadataElement>());
    try {
      eval.getKey(msg);
      fail();
    }
    catch (ServiceException e) {

    }
    try {
      eval.getValue(msg);
      fail();
    }
    catch (ServiceException e) {

    }
  }

  @Test
  public void testEvaluateKey() throws Exception {
    testEvaluateKey_ErrorOnEmpty_NonEmptyValue();
  }

  @Test
  public void testEvaluateKey_WithError() throws Exception {
    CacheEntryEvaluator eval = new CacheEntryEvaluator();
    StaticCacheValueTranslator translator = new StaticCacheValueTranslator() {
      @Override
      public String getValueFromMessage(AdaptrisMessage msg) throws CoreException {
        throw new UnsupportedOperationException();
      }
    };
    eval.setKeyTranslator(translator);
    AdaptrisMessage msg = createMessage(new ArrayList<MetadataElement>());
    // We don't actually *expect* to catch an exception here.
    // The code just returns null in the event we can't get the value from the AdaptrisMessage.
    assertNull(eval.getKey(msg));
  }

  @Test
  public void testEvaluateKey_ErrorOnEmpty_EmptyValue() throws Exception {
    CacheEntryEvaluator eval = new CacheEntryEvaluator();
    MetadataCacheValueTranslator translator = new MetadataCacheValueTranslator(DEFAULT_METADATA_KEY);
    eval.setKeyTranslator(translator);
    eval.setErrorOnEmptyKey(true);
    AdaptrisMessage msg = createMessage(new ArrayList<MetadataElement>());
    try {
      eval.getKey(msg);
      fail();
    }
    catch (ServiceException expected) {

    }

  }

  @Test
  public void testEvaluateKey_ErrorOnEmpty_NonEmptyValue() throws Exception {
    CacheEntryEvaluator eval = new CacheEntryEvaluator();
    MetadataCacheValueTranslator translator = new MetadataCacheValueTranslator(DEFAULT_METADATA_KEY);
    eval.setKeyTranslator(translator);
    AdaptrisMessage msg = createMessage(Arrays.asList(new MetadataElement[]
    {
      new MetadataElement(DEFAULT_METADATA_KEY, DEFAULT_VALUE)
    }));
    assertEquals(DEFAULT_VALUE, eval.getKey(msg));
  }

  @Test
  public void testEvaluateKey_NoErrorOnEmpty_EmptyValue() throws Exception {
    CacheEntryEvaluator eval = new CacheEntryEvaluator();
    MetadataCacheValueTranslator translator = new MetadataCacheValueTranslator(DEFAULT_METADATA_KEY);
    eval.setKeyTranslator(translator);
    eval.setErrorOnEmptyKey(false);
    AdaptrisMessage msg = createMessage(new ArrayList<MetadataElement>());
    assertEquals(null, eval.getKey(msg));
  }

  @Test
  public void testEvaluateValue() throws Exception {
    testEvaluateValue_ErrorOnEmpty_NonEmptyValue();
  }

  @Test
  public void testEvaluateValue_WithError() throws Exception {
    CacheEntryEvaluator eval = new CacheEntryEvaluator();
    CacheValueTranslator translator = new StaticCacheValueTranslator() {
      @Override
      public String getValueFromMessage(AdaptrisMessage msg) throws CoreException {
        throw new UnsupportedOperationException();
      }
    };
    eval.setValueTranslator(translator);
    AdaptrisMessage msg = createMessage(new ArrayList<MetadataElement>());
    // We don't actually *expect* to catch an exception here.
    // The code just returns null in the event we can't get the value from the AdaptrisMessage.
    assertNull(eval.getValue(msg));
  }

  @Test
  public void testEvaluateValue_ErrorOnEmpty_EmptyValue() throws Exception {
    CacheEntryEvaluator eval = new CacheEntryEvaluator();
    CacheValueTranslator translator = new MetadataCacheValueTranslator(DEFAULT_METADATA_KEY);
    eval.setValueTranslator(translator);
    eval.setErrorOnEmptyValue(true);
    AdaptrisMessage msg = createMessage(new ArrayList<MetadataElement>());
    try {
      eval.getValue(msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  @Test
  public void testEvaluateValue_ErrorOnEmpty_NonEmptyValue() throws Exception {
    CacheEntryEvaluator eval = new CacheEntryEvaluator();
    CacheValueTranslator translator = new MetadataCacheValueTranslator(DEFAULT_METADATA_KEY);
    eval.setValueTranslator(translator);
    AdaptrisMessage msg = createMessage(Arrays.asList(new MetadataElement[]
    {
      new MetadataElement(DEFAULT_METADATA_KEY, DEFAULT_VALUE)
    }));
    assertEquals(DEFAULT_VALUE, eval.getValue(msg));
  }

  @Test
  public void testEvaluateValue_NoErrorOnEmpty_EmptyValue() throws Exception {
    CacheEntryEvaluator eval = new CacheEntryEvaluator();
    CacheValueTranslator translator = new MetadataCacheValueTranslator(DEFAULT_METADATA_KEY);
    eval.setValueTranslator(translator);
    eval.setErrorOnEmptyValue(false);
    AdaptrisMessage msg = createMessage(new ArrayList<MetadataElement>());
    assertEquals(null, eval.getValue(msg));
  }

  @Test
  public void testNullCacheValueTranslator() throws Exception {
    NullCacheValueTranslator cvt = new CacheEntryEvaluator.NullCacheValueTranslator();
    assertNull(cvt.getValueFromMessage(createMessage(Collections.EMPTY_SET)));
    cvt.addValueToMessage(createMessage(Collections.EMPTY_SET), null);
  }

  protected AdaptrisMessage createMessage(Collection<MetadataElement> metadata) {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("blah");
    for (MetadataElement element : metadata) {
      msg.addMetadata(element);
    }
    return msg;
  }

}
