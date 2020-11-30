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

package com.adaptris.core.services.aggregator;

import java.util.Collection;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.conditional.Condition;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
/**
 * Abstract implementation of {@link MessageAggregator}.
 *
 */
@NoArgsConstructor
public abstract class MessageAggregatorImpl implements MessageAggregator {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * Whether or not to overwrite original metadata with metadata from the split messages.
   *
   */
  @Getter
  @Setter
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean overwriteMetadata;

  /**
   * Allows you to filter the messages based on a condition - optional, positive filter match
   */
  @AdvancedConfig
  @Getter
  @Setter
  @Valid
  private Condition filterCondition;

  /**
   * Should an error occur with the filter, should we exclude these messages from the result?
   */
  @AdvancedConfig
  @Getter
  @Setter
  @InputFieldDefault(value = "false")
  private Boolean retainFilterExceptionsMessages;


  protected Collection<AdaptrisMessage> filter(Collection<AdaptrisMessage> messages) {
    log.trace("MessageAggregator number of messages prior to filtering: {}", messages.size());
    Collection<AdaptrisMessage> filteredResult = messages.stream()
        .filter(adaptrisMessage -> filter(adaptrisMessage)).collect(Collectors.toList());
    log.trace("MessageAggregator number of messages after filtering: {}", filteredResult.size());
    return filteredResult;
  }

  protected boolean filter(AdaptrisMessage message) {
    try {
      return filterCondition().evaluate(message);
    } catch (CoreException e) {
      log.error("Error encountered in filtering message: [{}]", message.getUniqueId(), e);
      return retainFilterExceptionsMessages();
    }
  }

  public <T extends MessageAggregatorImpl> T withOverwriteMetadata(Boolean b) {
    setOverwriteMetadata(b);
    return (T) this;
  }

  protected boolean overwriteMetadata() {
    return BooleanUtils.toBooleanDefaultIfNull(getOverwriteMetadata(), false);
  }

  protected void overwriteMetadata(AdaptrisMessage src, AdaptrisMessage target) {
    if (overwriteMetadata()) {
      target.setMetadata(src.getMetadata());
    }
  }

  private Condition filterCondition() {
    return ObjectUtils.defaultIfNull(getFilterCondition(), (m) -> true);
  }

  private boolean retainFilterExceptionsMessages() {
    return BooleanUtils.toBooleanDefaultIfNull(getRetainFilterExceptionsMessages(), false);
  }
}
