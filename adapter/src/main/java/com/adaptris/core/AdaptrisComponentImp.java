package com.adaptris.core;

import java.util.ArrayList;
import java.util.List;

public abstract class AdaptrisComponentImp implements AdaptrisComponent {

  private transient AdaptrisComponent parentComponent;
  
  private transient List<AdaptrisComponent> childComponents;
  
  public AdaptrisComponentImp() {
    this.setChildComponents(new ArrayList<>());
  }
  
  public AdaptrisComponent getParentComponent() {
   return parentComponent;
  }

  public List<AdaptrisComponent> getChildComponents() {
    return childComponents;
  }
  
  public void setParentComponent(AdaptrisComponent parentComponent) {
    this.parentComponent = parentComponent;
  }
  
  public void setChildComponents(List<AdaptrisComponent> childComponents) {
    this.childComponents = childComponents;
  }
  
}
