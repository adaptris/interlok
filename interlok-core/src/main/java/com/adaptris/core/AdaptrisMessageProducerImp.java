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

import javax.validation.Valid;

/**
 * <p>
 * Implementation of behaviour common to <code>AdaptrisMessageProducer</code>s.
 * </p>
 */
public abstract class AdaptrisMessageProducerImp
  extends AdaptrisMessageWorkerImp
  implements AdaptrisMessageProducer {

  @Valid
  private ProduceDestination destination;
  
  public AdaptrisMessageProducerImp() {    
  }
  // gets and sets

  /** 
   * @see com.adaptris.core.AdaptrisMessageProducer
   *   #setDestination(com.adaptris.core.ProduceDestination) 
   */
  @Override
  public void setDestination(ProduceDestination dest) { // may be null...
    destination = dest;
  }

  /** @see com.adaptris.core.AdaptrisMessageProducer#getDestination() */
  @Override
  public ProduceDestination getDestination() {
    return destination;
  }
}
