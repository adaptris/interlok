package com.adaptris.core.management;

import java.io.Serializable;

import com.adaptris.core.ComponentState;

public class ManagementComponentInfo implements Serializable {

  private static final long serialVersionUID = 2020072701L;

  private String name;

  private String className;

  private ComponentState state;

  private transient ManagementComponent instance;

  public ManagementComponentInfo() {
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public ComponentState getState() {
    return state;
  }

  public void setState(ComponentState state) {
    this.state = state;
  }

  public ManagementComponent getInstance() {
    return instance;
  }

  public void setInstance(ManagementComponent instance) {
    this.instance = instance;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
