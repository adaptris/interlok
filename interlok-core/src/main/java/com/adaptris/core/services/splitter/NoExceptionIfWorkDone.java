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

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.aggregator.AppendingMessageAggregator;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * Ignores exception so long as some messages were considered successful based on a metadata key.
 * <p>
 * This strategy is useful if messages within a split-join are transient, and can be ignored
 * provided some of them work; it allows you to ignore exceptions processing individual mesages
 * provided one or more messages have set a specific metadata to the value {@code true | 1}.
 * </p>
 * <p>
 * There are some caveats to using this class as part of a {@link FixedSplitJoinService}
 * <ul>
 * <li>If your aggregator is sensitive to payload structure (e.g you're aggregating as a JSON ARRAY,
 * but because of failures, the message to be aggregated is in fact XML) then you have to use a
 * {@link Condition} to filter the message somehow. Presumably via the same metadata flag that
 * you've set here.</li>
 * <li>If your aggregator isn't sensitive to payloads (e.g. an {@link AppendingMessageAggregator}
 * then you may still end up with a mix of JSON/XML</li>
 * <li>Exceptions may still be thrown by the aggregator, and they will be propagated to the service;
 * this only ignores exceptions that come from the service-list executed as part of the
 * {@code splitter}.</li>
 * </ul>
 * </p>
 *
 * @config no-exception-if-work-done
 *
 */
@XStreamAlias("no-exception-if-work-done")
@Slf4j
@ComponentProfile(
    summary = "No exceptions if fixed-split-join-service recorded 'some work'",
    since = "3.11.1", tag = "service,splitjoin")
public class NoExceptionIfWorkDone extends ServiceExceptionHandler {

  public static final String DEFAULT_METADATA_KEY = "serviceResult";

  /**
   * Set the metadata key that captures if any service did work.
   * <p>
   * Defaults to {@value #DEFAULT_METADATA_KEY} if not specified.
   * </p>
   */
  @Getter
  @Setter
  @InputFieldDefault(value = DEFAULT_METADATA_KEY)
  private String metadataKey;

  private transient boolean workDone;
  private final transient Object locker = new Object();

  @Override
  public void throwExceptionAsRequired() throws ServiceException {
    if (!workDone) {
      super.throwExceptionAsRequired();
    }
    workDone = false;
  }

  @Synchronized("locker")
  @Override
  public void markSuccessful(AdaptrisMessage msg) {
    String metadataValue = msg.getMetadataValue(metadataKey());
    if (!workDone) {
      // If there's been no work done (yet) then set the flag.
      workDone = BooleanUtils.or(new boolean[] {
          BooleanUtils.toBoolean(metadataValue),
          BooleanUtils.toBoolean(NumberUtils.toInt(metadataValue))
      });
    }
  }

  private String metadataKey() {
    return StringUtils.defaultIfBlank(getMetadataKey(), DEFAULT_METADATA_KEY);
  }

  public NoExceptionIfWorkDone withMetadataKey(String k) {
    setMetadataKey(k);
    return this;
  }
}
