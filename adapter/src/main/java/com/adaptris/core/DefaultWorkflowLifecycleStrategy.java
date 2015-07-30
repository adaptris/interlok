package com.adaptris.core;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Default strategy for starting workflows.
 * <p>
 * This strategy is functionally equivalent to the way in which workflow operations were originally handled. If a workflow fails to
 * start then an exception is thrown back to the parent channel.
 * </p>
 * 
 * @config default-workflow-lifecycle-strategy
 * 
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("default-workflow-lifecycle-strategy")
public class DefaultWorkflowLifecycleStrategy implements WorkflowLifecycleStrategy {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  public DefaultWorkflowLifecycleStrategy() {

  }

  public void start(List<Workflow> workflowList) throws CoreException {
    for (Workflow w : workflowList) {
      LifecycleHelper.start(w);
    }
  }

  public void init(List<Workflow> workflowList) throws CoreException {
    for (Workflow w : workflowList) {
      LifecycleHelper.init(w);
    }
  }

  public void stop(List<Workflow> workflowList) {
    stopConsumers(workflowList);
    for (Workflow w : workflowList) {
      LifecycleHelper.stop(w);
    }
  }

  public void close(List<Workflow> workflowList) {
    closeConsumers(workflowList);
    for (Workflow w : workflowList) {
      LifecycleHelper.close(w);
    }
  }

  public void stopConsumers(List<Workflow> workflowList) {
    for (Workflow w : workflowList) {
      LifecycleHelper.stop(w.getConsumer());
    }
  }

  public void closeConsumers(List<Workflow> workflowList) {
    for (Workflow w : workflowList) {
      LifecycleHelper.close(w.getConsumer());
    }
  }
}
