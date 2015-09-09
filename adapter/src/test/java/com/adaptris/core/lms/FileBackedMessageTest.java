package com.adaptris.core.lms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageCase;
import com.adaptris.core.BaseCase;
import com.adaptris.core.MessageLifecycleEvent;
import com.adaptris.core.StandaloneProducer;

public class FileBackedMessageTest extends AdaptrisMessageCase {

  private FileBackedMessageFactory mf;

  @Before
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
      getMessageFactory().newMessage((byte[])null);
    } catch (NullPointerException e) {
      fail("Null payload must not throw NPE");
    }
  }
  
  @Test
  public void testSetNullPayloadString() {
    try {
      getMessageFactory().newMessage((String)null);
    } catch (NullPointerException e) {
      fail("Null payload must not throw NPE");
    }
  }
  
  @Test
  public void testMaxSize() {
    getMessageFactory().setMaxMemorySizeBytes(10);
    AdaptrisMessage orig = getMessageFactory().newMessage(PAYLOAD);
    try {
      byte[] bytes = orig.getPayload();
      fail("Managed to get " + new String(bytes));
    }
    catch (RuntimeException e) {
      // expected.
    }
  }
  
  @Test
  public void testMaxSizeString() {
    getMessageFactory().setMaxMemorySizeBytes(10);
    AdaptrisMessage orig = getMessageFactory().newMessage(PAYLOAD);
    try {
      String string = orig.getStringPayload();
      fail("Managed to get " + string);
    }
    catch (RuntimeException e) {
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
    }
    catch (IOException e) {
      ; // expected
    }
  }

  @Test
  public void testInitFromFile() throws Exception {
    FileBackedMessage orig = (FileBackedMessage) getMessageFactory().newMessage();
    File srcFile = new File(BaseCase.PROPERTIES.getProperty("msg.initFromFile"));
    orig.initialiseFrom(srcFile);
    
    assertEquals("file size ", srcFile.length(), orig.getSize());
    assertEquals("payload size ", srcFile.length(), orig.getPayload().length);
  }
  
  @Test
  public void testCloneFileBackedMessageWithoutContents() throws Exception {
    AdaptrisMessage msg1 = getMessageFactory().newMessage();
    msg1.addEvent(new StandaloneProducer(), true);
    
    AdaptrisMessage msg2 = (AdaptrisMessage) msg1.clone();

    assertTrue(msg2.getPayload() != msg1.getPayload());
    assertTrue(msg2.getMetadata() != msg1.getMetadata());
    assertTrue(msg2.getMessageLifecycleEvent() != msg1.getMessageLifecycleEvent());
    assertTrue(msg2.getStringPayload().equals(msg1.getStringPayload()));
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
    assertTrue("Message Size > 0", fileMsg.getSize() > 0);
    out.close();
    assertTrue("Message Size > 0 after 2nd close", fileMsg.getSize() > 0);
  }

}