package com.adaptris.core.runtime;

import com.adaptris.core.CoreException;
import com.adaptris.core.SerializableAdaptrisMessage;

/**
 * Interface specifying controls for a single workflow.
 */
public interface WorkflowManagerMBean extends AdapterComponentMBean, ChildComponentMBean, ParentRuntimeInfoComponentMBean,
    HierarchicalMBean {

  /**
   * Allows you to inject a serialised message into a workflow.
   * 
   * @param serialisedMessage an adaptris message
   * @return the contents of the message after processing from the workflow.
   * @throws CoreException wrapping any underlying Exception
   * @since 3.0.4
   */
  SerializableAdaptrisMessage injectMessageWithReply(SerializableAdaptrisMessage serialisedMessage) throws CoreException;

  /**
   * Allows you to inject a serialised message into a workflow.
   * 
   * @param serialisedMessage an adaptris message
   * @return true - if the message cold be handed to the workflow
   * @throws CoreException wrapping any underlying Exception
   */
  boolean injectMessage(SerializableAdaptrisMessage serialisedMessage) throws CoreException;

}
