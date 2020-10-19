package com.adaptris.core.services.splitter;

import java.util.List;
import java.util.concurrent.Future;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.validation.constraints.ConfigDeprecated;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @deprecated since 3.11.1 replaced by {@link ServiceErrorHandler} and
 *             {@link PooledSplitJoinService}.
 * @config default-pooling-future-exception-strategy
 *
 */
@XStreamAlias("default-pooling-future-exception-strategy")
@Deprecated
@ConfigDeprecated(removalVersion = "4.0.0", message = "since 3.11.1 replaced by 'ServiceErrorHandler' and 'PooledSplitJoinService'", groups = Deprecated.class)
public class DefaultPoolingFutureExceptionStrategy implements PoolingFutureExceptionStrategy {

  public static final String EXCEPTION_MSG = "Timeout exceeded waiting for split services to complete.";

  @Override
  public void handle(ServiceExceptionHandler handler, List<Future<AdaptrisMessage>> results) throws CoreException {
    handler.throwFirstException();
    // Now check the futures to see if any were cancelled.
    for (Future<AdaptrisMessage> f : results) {
      if (f.isCancelled()) {
        throw new CoreException(EXCEPTION_MSG);
      }
    }
  }
}
