package com.adaptris.core.jdbc.retry;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.interlok.InterlokException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Service which processes asynchronous acknowledgements for messages stored
 * using {@link StoreMessageForRetryServiceTest}.
 * </p>
 * <p>
 * The following metadata keys are required.
 * <ul>
 * <li>{@value com.adaptris.core.services.retry.Constants#ACKNOWLEDGE_ID_KEY}
 * contains the ID that was previously used by
 * {@link StoreMessageForRetryServiceTest} as the correlation id.</li>
 * </ul>
 * </p>
 */
@XStreamAlias("acknowledge-message-service")
@AdapterComponent
@ComponentProfile(summary = "processes asynchronous acknowledgements.", since = "4.9.0", tag = "retry")
@DisplayOrder(order = { "pruneExpired", "retryStore" })
public class AcknowledgeService extends RetryServiceImp {

  /**
   *
   * @see RetryServiceImpTest#performService(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  protected void performService(AdaptrisMessage msg) throws ServiceException {

    String acknowledgeId = msg.getMetadataValue(Constants.ACKNOWLEDGE_ID_KEY);
    if (acknowledgeId == null) {
      log.debug(Constants.ACKNOWLEDGE_ID_KEY + " not available as metadata key or returned null");
      return;
    }
    try {
      log.debug("Acknowledging [" + acknowledgeId + "] as successfully sent");
      getRetryStore().acknowledge(acknowledgeId);
    } catch (InterlokException e) {
      throw new ServiceException(e);
    }
  }

  @Override
  protected void stopService() {
    // TODO Auto-generated method stub
  }
}