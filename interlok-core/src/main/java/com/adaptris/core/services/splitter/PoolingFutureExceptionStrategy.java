package com.adaptris.core.services.splitter;

import java.util.List;
import java.util.concurrent.Future;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.validation.constraints.ConfigDeprecated;

/**
 * @deprecated since 3.11.1 replaced by {@link ServiceErrorHandler} and
 *             {@link PooledSplitJoinService}.
 *
 */
@FunctionalInterface
@Deprecated
@ConfigDeprecated(removalVersion = "4.0.0", message = "since 3.11.1 replaced by 'ServiceErrorHandler' and 'PooledSplitJoinService'", groups = Deprecated.class)
public interface PoolingFutureExceptionStrategy {
  void handle(ServiceExceptionHandler handler, List<Future<AdaptrisMessage>> results) throws CoreException;
}
