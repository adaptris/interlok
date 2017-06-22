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

import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.util.CastorizedList;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * <p>
 * Container for a collection of <code>Workflow</code>s.
 * </p>
 * 
 * @config workflow-list
 */
@XStreamAlias("workflow-list")
@AdapterComponent
@ComponentProfile(summary = "A Collection of Workflows", tag = "base")
public final class WorkflowList extends AbstractCollection<Workflow>
    implements ComponentLifecycle, ComponentLifecycleExtension, List<Workflow> {

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  @Valid
  @NotNull
  @AutoPopulated
  @XStreamImplicit
  private List<Workflow> workflows;
  private transient Map<String, Workflow> addressableWorkflows;
  @Valid
  private WorkflowLifecycleStrategy lifecycleStrategy;
  private transient WorkflowLifecycleStrategy defaultStrategy = new DefaultWorkflowLifecycleStrategy();

  @Deprecated
  private String uniqueId;
  
  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public WorkflowList() {
    workflows = new ArrayList<Workflow>();
    addressableWorkflows = new HashMap<String, Workflow>();
  }

  public WorkflowList(Collection<Workflow> workflows) {
    this();
    addAll(workflows);
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  @Override
  public void init() throws CoreException {
    addressableWorkflows.clear();
    for (Workflow w : workflows) {
      register(w);
    }
    log.trace("Workflows that can be manipulated are: " + addressableWorkflows.keySet());
    lifecycleStrategy().init(workflows);
  }

  /** @see com.adaptris.core.AdaptrisComponent#start() */
  @Override
  public void start() throws CoreException {
    lifecycleStrategy().start(workflows);
  }

  /** @see com.adaptris.core.AdaptrisComponent#stop() */
  @Override
  public void stop() {
    lifecycleStrategy().stop(workflows);

  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  public void close() {
    lifecycleStrategy().close(workflows);
  }


  @Override
  public void prepare() throws CoreException {
    for (Workflow w : workflows) {
      w.prepare();
    }
  }

  public WorkflowLifecycleStrategy getLifecycleStrategy() {
    return lifecycleStrategy;
  }

  /**
   * Specify the strategy to use when handling individual workflow lifecycle.
   * <p>
   * If not explicitly specified, then {@link DefaultWorkflowLifecycleStrategy} will be used to handle the workflow operations.
   * </p>
   *
   * @param wls the strategy to set
   * @see DefaultWorkflowLifecycleStrategy
   */
  public void setLifecycleStrategy(WorkflowLifecycleStrategy wls) {
    lifecycleStrategy = wls;
  }

  private WorkflowLifecycleStrategy lifecycleStrategy() {
    return getLifecycleStrategy() != null ? getLifecycleStrategy() : defaultStrategy;
  }

  // /** @see java.lang.Object#toString() */
  // @Override
  // public String toString() {
  // StringBuffer result = new StringBuffer();
  //
  // result.append("[");
  // result.append(this.getClass().getName());
  // result.append("] ");
  //
  // result.append("count [" + workflows.size() + "] ");
  //
  // for (int i = 0; i < workflows.size(); i++) {
  // result.append("\n\nworkflow [" + (i + 1) + "] ");
  // result.append(workflows.get(i));
  // }
  //
  // return result.toString();
  // }

  /**
   * <p>
   * Returns a <code>List</code> of <code>Workflow</code>s.
   * </p>
   *
   * @return a <code>List</code> of <code>Workflow</code>s
   */
  public List<Workflow> getWorkflows() {
    return new CastorizedList(this);
  }

  /**
   * <p>
   * Sets a <code>List</code> of <code>Workflow</code>s.
   * </p>
   *
   * @param l a <code>List</code> of <code>Workflow</code>s
   */
  public void setWorkflows(List<Workflow> l) {
    if (l == null) {
      throw new IllegalArgumentException("Workflow list is null, not allowed");
    }
    addressableWorkflows.clear();
    for (Workflow w : l) {
      register(w);
    }

    workflows = l;

  }

  /**
   * Add a workflow.
   *
   * @param element the workflow to add
   * @see #add(Workflow)
   */
  public void addWorkflow(Workflow element) {
    add(element);
  }

  @Override
  public int size() {
    return workflows.size();
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public Iterator<Workflow> iterator() {
    return workflows.listIterator();
  }

  @Override
  public boolean remove(Object o) {
    if (o != null && Workflow.class.isAssignableFrom(o.getClass())) {
      return removeWorkflow((Workflow) o);
    }
    return false;
  }

  /**
   * Remove a Workflow from this channel list.
   *
   * @param wf
   * @return true if the workflow was removed
   */
  public boolean removeWorkflow(Workflow wf) {
    if (wf == null) {
      return false;
    }
    boolean result = workflows.remove(wf);
    unregister(wf);
    return result;
  }

  @Override
  public boolean add(Workflow element) {
    if (element == null) {
      throw new IllegalArgumentException("Null workflow element");
    }
    return workflows.add(register(element));
  }

  private Workflow register(Workflow element) {
    String id = element.getUniqueId();
    if (!isBlank(id)) {
      if (addressableWorkflows.containsKey(id)) {
        throw new IllegalArgumentException("duplicate Workflow ID [" + id + "]");
      }
      else {
        addressableWorkflows.put(id, element);
      }
    }
    return element;
  }

  private Workflow unregister(Workflow element) {
    String id = element.getUniqueId();
    log.warn("Unregistering " + id);
    if (!isBlank(id)) {
      addressableWorkflows.remove(id);
    }
    return element;
  }

  @Override
  public void add(int index, Workflow element) {
    if (element == null) {
      throw new IllegalArgumentException("Null workflow element");
    }
    workflows.add(index, register(element));
  }

  @Override
  public boolean addAll(int index, Collection<? extends Workflow> c) {
    for (Workflow w : c) {
      register(w);
    }
    return workflows.addAll(index, c);
  }

  @Override
  public Workflow get(int index) {
    return workflows.get(index);
  }

  /**
   * Get a workflow by its uniqueid.
   *
   * @param uniqueId the unique id of the workflow
   * @return the workflow or null if no match is found.
   */
  public Workflow getWorkflow(String uniqueId) {
    if (isBlank(uniqueId)) {
      throw new IllegalArgumentException("illegal param [" + uniqueId + "]");
    }
    return addressableWorkflows.get(uniqueId);
  }

  @Override
  public int indexOf(Object o) {
    return workflows.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return workflows.lastIndexOf(o);
  }

  @Override
  public void clear() {
    workflows.clear();
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public ListIterator<Workflow> listIterator() {
    return workflows.listIterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ListIterator<Workflow> listIterator(int index) {
    return workflows.listIterator(index);
  }

  @Override
  public Workflow remove(int index) {
    return unregister(workflows.remove(index));
  }

  @Override
  public Workflow set(int index, Workflow element) {
    unregister(workflows.get(index));
    return workflows.set(index, register(element));
  }

  @Override
  public List<Workflow> subList(int fromIndex, int toIndex) {
    return workflows.subList(fromIndex, toIndex);
  }

  /**
   * Not required as this component doesn't need to extend {@link AdaptrisComponent}
   * 
   * @deprecated since 3.6.3
   */
  @Deprecated
  public String getUniqueId() {
    return uniqueId;
  }

  /**
   * Not required as this component doesn't need to extend {@link AdaptrisComponent}
   * 
   * @deprecated since 3.6.3
   */
  @Deprecated
  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

}
