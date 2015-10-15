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

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Null implementation of <code>ProduceExceptionHandler</code> which logs a message only.
 * </p>
 * 
 * @config null-produce-exception-handler
 */
@XStreamAlias("null-produce-exception-handler")
public class NullProduceExceptionHandler extends ProduceExceptionHandlerImp {

  /** @see com.adaptris.core.ProduceExceptionHandler
   *   #handle(com.adaptris.core.Workflow) */
  public void handle(Workflow workflow) {
    log.debug("NullProduceExceptionHandler configured");
  }
}
