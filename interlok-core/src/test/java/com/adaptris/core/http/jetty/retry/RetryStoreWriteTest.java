package com.adaptris.core.http.jetty.retry;

import static com.adaptris.core.http.jetty.retry.FilesystemRetryStoreTest.INVALID_URL;
import static com.adaptris.core.http.jetty.retry.FilesystemRetryStoreTest.TEST_BASE_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileFilter;
import java.util.Optional;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
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
  public void testService_Exception() throws Exception {
    Assertions.assertThrows(ServiceException.class, () -> {
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage("hello");
      RetryStoreWriteService service = new RetryStoreWriteService()
          .withRetryStore(new FilesystemRetryStore().withBaseUrl(INVALID_URL));
      execute(service, msg);
    });
  }

  @Override
  protected RetryStoreWriteService retrieveObjectForSampleConfig() {
    return new RetryStoreWriteService().withRetryStore(new FilesystemRetryStore().withBaseUrl("file:///path/to/store"));

  }
}
