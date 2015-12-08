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

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Lenient implementation of FailedMessageRetrier.
 * <p>
 * This implementation allows <code>Workflow</code>s which cannot be uniquely identified to be
 * handled. This implementation logs a warning if <code>Workflow</code>s are not uniquely
 * identified. Message are resubmitted to the first <code>Workflow</code> with a matching ID that is
 * found
 * </p>
 * <p>
 * In the adapter configuration file this class is aliased as <b>lenient-failed-message-retrier</b>
 * which is the preferred alternative to the fully qualified classname when building your
 * configuration.
 * </p>
 * 
 * @author lchan
 * @author $Author: lchan $
 * @deprecated since 3.0.0 - Shouldn't be the case that you have a workflow/connection/channel
 *             combination in v3 that is non unique because everything should have a unique id.
 */
@Deprecated
@XStreamAlias("lenient-failed-message-retrier")
@AdapterComponent
@ComponentProfile(summary = "An alternative configurable failed message retrier that shouldn't be used",
    tag = "error-handling,base")
public class LenientFailedMessageRetrier extends FailedMessageRetrierImp {

  private static transient boolean warningLogged;

  public LenientFailedMessageRetrier() {
    if (!warningLogged) {
      log.warn("LenientFailedMessageRetrier is deprecated; use {} instead", DefaultFailedMessageRetrier.class.getCanonicalName());
      warningLogged = true;
    }

  }
  @Override
  public void addWorkflow(Workflow workflow) {
    String key = workflow.obtainWorkflowId();

    if (getWorkflows().keySet().contains(key)) {
      log.warn("Duplicate match on workflow-id [" + key + "]");
    }
    else {
      getWorkflows().put(key, workflow);
    }
  }
}
