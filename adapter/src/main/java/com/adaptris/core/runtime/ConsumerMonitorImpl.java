package com.adaptris.core.runtime;

import com.adaptris.core.AdaptrisMessageConsumer;

import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_CONSUMER_MONITOR_TYPE;

public abstract class ConsumerMonitorImpl<T extends AdaptrisMessageConsumer> extends ChildRuntimeInfoComponentImpl implements ConsumerMonitorMBean {

  private transient WorkflowManager parent;
  private transient T wrappedComponent;

  private ConsumerMonitorImpl(){
    super();
  }

  public ConsumerMonitorImpl(WorkflowManager owner, T consumer){
    this.parent = owner;
    this.wrappedComponent = consumer;
  }

  @Override
  protected String getType() {
    return JMX_CONSUMER_MONITOR_TYPE;
  }

  @Override
  protected String uniqueId() {
    return wrappedComponent.getUniqueId();
  }

  public T getWrappedComponent(){
    return this.wrappedComponent;
  }

  @Override
  public ParentRuntimeInfoComponent getParentRuntimeInfoComponent() {
    return parent;
  }
}
