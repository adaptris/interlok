package com.adaptris.core.http.jetty.retry;

import javax.validation.constraints.NotNull;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Delete a message from the configured retry store.
 *
 * <p>
 * While not tightly coupled it is designed as a supporting service for use with
 * {@link RetryFromJetty}.
 * </p>
 *
 * @since 3.11.1
 * @config retry-store-delete-service
 */
@XStreamAlias("retry-store-delete-service")
@NoArgsConstructor
@ComponentProfile(summary = "Delete a message from the retry store",
    since = "3.11.1", tag = "retry")
@DisplayOrder(order = {"messageId", "retryStore"})
public class RetryStoreDeleteService extends RetryStoreServiceImpl {

  /**
   * The messageID to delete.
   * <p>
   * This supports metadata resolution via {@link AdaptrisMessage#resolve(String)} since it is not
   * expected that it should be deleting the current messages unique-id.
   * </p>
   */
  @Getter
  @Setter
  @InputFieldHint(expression = true)
  @NotNull
  private String messageId;

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      getRetryStore().delete(msg.resolve(getMessageId()));
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  public void prepare() throws CoreException {
    Args.notNull(messageId, "messageId");
    super.prepare();
  }

  public RetryStoreDeleteService withMessageId(String s) {
    setMessageId(s);
    return this;
  }

}
