package com.adaptris.core.management.config;

import com.adaptris.core.CoreException;

public class ConfigurationException extends CoreException {

  private static final long serialVersionUID = -6768566897581637451L;  
  /**
   * <p>
   * Creates a new instance with a description of the <code>Exception</code>.
   * </p>
   * @param description description of the <code>Exception</code>
   */
  public ConfigurationException(String description) {
    super(description);
  }

}
