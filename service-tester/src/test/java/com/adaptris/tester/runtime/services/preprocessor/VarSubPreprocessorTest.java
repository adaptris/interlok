package com.adaptris.tester.runtime.services.preprocessor;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class VarSubPreprocessorTest extends PreprocessorCase {

  public VarSubPreprocessorTest(String name) {
    super(name);
  }

  @Test
  public void testExecute() throws Exception {
    String result = createPreprocessor().execute("hello ${foo}");
    assertEquals("hello bar", result);
  }

  @Test
  public void testExecuteNoFiles() throws Exception {
    try {
      new VarSubPreprocessor().execute("hello ${foo}");
      fail();
    } catch (PreprocessorException e){
      assertEquals(e.getMessage(), "At least one properties file must be set");
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    VarSubPreprocessor preprocessor = new VarSubPreprocessor();
    preprocessor.addPropertyFile("file:///home/users/test.properties");
    return preprocessor;
  }

  @Override
  protected Preprocessor createPreprocessor() {
    VarSubPreprocessor preprocessor = new VarSubPreprocessor();
    File propertiesFile = new File(this.getClass().getClassLoader().getResource("test.properties").getFile());
    preprocessor.addPropertyFile("file:///" + propertiesFile.getAbsolutePath());
    return preprocessor;
  }
}