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

import com.adaptris.annotation.Removal;
import com.adaptris.core.util.DestinationHelper;
import lombok.NoArgsConstructor;

/**
 * Abstract Request Reply enabled producer that may be extended by concrete sub-classes.
 *
 */
@NoArgsConstructor
public abstract class RequestReplyProducerImp extends RequestReplyProducerBase {

  @Override
  public final void produce(AdaptrisMessage msg) throws ProduceException {
    doProduce(msg, endpoint(msg));
  }

  @Deprecated
  @Removal(version = "4.0.0")
  @Override
  public final void produce(AdaptrisMessage msg, ProduceDestination dest) throws ProduceException {
    doProduce(msg, DestinationHelper.resolveProduceDestination(endpoint(msg), dest, msg));
  }

  @Override
  public final AdaptrisMessage request(AdaptrisMessage msg) throws ProduceException {
    return request(msg, endpoint(msg), defaultTimeout());
  }

  @Override
  public final AdaptrisMessage request(AdaptrisMessage msg, long timeout) throws ProduceException {
    return request(msg, endpoint(msg), timeout);
  }

  @Override
  @Deprecated
  @Removal(version = "4.0.0")
  public final AdaptrisMessage request(AdaptrisMessage msg, ProduceDestination destination)
      throws ProduceException {
    return request(msg, destination, defaultTimeout());
  }

  @Override
  @Deprecated
  @Removal(version = "4.0.0")
  public final AdaptrisMessage request(AdaptrisMessage msg, ProduceDestination destination,
      long timeout) throws ProduceException {
    return request(msg, DestinationHelper.resolveProduceDestination(endpoint(msg), destination, msg),
        timeout);
  }

  private AdaptrisMessage request(AdaptrisMessage msg, String endpoint, long timeout)
      throws ProduceException {
    AdaptrisMessage reply = doRequest(msg, endpoint, timeout);
    return mergeReply(reply, msg);
  }

  /**
   * Actually do the request.
   *
   */
  protected abstract AdaptrisMessage doRequest(AdaptrisMessage msg, String endpoint, long timeout)
      throws ProduceException;

  /**
   * Actually do the produce.
   *
   */
  protected abstract void doProduce(AdaptrisMessage msg, String endpoint) throws ProduceException;

}
