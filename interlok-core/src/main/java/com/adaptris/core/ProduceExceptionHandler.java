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
import com.adaptris.validation.constraints.ConfigDeprecated;

/**
 * <p> 
 * Implementations are pluggable responses to <code>ProduceException</code>s 
 * in a <code>Workflow</code>.
 * </p>
 *
 * @deprecated since 4.2.0
 */
@Deprecated(since = "4.2.0")
@ConfigDeprecated(message = "If you need restarting capability wrap your producer into a standalone-producer and set restart services on failure.", removalVersion = "5.1.0", groups = Deprecated.class)
@Removal(message = "If you need restarting capability wrap your producer into a standalone-producer and set restart services on failure.", version = "5.1.0")
public interface ProduceExceptionHandler {

  /**
   * <p>
   * Handle the <code>ProduceException</code>.
   * </p>
   * @param workflow the <code>Workflow</code> in which the 
   * <code>ProduceException</code> occurred
   */
  void handle(Workflow workflow);
}
