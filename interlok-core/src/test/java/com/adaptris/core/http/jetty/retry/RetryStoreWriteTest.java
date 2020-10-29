package com.adaptris.core.http.jetty.retry;

import static com.adaptris.core.http.jetty.retry.FilesystemRetryStoreTest.TEST_BASE_URL;
import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.FileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.interlok.junit.scaffolding.BaseCase;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;

public class RetryStoreWriteTest extends ExampleServiceCase {

  private File retryStoreDir;

  @Before
  public void setUp() throws Exception {
    retryStoreDir = FsHelper.toFile(BaseCase.getConfiguration(TEST_BASE_URL));
  }

  @Test
  public void testService() throws Exception {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("hello");
    RetryStoreWriteService service = new RetryStoreWriteService()
        .withRetryStore(new FilesystemRetryStore().withBaseUrl(getConfiguration(TEST_BASE_URL)));
    int base = retryStoreDir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY).length;
    execute(service, msg);
    assertEquals(1,
        retryStoreDir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY).length - base);
  }

  @Test(expected = ServiceException.class)
  public void testService_Exception() throws Exception {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("hello");
    RetryStoreWriteService service = new RetryStoreWriteService()
        .withRetryStore(
            new FilesystemRetryStore().withBaseUrl(getConfiguration(TEST_BASE_URL) + "/ invalid"));
    execute(service, msg);
  }

  @Override
  protected RetryStoreWriteService retrieveObjectForSampleConfig() {
    return new RetryStoreWriteService().withRetryStore(new FilesystemRetryStore().withBaseUrl("file:///path/to/store"));

  }
}
