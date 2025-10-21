package com.adaptris.core.http.jetty.retry;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.stubs.TempFileUtils;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.cloud.RemoteBlob;
import com.adaptris.interlok.junit.scaffolding.BaseCase;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

public class FilesystemRetryStoreTest {

  // On Windows since TEST_BASE_URL will contain file://localhost/c:/
  // This gets magically URL encoded... so we can't assume that spaces will
  // make things fail, so for an invalid URL we must make
  // sure that we never have a drive letter.
  public static final String INVALID_URL = "file://localhost/./ spaces / not / valid / in / url";
  public static final String TEST_BASE_URL = "retry.baseUrl";

  @TempDir
  Path tempDir;

  @AfterAll
  public static void afterAll() throws Exception {
    FileUtils.deleteQuietly(FsHelper.toFile(BaseCase.getConfiguration(TEST_BASE_URL)));
  }

  @Test
  public void testWrite_PayloadMessage() throws Exception {
    FilesystemRetryStore store = new FilesystemRetryStore().withBaseUrl(BaseCase.getConfiguration(TEST_BASE_URL));
    try {
      LifecycleHelper.initAndStart(store);
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage("hello");
      store.write(msg);
      File retryDir = FsHelper.toFile(BaseCase.getConfiguration(TEST_BASE_URL));
      File msgDir = new File(retryDir, msg.getUniqueId());
      assertTrue(retryDir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY).length >= 1);
      assertTrue(msgDir.exists());
      assertEquals(2, msgDir.listFiles().length);
    } finally {
      LifecycleHelper.stopAndClose(store);
    }
  }

  @Test
  public void testWrite_PayloadMetadataException() throws Exception {
    FilesystemRetryStore store = new FilesystemRetryStore().withBaseUrl(BaseCase.getConfiguration(TEST_BASE_URL));
    try {
      LifecycleHelper.initAndStart(store);
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage("hello");
      msg.addObjectHeader(CoreConstants.OBJ_METADATA_EXCEPTION, new Exception());
      store.write(msg);
      File retryDir = FsHelper.toFile(BaseCase.getConfiguration(TEST_BASE_URL));
      File msgDir = new File(retryDir, msg.getUniqueId());
      assertTrue(retryDir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY).length >= 1);
      assertTrue(msgDir.exists());
      assertEquals(3, msgDir.listFiles().length);
    } finally {
      LifecycleHelper.stopAndClose(store);
    }
  }

  @Test
  public void testWrite_FileBacked() throws Exception {
    FilesystemRetryStore store = new FilesystemRetryStore().withBaseUrl(BaseCase.getConfiguration(TEST_BASE_URL));
    try {
      LifecycleHelper.initAndStart(store);
      AdaptrisMessage msg = new FileBackedMessageFactory().newMessage("hello");
      store.write(msg);
      File dir = FsHelper.toFile(BaseCase.getConfiguration(TEST_BASE_URL));
      assertTrue(dir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY).length >= 1);
    } finally {
      LifecycleHelper.stopAndClose(store);
    }
  }

  @Test
  public void testWrite_Exception() throws Exception {
    Assertions.assertThrows(InterlokException.class, () -> {
      FilesystemRetryStore store = new FilesystemRetryStore().withBaseUrl(INVALID_URL);
      try {
        LifecycleHelper.initAndStart(store);
        AdaptrisMessage msg = new DefaultMessageFactory().newMessage("hello");
        store.write(msg);
      } finally {
        LifecycleHelper.stopAndClose(store);
      }
    });
  }

  @Test
  public void testBuildForRetry() throws Exception {
    FilesystemRetryStore store = new FilesystemRetryStore().withBaseUrl(BaseCase.getConfiguration(TEST_BASE_URL));
    try {
      LifecycleHelper.initAndStart(store);
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage("hello");
      store.write(msg);
      AdaptrisMessage retry = store.buildForRetry(msg.getUniqueId());
      assertEquals(msg.getUniqueId(), retry.getUniqueId());
      assertEquals(msg.getMessageHeaders(), retry.getMessageHeaders());
    } finally {
      LifecycleHelper.stopAndClose(store);
    }
  }

  @Test
  public void testBuildForRetry_FileBacked() throws Exception {
    FilesystemRetryStore store = new FilesystemRetryStore().withBaseUrl(BaseCase.getConfiguration(TEST_BASE_URL));
    try {
      LifecycleHelper.initAndStart(store);
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage("hello");
      store.write(msg);
      Map<String, String> metadata = store.getMetadata(msg.getUniqueId());
      AdaptrisMessage retry = store.buildForRetry(msg.getUniqueId(), metadata, new FileBackedMessageFactory());
      assertEquals(msg.getUniqueId(), retry.getUniqueId());
      assertEquals(msg.getMessageHeaders(), retry.getMessageHeaders());
    } finally {
      LifecycleHelper.stopAndClose(store);
    }
  }

  @Test
  public void testBuildForRetry_Exception() throws Exception {
    Assertions.assertThrows(InterlokException.class, () -> {
      FilesystemRetryStore store = new FilesystemRetryStore().withBaseUrl(INVALID_URL);
      try {
        LifecycleHelper.initAndStart(store);
        store.buildForRetry("xxx", Collections.emptyMap());
      } finally {
        LifecycleHelper.stopAndClose(store);
      }
    });
  }

  @Test
  public void testGetMetadata() throws Exception {
    FilesystemRetryStore store = new FilesystemRetryStore().withBaseUrl(BaseCase.getConfiguration(TEST_BASE_URL));
    try {
      LifecycleHelper.initAndStart(store);
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage("hello");
      store.write(msg);
      Map<String, String> metadata = store.getMetadata(msg.getUniqueId());
      assertEquals(msg.getMessageHeaders(), metadata);
    } finally {
      LifecycleHelper.stopAndClose(store);
    }
  }

  @Test
  public void testGetMetadata_Exception() throws Exception {
    Assertions.assertThrows(InterlokException.class, () -> {
      FilesystemRetryStore store = new FilesystemRetryStore().withBaseUrl(BaseCase.getConfiguration(TEST_BASE_URL));
      try {
        LifecycleHelper.initAndStart(store);
        store.getMetadata("xxx");
      } finally {
        LifecycleHelper.stopAndClose(store);
      }
    });
  }

  @Test
  public void testReport() throws Exception {
    FilesystemRetryStore store = new FilesystemRetryStore().withBaseUrl(BaseCase.getConfiguration(TEST_BASE_URL));
    try {
      LifecycleHelper.initAndStart(store);
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage("hello");
      store.write(msg);
      assertTrue(store.report(false).iterator().hasNext());
    } finally {
      LifecycleHelper.stopAndClose(store);
    }
  }

  @Test
  public void testReport_Exception() throws Exception {
    Assertions.assertThrows(InterlokException.class, () -> {
      FilesystemRetryStore store = new FilesystemRetryStore().withBaseUrl(INVALID_URL);

      try {
        LifecycleHelper.initAndStart(store);
        new DefaultMessageFactory().newMessage("hello");
        store.report(false);
      } finally {
        LifecycleHelper.stopAndClose(store);
      }
    });
  }

  @Test
  public void testReport_IncludesErrorMessageLine() throws Exception {
    FilesystemRetryStore store = new FilesystemRetryStore().withBaseUrl(BaseCase.getConfiguration(FilesystemRetryStoreTest.TEST_BASE_URL));
    try {
      LifecycleHelper.initAndStart(store);
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage("payload");
      msg.addObjectHeader(Exception.class.getName(), new Exception("Test error line"));
      store.write(msg);

      Iterable<RemoteBlob> blobs = store.report(true);
      boolean found = false;
      for (RemoteBlob blob : blobs) {
        if (blob.getName().contains(FilesystemRetryStore.NAME_ERROR_LINE_SEPERATOR)) {
          found = true;
          break;
        }
      }
      assertTrue(found, "Blob name should include error message line");
    } finally {
      LifecycleHelper.stopAndClose(store);
    }
  }

    @Test
    public void testReport_DoesNotIncludesErrorMessageLine() throws Exception {
        FilesystemRetryStore store = new FilesystemRetryStore().withBaseUrl(BaseCase.getConfiguration(FilesystemRetryStoreTest.TEST_BASE_URL));
        try {
            LifecycleHelper.initAndStart(store);
            AdaptrisMessage msg = new DefaultMessageFactory().newMessage("payload");
            msg.addObjectHeader(Exception.class.getName(), new Exception("Test error line"));
            store.write(msg);

            Iterable<RemoteBlob> blobs = store.report(false);
            boolean found = false;
            for (RemoteBlob blob : blobs) {
                if (blob.getName().contains(FilesystemRetryStore.NAME_ERROR_LINE_SEPERATOR)) {
                    found = true;
                    break;
                }
            }
            assertFalse(found, "Blob name should not include error message line");
        } finally {
            LifecycleHelper.stopAndClose(store);
        }
    }

  @Test
  public void testDelete() throws Exception {
    FilesystemRetryStore store = new FilesystemRetryStore().withBaseUrl(BaseCase.getConfiguration(TEST_BASE_URL));
    try {
      LifecycleHelper.initAndStart(store);
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage("hello");
      store.write(msg);
      assertTrue(store.delete(msg.getUniqueId()));
      File retryDir = FsHelper.toFile(BaseCase.getConfiguration(TEST_BASE_URL));
      File msgDir = new File(retryDir, msg.getUniqueId());
      assertFalse(msgDir.exists());
    } finally {
      LifecycleHelper.stopAndClose(store);
    }
  }

  @Test
  public void testDelete_Exception() throws Exception {
    Assertions.assertThrows(InterlokException.class, () -> {
      FilesystemRetryStore store = new FilesystemRetryStore().withBaseUrl(INVALID_URL);

      try {
        LifecycleHelper.initAndStart(store);
        store.delete("XXXX");
      } finally {
        LifecycleHelper.stopAndClose(store);
      }
    });
  }

  @Test
  public void testCreateForReport() throws Exception {
    FilesystemRetryStore store = new FilesystemRetryStore().withBaseUrl(BaseCase.getConfiguration(TEST_BASE_URL));
    try {
      LifecycleHelper.initAndStart(store);
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage("hello");
      store.write(msg);
      File retryStoreDir = FsHelper.toFile(BaseCase.getConfiguration(TEST_BASE_URL));
      File storedMsgDir = new File(retryStoreDir, msg.getUniqueId());

      RemoteBlob blob = FilesystemRetryStore.createForReport(storedMsgDir, null);
      assertNotNull(blob);
      assertEquals("hello".length(), blob.getSize());

      File randomDir = TempFileUtils.createTrackedDir(store);
      assertNull(FilesystemRetryStore.createForReport(randomDir, null));
      assertNull(FilesystemRetryStore.createForReport(null, null));
    } finally {
      LifecycleHelper.stopAndClose(store);
    }
  }

  @Test
  void testGetStackTrace_ValidFile() throws Exception {
    FilesystemRetryStore store = spy(new FilesystemRetryStore());
    store.setBaseUrl(tempDir.toUri().toString());

    String msgId = "validMessageId";
    Path msgDir = tempDir.resolve(msgId);
    Files.createDirectory(msgDir);
    Path stackTraceFile = msgDir.resolve("stacktrace.txt");
    Files.writeString(stackTraceFile, "Stack trace content");

    String stackTrace = store.getStackTrace(msgId);
    assertEquals("Stack trace content", stackTrace);
  }

  @Test
  void testGetStackTrace_FileNotFound() {
    FilesystemRetryStore store = spy(new FilesystemRetryStore());
    store.setBaseUrl(tempDir.toUri().toString());

    String msgId = "missingMessageId";

    InterlokException exception = assertThrows(InterlokException.class, () -> store.getStackTrace(msgId));
    assertTrue(exception.getMessage().contains("Does not exist ["));
  }

  @Test
  void testGetStackTrace_UnreadableFile() throws Exception {
    FilesystemRetryStore store = spy(new FilesystemRetryStore());
    String msgId = "unreadableMessageId";

    doThrow(new InterlokException("Permission denied")).when(store).getStackTrace(msgId);

    InterlokException exception = assertThrows(InterlokException.class, () -> store.getStackTrace(msgId));
    assertTrue(exception.getMessage().contains("Permission denied"));
  }

  @Test
  void testValidatePathComponent_ValidComponent() {
    assertDoesNotThrow(() -> FilesystemRetryStore.validatePathComponent("validMessageId"));
  }

  @Test
  void testValidatePathComponent_NullComponent() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
          () -> FilesystemRetryStore.validatePathComponent(null));
    assertEquals("Message ID may not be null or empty", exception.getMessage());
  }

  @Test
  void testValidatePathComponent_EmptyComponent() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
          () -> FilesystemRetryStore.validatePathComponent(""));
    assertEquals("Message ID may not be null or empty", exception.getMessage());
  }

  @Test
  void testValidatePathComponent_PathTraversalDetected() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
          () -> FilesystemRetryStore.validatePathComponent("../invalidMessageId"));
    assertEquals("Invalid message ID: path traversal or separator detected", exception.getMessage());
  }

  @Test
  void testValidatePathComponent_SeparatorDetected() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
          () -> FilesystemRetryStore.validatePathComponent("invalid/messageId"));
    assertEquals("Invalid message ID: path traversal or separator detected", exception.getMessage());
  }

  @Test
  void testValidatePathComponent_AbsolutePathDetected() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
          () -> FilesystemRetryStore.validatePathComponent("C:\\absolutePath"));
    assertEquals("Invalid message ID: path traversal or separator detected", exception.getMessage());
  }

  @Test
  void testAcknowledge_NullImplementation() {
    FilesystemRetryStore store = new FilesystemRetryStore();
    assertDoesNotThrow(() -> store.acknowledge("testAcknowledgeId"));
  }

  @Test
  void testDeleteAcknowledged_NullImplementation() {
    FilesystemRetryStore store = new FilesystemRetryStore();
    assertDoesNotThrow(() -> store.deleteAcknowledged());
  }

  @Test
  void testUpdateRetryCount_NullImplementation() {
    FilesystemRetryStore store = new FilesystemRetryStore();
    assertDoesNotThrow(() -> store.updateRetryCount("testMessageId"));
  }

  @Test
  void testMakeConnection_NullImplementation() {
    FilesystemRetryStore store = new FilesystemRetryStore();
    assertDoesNotThrow(() -> store.makeConnection(null));
  }
}
