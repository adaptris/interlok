package com.adaptris.core.runtime;

import java.io.Serializable;

public class ClassDescriptorProperty implements Serializable {

  private static final long serialVersionUID = -8684225706153541943L;

  private String name;
  
  private String className;
  
  private int order;
  
  private boolean advanced;
  
  private String type;
  
  private String defaultValue;
  
  private boolean nullAllowed;
  
  private boolean autoPopulated;
  
  public ClassDescriptorProperty() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public boolean isAdvanced() {
    return advanced;
  }

  public void setAdvanced(boolean advanced) {
    this.advanced = advanced;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public boolean isNullAllowed() {
    return nullAllowed;
  }

  public void setNullAllowed(boolean nullAllowed) {
    this.nullAllowed = nullAllowed;
  }

  public boolean isAutoPopulated() {
    return autoPopulated;
  }

  public void setAutoPopulated(boolean autoPopulated) {
    this.autoPopulated = autoPopulated;
  }

  
}
