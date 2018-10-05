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

package com.adaptris.core.interceptor;

import com.adaptris.core.PoolingWorkflow;
import com.adaptris.core.http.jetty.JettyNoBacklogInterceptor;
import com.adaptris.core.runtime.ChildRuntimeInfoComponentMBean;

/**
 * Management bean interface for messages in flight.
 * 
 */
public interface MessageInFlightMBean extends ChildRuntimeInfoComponentMBean {


  /**
   * Whether or not there are any messages in flight for the workflow.
   * 
   * @return true if there are messages in flight
   */
  boolean messagesInFlight();

  /**
   * Return the count of messages that are currently in flight.
   * <p>
   * Other than {@link PoolingWorkflow}, this will generally return 1 or 0. {@link PoolingWorkflow} will at most return
   * {@link PoolingWorkflow#getPoolSize()}. It is included for completeness.
   * </p>
   * 
   * @return the number of messages in flight.
   */
  int messagesInFlightCount();

  /**
   * Return the count of messages that are currently queued.
   * <p>
   * This only makes sense for {@link PoolingWorkflow} as other workflows are single threaded; so will most likely return 0. In the
   * context of PoolingWorkflow, this is the number of messages that have been submitted to the workflow, but have not yet been
   * processed.
   * </p>
   * 
   * @see JettyNoBacklogInterceptor
   * @return the number of messages that have been submitted for processing, but have not entered the workflow proper.
   */
  int messagesPendingCount();

}
