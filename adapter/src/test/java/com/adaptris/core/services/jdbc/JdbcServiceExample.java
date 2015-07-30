package com.adaptris.core.services.jdbc;

import com.adaptris.core.jdbc.JdbcServiceCase;

public abstract class JdbcServiceExample extends JdbcServiceCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "JdbcServiceExamples.baseDir";

  public JdbcServiceExample(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

}
