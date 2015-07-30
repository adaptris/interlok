package com.adaptris.naming.adapter;


public class NamingEntry {
  
  private String name;
  
  private Object object;
  
  private NamingEntryType type;
  
  public NamingEntry() {
  }
  
  public NamingEntry(String name, Object object, NamingEntryType type) {
    this.setName(name);
    this.setObject(object);
    this.setType(type);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Object getObject() {
    return object;
  }

  public void setObject(Object object) {
    this.object = object;
  }

  public NamingEntryType getType() {
    return type;
  }

  public void setType(NamingEntryType type) {
    this.type = type;
  }

}
