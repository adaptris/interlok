package com.adaptris.core.services.jdbc.retry;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jdbc.retry.Constants;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.InterlokException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Service which obtains messages from the Database that meet the appropriate
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

@XStreamAlias("jdbc-retry-message-service")
@AdapterComponent
@ComponentProfile(summary = "retries a message from the retry store.", since = "5.0.0", tag = "jdbc, retry")
@DisplayOrder(order = {"pruneExpired", "retryStore"})
public class JdbcRetryMessagesService extends JdbcRetryServiceImp {
  
  @InputFieldDefault(value = "false")
  private boolean pruneExpired;
  
  @NotNull
  @Valid
  private StandaloneProducer expiredMessagesProducer;

  public JdbcRetryMessagesService() {
    setExpiredMessagesProducer(new StandaloneProducer());
    setPruneExpired(false);
  }
  
  /** @see com.adaptris.core.AdaptrisComponent#start() */
  @Override
  protected void startService() throws CoreException {
    LifecycleHelper.start(getExpiredMessagesProducer());
  }
  
  /** @see com.adaptris.core.AdaptrisComponent#stop() */
  @Override
  protected void stopService() {
    LifecycleHelper.stop(getExpiredMessagesProducer());
  }

  /**
   * The main service method, which retries an entry in the retry database table if it meets it's criteria.
   * 
   * @see com.adaptris.core.Service#doService(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    pruneAcknowledged();
    try {
      pruneExpired();
      List<AdaptrisMessage> retryMsgs = obtainMessagesToRetry();
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

    LifecycleHelper.init(service);
    LifecycleHelper.start(service);

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
      LifecycleHelper.stop(service);
      LifecycleHelper.close(service);
    }
  }

  private void handleSynchronous(AdaptrisMessage retry, Service service) throws InterlokException {
    try {
      service.doService(retry);
      acknowledge(retry.getMetadataValue(Constants.ACKNOWLEDGE_ID_KEY));
    }
    catch (InterlokException e) {
      updateRetryCount(retry.getUniqueId());
    }
  }

  private void handleAsynchronous(AdaptrisMessage retry, Service service) throws InterlokException {
    try {
      service.doService(retry);
      updateRetryCount(retry.getUniqueId());
    }
    catch (ServiceException e) {
      if ("true".equalsIgnoreCase(retry
          .getMetadataValue(Constants.ASYNC_AUTO_RETRY))) {
        updateRetryCount(retry.getUniqueId());
      }
      else {
        throw e;
      }
    }
  }

  private void pruneExpired() {
    try {
      if (isPruneExpired()) {
        log.debug("Pruning Expired Messages");
        List<AdaptrisMessage> expiredMsgs = obtainExpiredMessages();
        for (AdaptrisMessage expired : expiredMsgs) {
          log.debug("Producing Expired Message " + expired.getUniqueId());
          log.debug("EXPIRED MESSAGE" + expired.toString());
          getExpiredMessagesProducer().produce(expired);
          delete(expired.getUniqueId());
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
  
  private boolean isPruneExpired() {
    return BooleanUtils.toBooleanDefaultIfNull(getPruneExpired(), false);
  }
}
