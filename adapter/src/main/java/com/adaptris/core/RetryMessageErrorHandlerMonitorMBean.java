/*
 * Copyright 2018 Adaptris Ltd.
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

import java.util.Collection;

import com.adaptris.core.runtime.ChildRuntimeInfoComponentMBean;

public interface RetryMessageErrorHandlerMonitorMBean extends ChildRuntimeInfoComponentMBean {

  /**
   * Fail all the messages associated with the {@link RetryMessageErrorHandler} instance.
   * 
   * @param failFutureMessages if true, then fail all future messages coming into the {@link RetryMessageErrorHandler} instance
   *          until the next component restart.
   * 
   */
  void failAllMessages(boolean failFutureMessages);

  /**
   * Get a list of all messages waiting for retry.
   * 
   */
  Collection<String> waitingForRetry();

  /**
   * Fail in individual message.
   * 
   */
  void failMessage(String s);

}
