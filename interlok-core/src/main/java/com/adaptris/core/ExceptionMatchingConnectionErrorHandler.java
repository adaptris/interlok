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
public class ExceptionMatchingConnectionErrorHandler extends ConnectionErrorHandlerImp {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  protected ConnectionErrorHandler delegate;
  protected ExceptionMatcher exceptionMatcher;


    @Override
    public void handleConnectionException() {
        delegate.handleConnectionException();
    }

    @Override
    public boolean canHandleException(Exception exception) {
        if (exceptionMatcher == null || !exceptionMatcher.matches(exception)) {
            return super.canHandleException(exception);
        } else return true;
    }

    @Override
    public void init() throws CoreException {
        delegate.init();
    }

    @Override
    public void start() throws CoreException {
        delegate.start();
    }

    @Override
    public void stop() {
        delegate.stop();
    }

    @Override
    public void close() {
        delegate.close();
    }
}
