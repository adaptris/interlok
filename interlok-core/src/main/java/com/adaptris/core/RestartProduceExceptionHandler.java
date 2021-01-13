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

import com.adaptris.validation.constraints.ConfigDeprecated;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link ProduceExceptionHandler} which attempts to restart the {@link Workflow} that had the failure.
 *
 * @config restart-produce-exception-handler
 *
 * @deprecated since 3.10.2
 */
@Deprecated
@ConfigDeprecated(message = "If you need restarting capability use channel-restart-produce-exception-handler or wrap your producer into a standalone-producer and set restart services on failure.", removalVersion = "4.0.0", groups = Deprecated.class)
@XStreamAlias("restart-produce-exception-handler")
public class RestartProduceExceptionHandler extends ProduceExceptionHandlerImp {

  /**
   * @see com.adaptris.core.ProduceExceptionHandler#handle(com.adaptris.core.Workflow)
   */
  @Override
  public void handle(Workflow workflow) {
    restart(workflow);
  }
}
