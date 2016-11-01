package com.adaptris.tester.runtime.services.preprocessor;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class VarSubPreprocessorTest {

  @Test
  public void execute() throws Exception {
    VarSubPreprocessor preprocessor = new VarSubPreprocessor();
    File propertiesFile = new File(this.getClass().getClassLoader().getResource("test.properties").getFile());
    preprocessor.addPropertyFile("file:///" + propertiesFile.getAbsolutePath());
    String result = preprocessor.execute("hello ${foo}");
    assertEquals("hello bar", result);
  }

}