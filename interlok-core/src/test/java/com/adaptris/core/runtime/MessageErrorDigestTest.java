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

package com.adaptris.core.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;

public class MessageErrorDigestTest {

  private AdaptrisMessageFactory factory;

  @Before
  public void setUp() throws Exception {
    factory = DefaultMessageFactory.getDefaultInstance();
  }


  @Test
  public void testAddMessages() throws Exception {
    MessageErrorDigest digest = new MessageErrorDigest();
    digest.add(createMessageError("1", "workflow1", new Exception()));

    assertEquals(1, digest.size());
    assertEquals("1", digest.get(0).getUniqueId());
  }

  @Test
  public void testAddMessagesPastMaxAmount() throws Exception {
    MessageErrorDigest digest = new MessageErrorDigest();
    digest.setMaxMessages(3);
    digest.addAll(createListOfErrors(3, 1));

    assertEquals(3, digest.size());

    digest.add(createMessageError("4", "workflow4", new Exception()));
    digest.add(createMessageError("5", "workflow5", new Exception()));

    assertEquals(3, digest.size());
    assertNotNull(digest.get(0).getStackTrace());
  }

  @Test
  public void testAddMessagesFIFO() throws Exception {
    MessageErrorDigest digest = new MessageErrorDigest();
    digest.setMaxMessages(3);

    digest.addAll(createListOfErrors(5, 1));


    assertEquals(3, digest.size());

    assertEquals("3", digest.get(0).getUniqueId());
    assertEquals("4", digest.get(1).getUniqueId());
    assertEquals("5", digest.get(2).getUniqueId());
  }

  @Test
  public void testDigestSimpleClone() throws Exception {
    MessageErrorDigest digest = new MessageErrorDigest();

    digest.addAll(createListOfErrors(5, 1));

    MessageErrorDigest clonedDigest = new MessageErrorDigest(digest);

    assertEquals(5, clonedDigest.size());
    assertEquals(MessageErrorDigest.DEFAULT_MAX_MESSAGES, clonedDigest.getMaxMessages());

  }

  @Test
  public void testDigestCloneWithOffset() throws Exception {
    MessageErrorDigest digest = new MessageErrorDigest();

    digest.addAll(createListOfErrors(5, 1));

    MessageErrorDigest clonedDigest = new MessageErrorDigest(digest, 2);

    assertEquals(3, clonedDigest.size());

    assertEquals("3", clonedDigest.get(0).getUniqueId());
    assertEquals("4", clonedDigest.get(1).getUniqueId());
    assertEquals("5", clonedDigest.get(2).getUniqueId());
    assertEquals(MessageErrorDigest.DEFAULT_MAX_MESSAGES, clonedDigest.getMaxMessages());

  }

  @Test
  public void testDigestCloneWithOffsetAndLimit() throws Exception {
    MessageErrorDigest digest = new MessageErrorDigest();

    digest.addAll(createListOfErrors(5, 1));

    MessageErrorDigest clonedDigest = new MessageErrorDigest(digest, 2, 3);

    assertEquals(1, clonedDigest.size());
    assertEquals("3", clonedDigest.get(0).getUniqueId());
    assertEquals(MessageErrorDigest.DEFAULT_MAX_MESSAGES, clonedDigest.getMaxMessages());
  }

  @Test
  public void testDigestCloneWithOffsetGreaterThanOriginalsMessageCount() throws Exception {
    try {
      MessageErrorDigest digest = new MessageErrorDigest();

      digest.addAll(createListOfErrors(5, 1));

      MessageErrorDigest clonedDigest = new MessageErrorDigest(digest, 1000, 1001);
      fail("Should throw an error cos orginal digest does not have 1000 messages.");
    }
    catch (Exception ex) {
      // expected
    }
  }

  @Test
  public void testSetMaxMessages() throws Exception {
    MessageErrorDigest digest = new MessageErrorDigest();
    assertEquals(MessageErrorDigest.DEFAULT_MAX_MESSAGES, digest.getMaxMessages());
    digest.setMaxMessages(101);
    assertEquals(101, digest.getMaxMessages());
    digest.setMaxMessages(-1);
    assertEquals(MessageErrorDigest.DEFAULT_MAX_MESSAGES, digest.getMaxMessages());
  }

  @Test
  public void testGet() throws Exception {
    MessageErrorDigest digest = new MessageErrorDigest();
    MessageDigestErrorEntry error = createMessageError("1", "workflow1", new Exception());
    digest.add(error);
    digest.addAll(createListOfErrors(4, 2));
    assertEquals(error, digest.get(0));
  }

  @Test
  public void testRemove_ByIndex() throws Exception {
    MessageErrorDigest digest = new MessageErrorDigest();
    MessageDigestErrorEntry error = createMessageError("1", "workflow1", new Exception());
    digest.add(error);
    digest.addAll(createListOfErrors(4, 2));
    digest.remove(0);
    assertEquals(4, digest.size());
    assertFalse(digest.contains(error));
  }

  @Test
  public void testRemove() throws Exception {
    MessageErrorDigest digest = new MessageErrorDigest();
    MessageDigestErrorEntry error = createMessageError("1", "workflow1", new Exception());
    digest.add(error);
    digest.addAll(createListOfErrors(4, 2));
    digest.remove(error);
    assertEquals(4, digest.size());
    assertFalse(digest.contains(error));
  }

  @Test
  public void testAddAll_WithIndex() throws Exception {
    MessageErrorDigest digest = new MessageErrorDigest();
    try {
      digest.addAll(0, createListOfErrors(5, 1));

      fail();
    }
    catch (UnsupportedOperationException expected) {

    }
  }

  @Test
  public void testAdd_WithIndex() throws Exception {
    MessageErrorDigest digest = new MessageErrorDigest();

    try {
      digest.add(0, createMessageError("1", "workflow1", new Exception()));
      fail();
    }
    catch (UnsupportedOperationException expected) {

    }
  }

  @Test
  public void testSet_WithIndex() throws Exception {
    MessageErrorDigest digest = new MessageErrorDigest();

    try {
      digest.set(0, createMessageError("1", "workflow1", new Exception()));
      fail();
    }
    catch (UnsupportedOperationException expected) {

    }
  }

  @Test
  public void testIndexOf() throws Exception {
    MessageErrorDigest digest = new MessageErrorDigest();
    MessageDigestErrorEntry error = createMessageError("1", "workflow1", new Exception());
    digest.add(error);
    digest.addAll(createListOfErrors(4, 2));
    assertEquals(0, digest.indexOf(error));
  }

  @Test
  public void testLastIndexOf() throws Exception {
    MessageErrorDigest digest = new MessageErrorDigest();
    MessageDigestErrorEntry error = createMessageError("1", "workflow1", new Exception());
    digest.add(error);
    digest.addAll(createListOfErrors(3, 2));
    digest.add(error);
    assertEquals(4, digest.lastIndexOf(error));
  }

  @Test
  public void testIterators() throws Exception {
    MessageErrorDigest digest = new MessageErrorDigest();
    digest.addAll(createListOfErrors(5, 1));
    assertNotNull(digest.iterator());
    assertNotNull(digest.listIterator());
    assertNotNull(digest.listIterator(0));
    for (MessageDigestErrorEntry e : digest) {
      // do nothing, this is just to check that we can iterate.
    }
  }

  @Test
  public void testSubList() throws Exception {
    MessageErrorDigest digest = new MessageErrorDigest();
    digest.addAll(createListOfErrors(5, 1));
    List<MessageDigestErrorEntry> subList = digest.subList(0, 2);
    assertEquals(2, subList.size());
    // Clearing the sublist should remove the corresponding contents of the digest.
    subList.clear();
    assertEquals(3, digest.size());
    assertEquals("3", digest.get(0).getUniqueId());
  }

  private List<MessageDigestErrorEntry> createListOfErrors(int size, int start) {
    List<MessageDigestErrorEntry> errors = new ArrayList<MessageDigestErrorEntry>();
    for (int i= 0; i < size; i++) {
      errors.add(createMessageError("" + (i + start), "workflow" + (i + start), new Exception()));
    }
    return errors;
  }

  private MessageDigestErrorEntry createMessageError(String messageId, String workflowId, Exception error) {
    MessageDigestErrorEntry entry = new MessageDigestErrorEntry(messageId, workflowId);
    entry.setStackTrace(error);
    return entry;
  }
}
