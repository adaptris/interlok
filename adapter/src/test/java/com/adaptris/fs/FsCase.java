package com.adaptris.fs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FsCase extends TestCase {

  protected transient Log logR = LogFactory.getLog(this.getClass());

  public FsCase(java.lang.String testName) {
    super(testName);
  }

  protected static Properties PROPERTIES;
  private static final String PROPERTIES_RESOURCE = "unit-tests.properties";

  static {
    PROPERTIES = new Properties();
    InputStream in = FsCase.class.getClassLoader().getResourceAsStream(PROPERTIES_RESOURCE);
    try {
      if (in == null) {
        throw new IOException("cannot locate resource [" + PROPERTIES_RESOURCE + "] on classpath");
      }
      PROPERTIES.load(in);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {

  }

}