/*
 * Copyright 2015 Adaptris Ltd.
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

package com.adaptris.core;

import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.lms.FileBackedMessage;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.stream.StreamUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Abstract Request Reply enabled producer that may be extended by concrete sub-classes.
 *
 */
@NoArgsConstructor
public abstract class RequestReplyProducerBase extends AdaptrisMessageProducerImp {

  @AdvancedConfig
  @InputFieldDefault(value = "false")
  @Getter
  @Setter
  private Boolean ignoreReplyMetadata;


  /**
   * The default timeout for request messages when not supplied.
   *
   * @return the default timeout.
   */
  protected abstract long defaultTimeout();

  protected void copyReplyContents(AdaptrisMessage reply, AdaptrisMessage original)
      throws ProduceException {
    try {
      if (reply instanceof FileBackedMessage && original instanceof FileBackedMessage) {
        ((FileBackedMessage) original).initialiseFrom(((FileBackedMessage) reply).currentSource());
        // INTERLOK-2189 stop the reply from going out of scope.
        ((FileBackedMessage) original).addObjectHeader(reply.getUniqueId(), reply);
      } else {
        StreamUtil.copyAndClose(reply.getInputStream(), original.getOutputStream());
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapProduceException(e);
    }
  }


  protected AdaptrisMessage mergeReply(AdaptrisMessage reply, AdaptrisMessage msg)
      throws ProduceException {
    if (reply == msg) {
      return msg;
    }
    copyReplyContents(reply, msg);
    if (!shouldIgnoreReplyMetadata()) {
      reply.getMetadata().forEach((e) -> msg.addMetadata(e));
      msg.getObjectHeaders().putAll(reply.getObjectHeaders());
    }
    Optional.ofNullable(reply.getContentEncoding()).ifPresent((s) -> msg.setContentEncoding(s));
    return msg;
  }


  private boolean shouldIgnoreReplyMetadata() {
    return BooleanUtils.toBooleanDefaultIfNull(getIgnoreReplyMetadata(), false);
  }
}
