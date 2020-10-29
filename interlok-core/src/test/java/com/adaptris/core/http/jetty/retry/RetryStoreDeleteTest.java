package com.adaptris.core.http.jetty.retry;

import static com.adaptris.core.http.jetty.retry.FilesystemRetryStoreTest.TEST_BASE_URL;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;

public class RetryStoreDeleteTest extends ExampleServiceCase {

  @Test
  public void testService() throws Exception {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("hello");
    msg.addMessageHeader("deleteMe", "xxx");
    RetryStoreDeleteService service = new RetryStoreDeleteService()
        .withMessageId("%message{deleteMe}")
        .withRetryStore(new FilesystemRetryStore().withBaseUrl(getConfiguration(TEST_BASE_URL)));
    execute(service, msg);
  }

  @Test(expected = ServiceException.class)
  public void testService_Exception() throws Exception {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("hello");
    msg.addMessageHeader("deleteMe", "xxx");
    RetryStoreDeleteService service = new RetryStoreDeleteService()
        .withMessageId("%message{deleteMe}")
        .withRetryStore(
            new FilesystemRetryStore().withBaseUrl(getConfiguration(TEST_BASE_URL) + "/ invalid"));
    execute(service, msg);
  }

  @Override
  protected RetryStoreDeleteService retrieveObjectForSampleConfig() {
    return new RetryStoreDeleteService().withMessageId("%message{message-id-to-delete}")
        .withRetryStore(new FilesystemRetryStore().withBaseUrl("file:///path/to/store"));

  }
}
