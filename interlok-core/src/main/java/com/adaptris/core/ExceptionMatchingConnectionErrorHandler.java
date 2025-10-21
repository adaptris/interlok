/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core;

import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.adaptris.core.util.LoggingHelper.friendlyName;

/**
 * <p>
 * A <code>ConnectionErrorHandler</code> that delegates to an underlying <code>ConnectionErrorHandler</code> when
 * an Exception matches based on <code>ExceptionMatcher</code>s.
 * </p>
 */
@XStreamAlias("exception-matching-connection-error-handler")
public class ExceptionMatchingConnectionErrorHandler implements ConnectionErrorHandler {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  protected ConnectionErrorHandler delegate;
  protected ExceptionMatcher exceptionMatcher;

    public ConnectionErrorHandler getDelegate() {
        return delegate;
    }

    public void setDelegate(ConnectionErrorHandler delegate) {
        this.delegate = delegate;
    }

    public ExceptionMatcher getExceptionMatcher() {
        return exceptionMatcher;
    }

    public void setExceptionMatcher(ExceptionMatcher exceptionMatcher) {
        this.exceptionMatcher = exceptionMatcher;
    }

    @Override
    public void handleConnectionException() {
        if (delegate != null) delegate.handleConnectionException();
    }

    @Override
    public boolean canHandleException(Exception exception) {
        log.debug("Matching exception: {}", exception.toString());
        boolean matches = exceptionMatcher != null && exceptionMatcher.matches(exception) && exceptionMatcher.matches(exception);
        log.debug("Matches: {}", matches);
        return matches;
    }

    @Override
    public void init() throws CoreException {
        if (delegate != null) delegate.init();
    }

    @Override
    public void start() throws CoreException {
        if (delegate != null) delegate.start();
    }

    @Override
    public void stop() {
        if (delegate != null) delegate.stop();
    }

    @Override
    public void close() {
        if (delegate != null) delegate.close();
    }

    @Override
    public void registerConnection(AdaptrisConnection connection) {
        if (delegate != null) delegate.registerConnection(connection);
    }

    @Override
    public <T> T retrieveConnection(Class<T> type) {
        return delegate != null ? delegate.retrieveConnection(type) : null;
    }

    @Override
    public boolean allowedInConjunctionWith(ConnectionErrorHandler ceh) {
        return true;
    }
}
