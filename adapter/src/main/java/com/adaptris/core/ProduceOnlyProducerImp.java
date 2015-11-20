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


/**
 * Convenience class that only supports produce rather than request.
 * 
 * <p>
 * All the request methods throw an UnsupportedOperationException
 * </p>
 * 
 * @author lchan
 * 
 */
public abstract class ProduceOnlyProducerImp extends AdaptrisMessageProducerImp {

  /**
   * @see com.adaptris.core.AdaptrisMessageProducerImp #produce(AdaptrisMessage, ProduceDestination)
   */
  @Override
  public void produce(AdaptrisMessage msg) throws ProduceException {
    produce(msg, getDestination());
  }

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
   * @see com.adaptris.core.AdaptrisMessageProducerImp
   *      #request(AdaptrisMessage,ProduceDestination)
   */
  @Override
  public final AdaptrisMessage request(AdaptrisMessage msg,
                                       ProduceDestination destination)
      throws ProduceException {
    throw new UnsupportedOperationException("Request Reply is not supported");
  }

  /**
   * UnsupportedOperationException is thrown
   * 
   * @see com.adaptris.core.AdaptrisMessageProducerImp #request(AdaptrisMessage,
   *      ProduceDestination, long)
   */
  @Override
  public final AdaptrisMessage request(AdaptrisMessage msg,
                                       ProduceDestination destination,
                                       long timeout) throws ProduceException {
    throw new UnsupportedOperationException("Request Reply is not supported");
  }

}
