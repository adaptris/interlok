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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public abstract class AdaptrisMessageFactoryImplCase {

  protected static final String TEST_PAYLOAD = "test payload";

  protected abstract AdaptrisMessageFactory getMessageFactory();

  @Test
  public void testCreateWithEncoding() {
    DefaultMessageFactory encodedMF = (DefaultMessageFactory) getMessageFactory();
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

      public String createName() {
        return "event";
      }

      public String createQualifier() {
        return "qualifier";
      }
      public Boolean getIsTrackingEndpoint() {
        return Boolean.FALSE;
      }

      public Boolean getIsConfirmation() {
        return Boolean.FALSE;
      }

      public boolean isConfirmation() {
        return false;
      }

      public boolean isTrackingEndpoint() {
        return false;
      }

      public void setIsConfirmation(Boolean b) {
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
    assertEquals("Metadata Key Values", orig.getMetadataValue("key1"), dest
        .getMetadataValue("key1"));
    assertFalse(orig.headersContainsKey("key3"));
    assertFalse(dest.headersContainsKey("key3"));
    assertEquals("MessageId", orig.getUniqueId(), dest.getUniqueId());
    assertEquals("Mle MessageId", orig.getMessageLifecycleEvent()
        .getMessageUniqueId(), dest.getMessageLifecycleEvent()
        .getMessageUniqueId());
    assertEquals("MarkerSizes", orig.getMessageLifecycleEvent().getMleMarkers()
        .size(), dest.getMessageLifecycleEvent().getMleMarkers().size());
  }

  @Test
  public void testCreate() {
    AdaptrisMessage msg = getMessageFactory().newMessage();
    assertEquals(0, msg.getPayload().length);
  }
}
