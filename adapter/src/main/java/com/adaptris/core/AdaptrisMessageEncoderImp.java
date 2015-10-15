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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Partial implementation of behaviour commom to all {@link AdaptrisMessageEncoder} instances.
 * </p>
 */
public abstract class AdaptrisMessageEncoderImp implements AdaptrisMessageEncoder {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private transient AdaptrisMessageFactory messageFactoryToUse;

  public AdaptrisMessageEncoderImp() {
    registerMessageFactory(new DefaultMessageFactory());
  }

  /**
   * 
   * @see AdaptrisMessageTranslator#registerMessageFactory(AdaptrisMessageFactory)
   */
  @Override
  public void registerMessageFactory(AdaptrisMessageFactory f) {
    messageFactoryToUse = f;
  }

  /**
   * 
   * @see AdaptrisMessageTranslator#currentMessageFactory()
   */
  @Override
  public AdaptrisMessageFactory currentMessageFactory() {
    return messageFactoryToUse;
  }
}
