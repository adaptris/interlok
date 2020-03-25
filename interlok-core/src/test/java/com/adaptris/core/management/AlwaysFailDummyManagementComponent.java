package com.adaptris.core.management;

import java.util.Properties;

public class AlwaysFailDummyManagementComponent implements ManagementComponent {

  @Override
  public void init(final Properties config) throws Exception {
    throw new Exception("Expected");
  }

  @Override
  public void start() throws Exception {
    throw new Exception("Expected");
  }

  @Override
  public void stop() throws Exception {
    throw new Exception("Expected");
  }

  @Override
  public void destroy() throws Exception {
    throw new Exception("Expected");
  }
  
}
