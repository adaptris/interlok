package com.adaptris.core.http.jetty.retry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileFilter;
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

public class FilesystemRetryStoreTest {

  // On Windows since TEST_BASE_URL will contain file://localhost/c:/
  // This gets magically URL encoded... so we can't assume that spaces will
  // make things fail, so for an invalid URL we must make
  // sure that we never have a drive letter.
  public static final String INVALID_URL = "file://localhost/./ spaces / not / valid / in / url";
  public static final String TEST_BASE_URL = "retry.baseUrl";

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
      AdaptrisMessage retry = store.buildForRetry(msg.getUniqueId(), store.getMetadata(msg.getUniqueId()),
          new FileBackedMessageFactory());
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
        AdaptrisMessage retry = store.buildForRetry("xxx", Collections.EMPTY_MAP);
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
      assertTrue(store.report().iterator().hasNext());
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
        AdaptrisMessage msg = new DefaultMessageFactory().newMessage("hello");
        store.report();
      } finally {
        LifecycleHelper.stopAndClose(store);
      }
    });
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

      RemoteBlob blob = FilesystemRetryStore.createForReport(storedMsgDir);
      assertNotNull(blob);
      assertEquals("hello".length(), blob.getSize());

      File randomDir = TempFileUtils.createTrackedDir(store);
      assertNull(FilesystemRetryStore.createForReport(randomDir));
      assertNull(FilesystemRetryStore.createForReport(null));
    } finally {
      LifecycleHelper.stopAndClose(store);
    }

  }
}
