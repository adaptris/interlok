package com.adaptris.core.services.splitter;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;

import java.util.List;
import java.util.concurrent.Future;

public interface PoolingFutureExceptionStrategy {
    void handle(ServiceExceptionHandler handler, List<Future<AdaptrisMessage>> results) throws CoreException;
}
