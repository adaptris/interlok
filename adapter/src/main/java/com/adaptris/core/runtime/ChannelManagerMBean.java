package com.adaptris.core.runtime;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.adaptris.core.CoreException;
import com.adaptris.core.Workflow;

/**
 * Interface specifying controls for a single channel.
 */
public interface ChannelManagerMBean extends AdapterComponentMBean, ParentRuntimeInfoComponentMBean,
    ChildRuntimeInfoComponentMBean, HierarchicalMBean, ParentComponentMBean {


  /**
   * Add a {@link Workflow} to this channel.
   *
   * @param xmlString the string representation of the workflow.
   * @return the ObjectName reference to the newly created ChannelManagerMBean.
   * @throws CoreException wrapping any exception
   * @throws IllegalStateException if the state of the adapter is not "Closed"
   * @throws MalformedObjectNameException upon ObjectName errors.
   */
  ObjectName addWorkflow(String xmlString) throws CoreException, IllegalStateException, MalformedObjectNameException;

  /**
   * Remove a {@link Workflow} from this channel.
   *
   * <p>
   * This also removes the associated {@link WorkflowManager} and calls {@link #unregisterMBean()}.
   * </p>
   *
   * @param id the id of the channel to remove.
   * @throws CoreException wrapping any exception
   * @throws IllegalStateException if the state of the adapter is not "Closed"
   * @return true if the channel existed and was removed, false otherwise.
   * @throws MalformedObjectNameException upon ObjectName errors.
   */
  boolean removeWorkflow(String id) throws CoreException, IllegalStateException, MalformedObjectNameException;

}
