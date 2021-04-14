package com.adaptris.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;

public class MessageHelperTest {
  @Test
  public void testCopyPayload() throws Exception {
    AdaptrisMessage reply = new DefaultMessageFactory().newMessage("hello world");
    AdaptrisMessage original = new DefaultMessageFactory().newMessage();
    MessageHelper.copyPayload(reply, original);
    assertEquals("hello world", original.getContent());
  }

  @Test(expected = IOException.class)
  public void testCopyPayload_Failure() throws Exception {
    AdaptrisMessage reply = new DefectiveMessageFactory(WhenToBreak.BOTH).newMessage();
    AdaptrisMessage original = new DefaultMessageFactory().newMessage();
    MessageHelper.copyPayload(reply, original);
  }

  @Test
  public void testCopyPayload_BothFileBacked() throws Exception {
    AdaptrisMessage reply = new FileBackedMessageFactory().newMessage("hello world");
    AdaptrisMessage original = new FileBackedMessageFactory().newMessage();
    MessageHelper.copyPayload(reply, original);
    assertEquals("hello world", original.getContent());
    assertTrue(original.getObjectHeaders().containsKey(reply.getUniqueId()));
  }

  @Test
  public void testCopyPayload_OneFileBacked() throws Exception {
    AdaptrisMessage reply = new FileBackedMessageFactory().newMessage("hello world");
    AdaptrisMessage original = new DefaultMessageFactory().newMessage();
    MessageHelper.copyPayload(reply, original);
    assertEquals("hello world", original.getContent());
    assertFalse(original.getObjectHeaders().containsKey(reply.getUniqueId()));
  }

  @Test
  public void testApplyCharset_Null() {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    MessageHelper.checkCharsetAndApply(msg, null, true);
  }

  @Test(expected = UnsupportedCharsetException.class)
  public void testApplyCharset_Passthru_Invalid() {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    MessageHelper.checkCharsetAndApply(msg, "UTF-9", true);
  }

  @Test
  public void testApplyCharset_Passthru_Valid() {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    MessageHelper.checkCharsetAndApply(msg, "ISO-8859-2", true);
    assertEquals("ISO-8859-2", msg.getContentEncoding());
  }


  @Test
  public void testApplyCharset_PassthruDisabled_Invalid() {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    MessageHelper.checkCharsetAndApply(msg, "UTF-9", false);
    assertEquals(UnsupportedCharsetException.class,
        msg.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION).getClass());
  }

  @Test
  public void testApplyCharset_PassthruDisabled_Valid() {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    MessageHelper.checkCharsetAndApply(msg, "ISO-8859-2", false);
    assertEquals("ISO-8859-2", msg.getContentEncoding());
  }

  @Test
  public void testStackTraceToString() throws Exception {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    assertFalse(MessageHelper.stackTraceAsString(msg).isPresent());
    msg.addObjectHeader(CoreConstants.OBJ_METADATA_EXCEPTION, new Exception());
    assertTrue(MessageHelper.stackTraceAsString(msg).isPresent());
  }
}
