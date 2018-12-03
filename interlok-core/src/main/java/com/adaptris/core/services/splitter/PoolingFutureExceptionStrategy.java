package com.adaptris.core.services.splitter;

import java.util.List;
import java.util.concurrent.Future;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;

@FunctionalInterface
public interface PoolingFutureExceptionStrategy {
    void handle(ServiceExceptionHandler handler, List<Future<AdaptrisMessage>> results) throws CoreException;
}
