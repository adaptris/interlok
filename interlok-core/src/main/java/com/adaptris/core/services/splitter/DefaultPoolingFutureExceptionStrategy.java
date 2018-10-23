package com.adaptris.core.services.splitter;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;

import java.util.List;
import java.util.concurrent.Future;

public class DefaultPoolingFutureExceptionStrategy implements PoolingFutureExceptionStrategy {

    @Override
    public void handle(ServiceExceptionHandler handler, List<Future<AdaptrisMessage>> results) throws CoreException {
        handler.throwFirstException();
        // Now check the futures to see if any were cancelled.
        for (Future<AdaptrisMessage> f : results) {
            if (f.isCancelled()) {
                throw new CoreException("Timeout exceeded waiting for job completion.");
            }
        }
    }
}
