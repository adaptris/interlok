/*
 * Copyright 2018 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.adaptris.core.services.splitter;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.aggregator.AppendingMessageAggregator;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * Ignore all exceptions coming nested services, including Timeouts
 *
 * <p>
 * This is included for completeness, since there are some obvious gotchas this class as part of a
 * {@link FixedSplitJoinService}
 * <ul>
 * <li>If your aggregator is sensitive to payload structure (e.g you're aggregating as a JSON ARRAY,
 * but because of failures, the message to be aggregated is in fact XML) then you have to use a
 * {@link Condition} to filter the message somehow</li>
 * <li>If your aggregator isn't sensitive to payloads (e.g. an {@link AppendingMessageAggregator}
 * then you may still end up with a mix of JSON/XML</li>
 * <li>Exceptions may still be thrown by the aggregator, and they will be propagated to the service;
 * this only ignores exceptions that come from the service-list executed as part of the
 * {@code splitter}.</li>
 * </ul>
 * </p>
 *
 * @config ignoring-service-exception-handler
 *
 */
@XStreamAlias("ignoring-service-exception-handler")
@Slf4j
@ComponentProfile(
    summary = "Ignore all exceptions from fixed-split-join-service",
    since = "3.11.1", tag = "service,splitjoin")
public class IgnoreAllExceptions implements ServiceErrorHandler {

  @Override
  public void uncaughtException(Thread t, Throwable e) {
    log.error("uncaughtException from {}", t.getName(), e);
  }

  @Override
  public void throwExceptionAsRequired() throws ServiceException {
  }
}
