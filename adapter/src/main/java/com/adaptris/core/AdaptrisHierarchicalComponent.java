package com.adaptris.core;

import java.util.List;

public interface AdaptrisHierarchicalComponent {

  /**
   * Which component is out parent?
   * 
   * If we are a service, then our parent might be another service, service-collection or workflow.
   * 
   * @return AdaptrisComponent
   */
  public AdaptrisComponent getParentComponent();
  
  /**
   * What components belong to this one?
   * 
   * If we are a workflow, then this would contain the service-collection.
   * 
   * If we are a service-collection then this would contain the services.
   * 
   * @return
   */
  public List<AdaptrisComponent> getChildComponents();
  
  /**
   * If we are a service, then our parent might be another service, service-collection or workflow.
   * 
   * @return void
   */
  public void setParentComponent(AdaptrisComponent parentComponent);
  
  /**
   * What components belong to this one?
   * 
   * If we are a workflow, then this would contain the service-collection, consumer and producer.
   * 
   * If we are a service-collection then this would contain the services.
   * 
   * @return
   */
  public void setChildComponents(List<AdaptrisComponent> childComponents);
   
}
