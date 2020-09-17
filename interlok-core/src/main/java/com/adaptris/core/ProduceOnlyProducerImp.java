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

import com.adaptris.annotation.Removal;
import com.adaptris.core.util.DestinationHelper;

/**
 * Convenience class that only supports produce rather than request.
 *
 * <p>
 * All the request methods throw an UnsupportedOperationException
 * </p>
 *
 */
public abstract class ProduceOnlyProducerImp extends AdaptrisMessageProducerImp {

  /**
   * UnsupportedOperationException is thrown
   *
   * @see com.adaptris.core.AdaptrisMessageProducerImp#request(AdaptrisMessage)
   */
  @Override
  public final AdaptrisMessage request(AdaptrisMessage msg)
      throws ProduceException {
    throw new UnsupportedOperationException("Request Reply is not supported");
  }

  /**
   * UnsupportedOperationException is thrown
   *
   * @see com.adaptris.core.AdaptrisMessageProducerImp#request(AdaptrisMessage, long)
   */
  @Override
  public final AdaptrisMessage request(AdaptrisMessage msg, long timeout)
      throws ProduceException {
    throw new UnsupportedOperationException("Request Reply is not supported");
  }

  /**
   * UnsupportedOperationException is thrown
   *
   * @see com.adaptris.core.AdaptrisMessageProducerImp #request(AdaptrisMessage,ProduceDestination)
   * @deprecated since 3.11.0 {@link ProduceDestination} is deprecated
   */
  @Override
  @Deprecated
  @Removal(version = "4.0")
  public final AdaptrisMessage request(AdaptrisMessage msg,
                                       ProduceDestination destination)
      throws ProduceException {
    throw new UnsupportedOperationException("Request Reply is not supported");
  }

  /**
   * UnsupportedOperationException is thrown
   *
   * @see com.adaptris.core.AdaptrisMessageProducerImp #request(AdaptrisMessage, ProduceDestination,
   *      long)
   * @deprecated since 3.11.0 {@link ProduceDestination} is deprecated
   */
  @Override
  @Deprecated
  @Removal(version = "4.0")
  public final AdaptrisMessage request(AdaptrisMessage msg,
                                       ProduceDestination destination,
                                       long timeout) throws ProduceException {
    throw new UnsupportedOperationException("Request Reply is not supported");
  }

  @Override
  public final void produce(AdaptrisMessage msg) throws ProduceException {
    doProduce(msg, endpoint(msg));
  }

  /**
   *
   * @deprecated since 3.11.0 {@link ProduceDestination} is deprecated
   */
  @Deprecated
  @Removal(version = "4.0")
  @Override
  public final void produce(AdaptrisMessage msg, ProduceDestination destination)
      throws ProduceException {
    doProduce(msg, DestinationHelper.resolveProduceDestination(endpoint(msg), destination, msg));
  }

  protected abstract void doProduce(AdaptrisMessage msg, String endpoint) throws ProduceException;
}

