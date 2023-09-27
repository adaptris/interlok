package com.adaptris.core.services.jdbc.retry;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.jdbc.retry.Constants;
import com.adaptris.interlok.InterlokException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Service which processes asynchronous acknowledgements for messages stored
 * using {@link JdbcStoreMessageForRetryService}.
 * </p>
 * <p>
 * The following metadata keys are required.
 * <ul>
 * <li>{@value com.adaptris.core.jdbc.retry.Constants#ACKNOWLEDGE_ID_KEY}
 * contains the ID that was previously used by
 * {@link JdbcStoreMessageForRetryService} as the correlation id.</li>
 * </ul>
 * </p>
 */
@XStreamAlias("jdbc-acknowledge-message-service")
@AdapterComponent
@ComponentProfile(summary = "processes asynchronous acknowledgements.", since = "5.0.0", tag = "jdbc, retry")
@DisplayOrder(order = { "pruneExpired", "retryStore" })
public class JdbcAcknowledgeService extends JdbcRetryServiceImp {

  /**
   *
   * @see JdbcRetryServiceImp#performService(com.adaptris.core.AdaptrisMessage)
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
      acknowledge(acknowledgeId);
    } catch (InterlokException e) {
      throw new ServiceException(e);
    }
  }

  @Override
  protected void stopService() {
    // TODO Auto-generated method stub
  }
}