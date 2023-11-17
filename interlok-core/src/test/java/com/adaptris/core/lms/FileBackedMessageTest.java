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

package com.adaptris.core.lms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageCase;
import com.adaptris.core.MessageLifecycleEvent;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.interlok.junit.scaffolding.BaseCase;

public class FileBackedMessageTest extends AdaptrisMessageCase {

  private FileBackedMessageFactory mf;

  @BeforeEach
  public void setup() {
    mf = new FileBackedMessageFactory();
  }

  /**
   * @see com.adaptris.core.AdaptrisMessageCase#getMessageFactory()
   */
  @Override
  protected FileBackedMessageFactory getMessageFactory() {
    return mf;
  }

  @Test
  public void testSetNullPayloadBytes() {
    try {
      getMessageFactory().newMessage((byte[]) null);
    } catch (NullPointerException e) {
      fail("Null payload must not throw NPE");
    }
  }

  @Test
  public void testSetNullPayloadString() {
    try {
      getMessageFactory().newMessage((String) null);
    } catch (NullPointerException e) {
      fail("Null payload must not throw NPE");
    }
  }

  @Test
  public void testMaxSize() {
    getMessageFactory().setMaxMemorySizeBytes(10L);
    AdaptrisMessage orig = getMessageFactory().newMessage(PAYLOAD);
    try {
      byte[] bytes = orig.getPayload();
      fail("Managed to get " + new String(bytes));
    } catch (RuntimeException e) {
      // expected.
    }
  }

  @Test
  public void testMaxSizeString() {
    getMessageFactory().setMaxMemorySizeBytes(10L);
    AdaptrisMessage orig = getMessageFactory().newMessage(PAYLOAD);
    try {
      String string = orig.getContent();
      fail("Managed to get " + string);
    } catch (RuntimeException e) {
      // expected.
    }
  }

  @Test
  public void testInitFromFileNotExists() throws Exception {
    FileBackedMessage orig = (FileBackedMessage) getMessageFactory().newMessage();
    File srcFile = new File("AnyOldFile");
    try {
      orig.initialiseFrom(srcFile);
      fail("Managed to initialise from a non-existent file");
    } catch (IOException e) {
      ; // expected
    }
  }

  @Test
  public void testInitFromFile() throws Exception {
    FileBackedMessage orig = (FileBackedMessage) getMessageFactory().newMessage();
    File srcFile = new File(BaseCase.PROPERTIES.getProperty("msg.initFromFile"));
    orig.initialiseFrom(srcFile);

    assertEquals(srcFile.length(), orig.getSize());
    assertEquals(srcFile.length(), orig.getPayload().length);
  }

  @Test
  public void testCurrentSource() throws Exception {
    FileBackedMessage orig = (FileBackedMessage) getMessageFactory().newMessage();
    assertNotNull(orig.currentSource());
    File srcFile = new File(BaseCase.PROPERTIES.getProperty("msg.initFromFile"));
    orig.initialiseFrom(srcFile);

    assertEquals(srcFile.length(), orig.getSize());
    assertEquals(srcFile.length(), orig.getPayload().length);
  }

  @Test
  public void testCloneFileBackedMessageWithoutContents() throws Exception {
    AdaptrisMessage msg1 = getMessageFactory().newMessage();
    msg1.addEvent(new StandaloneProducer(), true);

    AdaptrisMessage msg2 = (AdaptrisMessage) msg1.clone();

    assertTrue(msg2.getPayload() != msg1.getPayload());
    assertTrue(msg2.getMetadata() != msg1.getMetadata());
    assertTrue(msg2.getMessageLifecycleEvent() != msg1.getMessageLifecycleEvent());
    assertTrue(msg2.getContent().equals(msg1.getContent()));
    assertTrue(msg2.getMetadata().equals(msg1.getMetadata()));
    MessageLifecycleEvent event1 = msg1.getMessageLifecycleEvent();
    MessageLifecycleEvent event2 = msg2.getMessageLifecycleEvent();
    assertEquals(event1.getCreationTime(), event2.getCreationTime());
    assertEquals(event1.getMessageUniqueId(), event2.getMessageUniqueId());
    assertEquals(event1.getMleMarkers().size(), event2.getMleMarkers().size());
  }

  @Test
  public void testBug1478() throws Exception {
    FileBackedMessage fileMsg = (FileBackedMessage) getMessageFactory().newMessage();
    OutputStream out = fileMsg.getOutputStream();
    PrintStream printer = new PrintStream(out);
    printer.println("Quick zephyrs blow, vexing daft Jim");
    printer.close();
    assertTrue(fileMsg.getSize() > 0);
    out.close();
    assertTrue(fileMsg.getSize() > 0);
  }

  @Test
  public void testSetNullContent() throws Exception {
    FileBackedMessage fileMsg = (FileBackedMessage) getMessageFactory().newMessage();
    fileMsg.setContent(null, null);
    assertEquals(0, fileMsg.getSize());

  }

  @Test
  public void testSetNullPayload() throws Exception {
    FileBackedMessage fileMsg = (FileBackedMessage) getMessageFactory().newMessage();
    fileMsg.setPayload(null);
    assertEquals(0, fileMsg.getSize());
    fileMsg.setPayload(new byte[0]);
    assertEquals(0, fileMsg.getSize());
  }

  @Test
  public void testSetContent_BadEncoding() throws Exception {
    Assertions.assertThrows(RuntimeException.class, () -> {
      FileBackedMessage fileMsg = (FileBackedMessage) getMessageFactory().newMessage();
      fileMsg.setContent("xxx", "bad-encoding-hah");
    });

    // should throw RTE.
  }
}
