/*
 * $Author: lchan $
 * $RCSfile: ProcessorConfig.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/10/27 07:10:26 $
 */
package com.adaptris.http.test;

import java.util.Properties;

/**
 * @author lchan
 * @author $Author: lchan $
 */
class ProcessorConfig {

  private String className = "";
  private String url = "";
  private String id = "";
  private Properties config;
    
  ProcessorConfig(String propertyKey) {
    if (propertyKey.lastIndexOf(".") > 0) {
      id = propertyKey.substring(0, propertyKey.lastIndexOf("."));
    } else {
      id = propertyKey;
    }
    config = new Properties();
  }

  void set(String propertyKey, String value) {
    if (propertyKey.indexOf(".class") > 0) {
      className = value;
    } else if (propertyKey.indexOf(".url") > 0) {
      url = value;
    } else {
      int dot = propertyKey.lastIndexOf(".");
      if (dot > 0) {
        propertyKey = propertyKey.substring(dot + 1);
      }      
      config.setProperty(propertyKey, value);
    }
  }

  String getClassName() {
    return className;
  }

  String getId() {
    return id;
  }
  
  String getUrl() {
    return url;
  }
  
  
  Properties getConfig() {
    return config;
  }
    
  public boolean equals(Object o) {
    if (!this.getClass().equals(o.getClass())) {
      return false;
    } else {
      return id.equals(((ProcessorConfig) o).getId());
    }
  }

  public int hashCode() {
    return id.hashCode();
  }
}
