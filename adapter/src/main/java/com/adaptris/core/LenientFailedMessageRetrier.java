/*
 * $RCSfile: LenientFailedMessageRetrier.java,v $
 * $Revision: 1.4 $
 * $Date: 2009/04/21 10:25:38 $
 * $Author: lchan $
 */
package com.adaptris.core;

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
