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

package com.adaptris.core.services.metadata.compare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.metadata.MetadataServiceExample;
import com.adaptris.core.util.LifecycleHelper;

public class MetadataComparisonServiceTest extends MetadataServiceExample {

  private static final String EXAMPLE_RESULT_KEY = "metadata key that will contain the result post service";
  private static final String KEY_1 = "key1";
  private static final String KEY_2 = "key2";

  private enum ComparatorCreator {
    CompareTimestamps {

      @Override
      CompareTimestamps create() {
        return new CompareTimestamps(EXAMPLE_RESULT_KEY);
      }

    },
    EndsWith {

      @Override
      EndsWith create() {
        return new EndsWith(EXAMPLE_RESULT_KEY);
      }

    },
    EndsWithIgnoreCase {

      @Override
      EndsWithIgnoreCase create() {
        return new EndsWithIgnoreCase(EXAMPLE_RESULT_KEY);
      }

    },

    StartsWith {

      @Override
      StartsWith create() {
        return new StartsWith(EXAMPLE_RESULT_KEY);
      }

    },
    StartsWithIgnoreCase {

      @Override
      StartsWithIgnoreCase create() {
        return new StartsWithIgnoreCase(EXAMPLE_RESULT_KEY);
      }

    },

    Contains {

      @Override
      Contains create() {
        return new Contains(EXAMPLE_RESULT_KEY);
      }

    },
    ContainsIgnoreCase {

      @Override
      ContainsIgnoreCase create() {
        return new ContainsIgnoreCase(EXAMPLE_RESULT_KEY);
      }

    },
    
    Equals {

      @Override
      Equals create() {
        return new Equals(EXAMPLE_RESULT_KEY);
      }
      
    },
    
    EqualsIgnoreCase {

      @Override
      EqualsIgnoreCase create() {
        return new EqualsIgnoreCase(EXAMPLE_RESULT_KEY);
      }
    };
    abstract ComparatorImpl create();

  };


  @Test
  public void testInit() throws Exception {
    MetadataComparisonService s = new MetadataComparisonService();
    assertNull(s.getFirstKey());
    assertNull(s.getSecondKey());
    assertNull(s.getComparator());
    try {
      try {
        LifecycleHelper.init(s);
      }
      catch (CoreException expected) {

      }
      s.setFirstKey(getName());
      try {
        LifecycleHelper.init(s);
      }
      catch (CoreException expected) {

      }
      s.setSecondKey(getName());
      try {
        LifecycleHelper.init(s);
      }
      catch (CoreException expected) {

      }
      s.setComparator(new Equals());
      LifecycleHelper.init(s);
    }
    finally {
      stop(s);
    }
  }

  @Test
  public void testStartsWith() throws Exception {
    MetadataComparisonService s = new MetadataComparisonService(KEY_1, KEY_2, new StartsWith());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(KEY_1, "abc");
    msg.addMetadata(KEY_2, "a");
    execute(s, msg);
    assertEquals("true", msg.getMetadataValue(StartsWith.class.getCanonicalName()));
  }

  @Test
  public void testStartsWithIgnoreCase() throws Exception {
    MetadataComparisonService s = new MetadataComparisonService(KEY_1, KEY_2, new StartsWithIgnoreCase());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(KEY_1, "abc");
    msg.addMetadata(KEY_2, "A");
    execute(s, msg);
    assertEquals("true", msg.getMetadataValue(StartsWithIgnoreCase.class.getCanonicalName()));
  }

  @Test
  public void testEndsWith() throws Exception {
    MetadataComparisonService s = new MetadataComparisonService(KEY_1, KEY_2, new EndsWith());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(KEY_1, "abc");
    msg.addMetadata(KEY_2, "c");
    execute(s, msg);
    assertEquals("true", msg.getMetadataValue(EndsWith.class.getCanonicalName()));
  }

  @Test
  public void testEndsWithIgnoreCase() throws Exception {
    MetadataComparisonService s = new MetadataComparisonService(KEY_1, KEY_2, new EndsWithIgnoreCase());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(KEY_1, "abc");
    msg.addMetadata(KEY_2, "C");
    execute(s, msg);
    assertEquals("true", msg.getMetadataValue(EndsWithIgnoreCase.class.getCanonicalName()));
  }

  @Test
  public void testContains() throws Exception {
    MetadataComparisonService s = new MetadataComparisonService(KEY_1, KEY_2, new Contains());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(KEY_1, "abc");
    msg.addMetadata(KEY_2, "a");
    execute(s, msg);
    assertEquals("true", msg.getMetadataValue(Contains.class.getCanonicalName()));
  }

  @Test
  public void testContainsIgnoreCase() throws Exception {
    MetadataComparisonService s = new MetadataComparisonService(KEY_1, KEY_2, new ContainsIgnoreCase());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(KEY_1, "abc");
    msg.addMetadata(KEY_2, "A");
    execute(s, msg);
    assertEquals("true", msg.getMetadataValue(ContainsIgnoreCase.class.getCanonicalName()));
  }

  @Test
  public void testEquals() throws Exception {
    MetadataComparisonService s = new MetadataComparisonService(KEY_1, KEY_2, new Equals());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(KEY_1, "abc");
    msg.addMetadata(KEY_2, "def");
    execute(s, msg);
    assertEquals("false", msg.getMetadataValue(Equals.class.getCanonicalName()));
  }

  @Test
  public void testEqualsIgnoreCase() throws Exception {
    MetadataComparisonService s = new MetadataComparisonService(KEY_1, KEY_2, new EqualsIgnoreCase());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(KEY_1, "abc");
    msg.addMetadata(KEY_2, "ABC");
    execute(s, msg);
    assertEquals("true", msg.getMetadataValue(EqualsIgnoreCase.class.getCanonicalName()));
  }

  @Test
  public void testCompareTimestamp() throws Exception {
    MetadataComparisonService s = new MetadataComparisonService(KEY_1, KEY_2, new CompareTimestamps());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    msg.addMetadata(KEY_1, sdf.format(new Date(1)));
    msg.addMetadata(KEY_2, sdf.format(new Date()));
    execute(s, msg);
    assertEquals("-1", msg.getMetadataValue(CompareTimestamps.class.getCanonicalName()));
  }

  @Test
  public void testCompareTimestamp_WithFormat() throws Exception {
    MetadataComparisonService s =
        new MetadataComparisonService(KEY_1, KEY_2, new CompareTimestamps("CompareTimestamps", "yyyy-MM-dd"));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    msg.addMetadata(KEY_1, sdf.format(new Date(1)));
    msg.addMetadata(KEY_2, sdf.format(new Date()));
    execute(s, msg);
    assertEquals("-1", msg.getMetadataValue("CompareTimestamps"));
  }

  @Test
  public void testCompareTimestamp_Fails() throws Exception {
    MetadataComparisonService s =
        new MetadataComparisonService(KEY_1, KEY_2, new CompareTimestamps());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    msg.addMetadata(KEY_1, sdf.format(new Date(1)));
    msg.addMetadata(KEY_2, sdf.format(new Date()));
    try {
      execute(s, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    List<Service> list = new ArrayList<>();
    for (ComparatorCreator h : ComparatorCreator.values()) {
      list.add(new MetadataComparisonService("first metadata key", "second metadata key", h.create()));
    }
    return list;
  }

  @Override
  protected String createBaseFileName(Object object) {
    MetadataComparisonService s = (MetadataComparisonService) object;
    return super.createBaseFileName(object) + "-" + s.getComparator().getClass().getSimpleName();
  }
}
