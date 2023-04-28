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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.adaptris.util.GuidGenerator;
import com.adaptris.util.PseudoRandomIdGenerator;

public abstract class AdaptrisMessageFactoryImplCase {

  protected static final String TEST_PAYLOAD = "test payload";

  protected abstract AdaptrisMessageFactory getMessageFactory();

  @Test
  public void testCreateWithEncoding() {
    AdaptrisMessageFactory encodedMF = getMessageFactory();
    encodedMF.setDefaultCharEncoding("ISO-8859-1");

    AdaptrisMessage m1 = encodedMF.newMessage(TEST_PAYLOAD);
    assertNotNull(m1.getContentEncoding());
    assertEquals(m1.getContentEncoding(), "ISO-8859-1");
    assertTrue(m1.getContent().equals(TEST_PAYLOAD));

    // encodedMF.setDefaultCharEncoding("");
    // AdaptrisMessage m2 = encodedMF.newMessage(TEST_PAYLOAD);
    // assertNull(m2.getCharEncoding());

    encodedMF.setDefaultCharEncoding(null);
    AdaptrisMessage m3 = encodedMF.newMessage(TEST_PAYLOAD);
    assertNull(m3.getContentEncoding());
  }

  @Test
  public void testCreateBytesSet() {
    byte[] pld = TEST_PAYLOAD.getBytes();

    Set mtd = new HashSet();
    mtd.add(new MetadataElement("key1", "val1"));
    mtd.add(new MetadataElement("key2", "val2"));

    AdaptrisMessage msg = getMessageFactory().newMessage(pld, mtd);

    // fails array equals() is ==
    // assertTrue(msg.getPayload().equals(pld));

    // therefore test using Strings...
    assertTrue(new String(msg.getPayload()).equals(new String(pld)));
    assertTrue(msg.getMetadata().equals(mtd));
  }

  @Test
  public void testCreateBytes() {
    byte[] pld = TEST_PAYLOAD.getBytes();

    AdaptrisMessage msg = getMessageFactory().newMessage(pld);

    // fails array equals() is ==
    // assertTrue(msg.getPayload().equals(pld));

    // therefore test using Strings...
    // assertNotNull(msg.getCharEncoding());
    assertTrue(new String(msg.getPayload()).equals(new String(pld)));
  }

  @Test
  public void testCreateStringSet() {
    String pld = TEST_PAYLOAD;

    Set mtd = new HashSet();
    mtd.add(new MetadataElement("key1", "val1"));
    mtd.add(new MetadataElement("key2", "val2"));

    AdaptrisMessage msg = getMessageFactory().newMessage(pld, mtd);

    assertTrue(msg.getContent().equals(pld));
    assertTrue(msg.getMetadata().equals(mtd));
  }

  @Test
  public void testCreateString() {
    String pld = TEST_PAYLOAD;

    AdaptrisMessage msg = getMessageFactory().newMessage(pld);
    assertTrue(msg.getContent().equals(pld));
  }

  @Test
  public void testCreateStringStringSet() throws UnsupportedEncodingException {
    String pld = TEST_PAYLOAD;
    String enc = "ISO-8859-1";

    Set mtd = new HashSet();
    mtd.add(new MetadataElement("key1", "val1"));
    mtd.add(new MetadataElement("key2", "val2"));

    AdaptrisMessage msg = getMessageFactory().newMessage(pld, enc, mtd);

    assertTrue(new String(msg.getPayload(), enc).equals(pld));
    assertTrue(msg.getMetadata().equals(mtd));

  }

  @Test
  public void testCreateStringString() throws UnsupportedEncodingException {
    String pld = TEST_PAYLOAD;
    String enc = "ISO-8859-1";

    AdaptrisMessage msg = getMessageFactory().newMessage(pld, enc);

    assertTrue(new String(msg.getPayload(), enc).equals(pld));

  }

  @Test
  public void testCreateMessageFromSource() throws Exception {
    String pld = TEST_PAYLOAD;
    AdaptrisMessage orig = getMessageFactory().newMessage(pld);
    orig.addMetadata(new MetadataElement("key1", "val1"));
    orig.addMetadata(new MetadataElement("key2", "val2"));
    orig.addEvent(new MessageEventGenerator() {

      @Override
      public String createName() {
        return "event";
      }

      @Override
      public String createQualifier() {
        return "qualifier";
      }
      public Boolean getIsTrackingEndpoint() {
        return Boolean.FALSE;
      }

      @Override
      public boolean isTrackingEndpoint() {
        return false;
      }

      public void setIsTrackingEndpoint(Boolean b) {
      }
    }, true);

    List keysToKeep = Arrays.asList(new String[]
    {
        "key1", "key3"
    });
    AdaptrisMessage dest = getMessageFactory().newMessage(orig, keysToKeep);
    assertEquals(0, dest.getPayload().length);
    assertEquals(orig.getMetadataValue("key1"), dest
        .getMetadataValue("key1"));
    assertFalse(orig.headersContainsKey("key3"));
    assertFalse(dest.headersContainsKey("key3"));
    assertEquals(orig.getUniqueId(), dest.getUniqueId());
    assertEquals(orig.getMessageLifecycleEvent()
        .getMessageUniqueId(), dest.getMessageLifecycleEvent()
        .getMessageUniqueId());
    assertEquals(orig.getMessageLifecycleEvent().getMleMarkers()
        .size(), dest.getMessageLifecycleEvent().getMleMarkers().size());
  }

  @Test
  public void testCreateMessageFromSource_NullKeysToKeep() throws Exception {
    String pld = TEST_PAYLOAD;
    AdaptrisMessage orig = getMessageFactory().newMessage(pld);
    orig.addMetadata(new MetadataElement("key1", "val1"));
    orig.addMetadata(new MetadataElement("key2", "val2"));
    orig.addEvent(new MessageEventGenerator() {

      @Override
      public String createName() {
        return "event";
      }

      @Override
      public String createQualifier() {
        return "qualifier";
      }

      public Boolean getIsTrackingEndpoint() {
        return Boolean.FALSE;
      }

      @Override
      public boolean isTrackingEndpoint() {
        return false;
      }
      public void setIsTrackingEndpoint(Boolean b) {}
    }, true);

    AdaptrisMessage dest = getMessageFactory().newMessage(orig, null);
    assertEquals(0, dest.getPayload().length);
    assertEquals(orig.getMetadataValue("key1"), dest.getMetadataValue("key1"));
    assertTrue(orig.headersContainsKey("key2"));
    assertTrue(dest.headersContainsKey("key2"));
    assertEquals(orig.getUniqueId(), dest.getUniqueId());
    assertEquals(orig.getMessageLifecycleEvent().getMessageUniqueId(),
        dest.getMessageLifecycleEvent().getMessageUniqueId());
    assertEquals(orig.getMessageLifecycleEvent().getMleMarkers().size(),
        dest.getMessageLifecycleEvent().getMleMarkers().size());
  }

  @Test
  public void testCreate() {
    AdaptrisMessage msg = getMessageFactory().newMessage();
    assertEquals(0, msg.getPayload().length);
  }

  @Test
  public void testIdGenerator() {
    AdaptrisMessageFactory fac = getMessageFactory();
    assertNull(fac.getUniqueIdGenerator());
    assertEquals(GuidGenerator.class, fac.uniqueIdGenerator().getClass());
    fac.setUniqueIdGenerator(new PseudoRandomIdGenerator("testIdGenerator", false));
    assertNotNull(fac.getUniqueIdGenerator());
    assertEquals(PseudoRandomIdGenerator.class, fac.getUniqueIdGenerator().getClass());
    assertEquals(PseudoRandomIdGenerator.class, fac.uniqueIdGenerator().getClass());
    AdaptrisMessage msg = fac.newMessage();
    assertTrue(msg.getUniqueId().startsWith("testIdGenerator"));
  }
}
