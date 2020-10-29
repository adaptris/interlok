package com.adaptris.core.http.jetty.retry;

import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.cloud.BlobListRenderer;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * List messages in the configured retry store.
 *
 * <p>
 * While not tightly coupled it is designed as a supporting service for use with
 * {@link RetryFromJetty}.
 * </p>
 *
 * @since 3.11.1
 * @config retry-store-list-service
 */
@XStreamAlias("retry-store-list-service")
@NoArgsConstructor
@ComponentProfile(summary = "List messages available to be retried from the retry store",
    since = "3.11.1", tag = "retry")
@DisplayOrder(order = {"reportRenderer", "retryStore"})
public class RetryStoreListService extends RetryStoreServiceImpl {

  @Getter
  @Setter
  private BlobListRenderer reportRenderer;

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      renderer().render(getRetryStore().report(), msg);
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }

  }

  private BlobListRenderer renderer() {
    return ObjectUtils.defaultIfNull(getReportRenderer(), new BlobListRenderer() {});
  }
}
