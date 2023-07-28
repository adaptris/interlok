package com.adaptris.core.jdbc.retry;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.interlok.InterlokException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Service which obtains messages from the retry store that meet the appropriate
 * criteria and retries them. This service is intended to be used in conjunction
 * with <code>PollingTrigger</code>.
 * </p>
 * <p>
 * The 'appropriate criteria' are as follows:
 * <ul>
 * <li>the message has not been acknowledged</li>
 * <li>the total number of retries has not been exceeded</li>
 * <li>the retry interval has elapsed since the last retry.</li>
 * </ul>
 * </p>
 */

@XStreamAlias("retry-message-service")
@AdapterComponent
@ComponentProfile(summary = "retries a message from the retry store.",
    since = "4.9.0", tag = "retry")
@DisplayOrder(order = {"pruneExpired", "retryStore"})
public class RetryMessagesService extends RetryServiceImp {
  
  @InputFieldDefault(value = "false")
  private boolean pruneExpired;
  
  @NotNull
  @Valid
  private StandaloneProducer expiredMessagesProducer;

  public RetryMessagesService() {
    setExpiredMessagesProducer(new StandaloneProducer());
    setPruneExpired(false);
  }

  /** @see com.adaptris.core.ServiceImp#start() */
  @Override
  public void start() throws CoreException {
    super.start();
    getExpiredMessagesProducer().start();
  }

  /** @see com.adaptris.core.ServiceImp#stop() */
  @Override
  public void stop() {
    super.stop();
    getExpiredMessagesProducer().stop();
  }

  @Override
  protected void stopService() {
    super.close();
    getExpiredMessagesProducer().close();
    getConnection().stop();
  }

  /**
   *
   * @see RetryServiceImpTest#performService(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  protected void performService(AdaptrisMessage msg) throws ServiceException {
    try {
      pruneExpired();
      List<AdaptrisMessage> retryMsgs = getRetryStore().obtainMessagesToRetry();
      for (AdaptrisMessage retry : retryMsgs) {
        doRetry(retry);
      }
    }
    catch (InterlokException e) {
      throw new ServiceException(e);
    }
  }

  private void doRetry(AdaptrisMessage retry) throws InterlokException {
    String marshalledService = retry
        .getMetadataValue(Constants.MARSHALLED_SERVICE_KEY);

    Class c = null;

    try {
      c = Class.forName(retry
          .getMetadataValue(Constants.MARSHALLED_CLASS_NAME_KEY));
    }
    catch (ClassNotFoundException e) {
      throw new CoreException(e);
    }

    Service service = (Service) marshaller.unmarshal(marshalledService);

    service.init();
    service.start();

    try {
      log.debug("Retrying message [" + retry.getUniqueId()
          + "] with acknowledge id ["
          + retry.getMetadataValue(Constants.ACKNOWLEDGE_ID_KEY) + "]");
      if ("true".equalsIgnoreCase(retry
          .getMetadataValue(Constants.ASYNCHRONOUS_KEY))) {
        handleAsynchronous(retry, service);
      }
      else {
        handleSynchronous(retry, service);
      }
    }
    finally {
      service.stop();
      service.close();
    }
  }

  private void handleSynchronous(AdaptrisMessage retry, Service service)
      throws InterlokException {

    try {
      service.doService(retry);
      getRetryStore().acknowledge(
          retry.getMetadataValue(Constants.ACKNOWLEDGE_ID_KEY));
    }
    catch (InterlokException e) {
      getRetryStore().updateRetryCount(retry.getUniqueId());
    }
  }

  private void handleAsynchronous(AdaptrisMessage retry, Service service)
      throws InterlokException {
    try {
      service.doService(retry);
      getRetryStore().updateRetryCount(retry.getUniqueId());
    }
    catch (ServiceException e) {
      if ("true".equalsIgnoreCase(retry
          .getMetadataValue(Constants.ASYNC_AUTO_RETRY))) {
        getRetryStore().updateRetryCount(retry.getUniqueId());
      }
      else {
        throw e;
      }
    }
  }

  private void pruneExpired() {
    try {
      if (pruneExpired) {
        log.debug("Pruning Expired Messages");
        List<AdaptrisMessage> expiredMsgs = getRetryStore().obtainExpiredMessages();
        for (AdaptrisMessage expired : expiredMsgs) {
          log.debug("Producing Expired Message " + expired.getUniqueId());
          log.debug("EXPIRED MESSAGE" + expired.toString());
          getExpiredMessagesProducer().produce(expired.getPayload());
          getRetryStore().delete(expired.getUniqueId());
        }
      }
    }
    catch (Exception e) {
      log.warn("Ignoring exception while pruning expired messages ["
          + e.getMessage() + "]");
    }
  }

  /**
   * @return the expiredMessagesProducer
   */
  public StandaloneProducer getExpiredMessagesProducer() {
    return expiredMessagesProducer;
  }

  /**
   * @param sp the expiredMessagesProducer to set
   */
  public void setExpiredMessagesProducer(StandaloneProducer sp) {
    if (sp == null) {
      throw new IllegalArgumentException(
          "Null StandaloneProducer is not allowed");
    }
    expiredMessagesProducer = sp;
  }

  /**
   * @return the pruneExpired
   */
  public boolean getPruneExpired() {
    return pruneExpired;
  }

  /**
   * Specify whether to produce messages using the configured
   * <code>expiredMessagesProducer</code> and subsequently deleting them.
   *
   * @param b the pruneExpired to set
   * @see #setExpiredMessagesProducer(StandaloneProducer)
   */
  public void setPruneExpired(boolean b) {
    pruneExpired = b;
  }
}
