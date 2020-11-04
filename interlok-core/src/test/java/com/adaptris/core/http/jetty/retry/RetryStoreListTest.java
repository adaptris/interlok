package com.adaptris.core.http.jetty.retry;

import static com.adaptris.core.http.jetty.retry.FilesystemRetryStoreTest.INVALID_URL;
import static com.adaptris.core.http.jetty.retry.FilesystemRetryStoreTest.TEST_BASE_URL;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.interlok.junit.scaffolding.BaseCase;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;

public class RetryStoreListTest extends ExampleServiceCase {

  private File retryStoreDir;

  @Before
  public void setUp() throws Exception {
    retryStoreDir = FsHelper.toFile(BaseCase.getConfiguration(TEST_BASE_URL));
  }

  @Test
  public void testService() throws Exception {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("hello");
    RetryStoreWriteService writer = new RetryStoreWriteService()
        .withRetryStore(new FilesystemRetryStore().withBaseUrl(getConfiguration(TEST_BASE_URL)));
    RetryStoreListService service = new RetryStoreListService()
        .withRetryStore(new FilesystemRetryStore().withBaseUrl(getConfiguration(TEST_BASE_URL)));
    execute(writer, msg);
    execute(service, msg);
    try (InputStream in = msg.getInputStream()) {
      List lines = IOUtils.readLines(in, StandardCharsets.UTF_8);
      assertTrue(lines.size() >= 1);
    }
  }

  @Test(expected = ServiceException.class)
  public void testService_Exception() throws Exception {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("hello");
    RetryStoreListService service = new RetryStoreListService()
        .withRetryStore(
            new FilesystemRetryStore().withBaseUrl(INVALID_URL));
    execute(service, msg);
  }

  @Override
  protected RetryStoreListService retrieveObjectForSampleConfig() {
    return new RetryStoreListService()
        .withRetryStore(new FilesystemRetryStore().withBaseUrl("file:///path/to/store"));

  }
}
