package com.adaptris.core.http.jetty.retry;

import static com.adaptris.core.http.jetty.retry.FilesystemRetryStoreTest.INVALID_URL;
import static com.adaptris.core.http.jetty.retry.FilesystemRetryStoreTest.TEST_BASE_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.interlok.junit.scaffolding.BaseCase;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;

public class RetryStoreWriteTest extends ExampleServiceCase {

  @Test
  public void testService() throws Exception {
    File retryStoreDir = FsHelper.toFile(BaseCase.getConfiguration(TEST_BASE_URL));
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("hello");
    RetryStoreWriteService service = new RetryStoreWriteService()
        .withRetryStore(new FilesystemRetryStore().withBaseUrl(getConfiguration(TEST_BASE_URL)));
    File[] files = retryStoreDir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
    int base = Optional.ofNullable(files).orElse(new File[0]).length;
    execute(service, msg);
    assertEquals(1, retryStoreDir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY).length - base);
  }

  @Test
  public void testService_Exception() {
    Assertions.assertThrows(ServiceException.class, () -> {
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage("hello");
      RetryStoreWriteService service = new RetryStoreWriteService()
          .withRetryStore(new FilesystemRetryStore().withBaseUrl(INVALID_URL));
      execute(service, msg);
    });
  }

  @Test
  public void testService_AddsWorkflowIdFromLifecycleEvent() throws Exception {
    File retryStoreDir = FsHelper.toFile(BaseCase.getConfiguration(TEST_BASE_URL));
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("hello");
    msg.setUniqueId("retry-workflowid-test");
    msg.getMessageLifecycleEvent().setWorkflowId("xml-worker-workflow-3@xml-worker");
    RetryStoreWriteService service = new RetryStoreWriteService()
        .withRetryStore(new FilesystemRetryStore().withBaseUrl(getConfiguration(TEST_BASE_URL)));

    execute(service, msg);

    File metadata = new File(new File(retryStoreDir, msg.getUniqueId()), "metadata.properties");
    Properties p = new Properties();
    try (FileInputStream in = new FileInputStream(metadata)) {
      p.load(in);
    }
    assertTrue(p.containsKey("workflowId"));
    assertEquals("xml-worker-workflow-3@xml-worker", p.getProperty("workflowId"));
  }

  @Test
  public void testService_DoesNotOverrideExistingWorkflowIdMetadata() throws Exception {
    File retryStoreDir = FsHelper.toFile(BaseCase.getConfiguration(TEST_BASE_URL));
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("hello");
    msg.setUniqueId("retry-existing-workflowid-test");
    msg.addMetadata("workflowId", "already-set-workflow");
    msg.getMessageLifecycleEvent().setWorkflowId("lifecycle-workflow-should-not-win");
    msg.addMetadata(CoreConstants.WORKFLOW_ID_KEY, "core-workflow-should-not-win");

    RetryStoreWriteService service = new RetryStoreWriteService()
        .withRetryStore(new FilesystemRetryStore().withBaseUrl(getConfiguration(TEST_BASE_URL)));

    execute(service, msg);

    File metadata = new File(new File(retryStoreDir, msg.getUniqueId()), "metadata.properties");
    Properties p = new Properties();
    try (FileInputStream in = new FileInputStream(metadata)) {
      p.load(in);
    }
    assertEquals("already-set-workflow", p.getProperty("workflowId"));
  }

  @Override
  protected RetryStoreWriteService retrieveObjectForSampleConfig() {
    return new RetryStoreWriteService().withRetryStore(new FilesystemRetryStore().withBaseUrl("file:///path/to/store"));

  }
}
