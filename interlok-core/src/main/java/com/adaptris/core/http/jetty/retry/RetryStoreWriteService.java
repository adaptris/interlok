package com.adaptris.core.http.jetty.retry;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.NoArgsConstructor;

/**
 * Write a message for retry with {@link RetryFromJetty}.
 *
 * @since 3.11.1
 * @config retry-store-write-message
 */
@XStreamAlias("retry-store-write-message")
@NoArgsConstructor
@ComponentProfile(summary = "Write a message to the retry store for future retries",
    since = "3.11.1", tag = "retry")
@DisplayOrder(order = {"retryStore"})
public class RetryStoreWriteService extends RetryStoreServiceImpl {

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      getRetryStore().write(msg);
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }

  }
}
