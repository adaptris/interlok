package com.adaptris.tester.runtime.services.preprocessor;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by Warmanm on 12/10/2016.
 */
public class XincludePreprocessorTest {

  @Ignore
  @Test
  public void execute() throws Exception {
    File serviceXml = new File(this.getClass().getClassLoader().getResource("service.xml").getFile());
    String XML = "<xi:include xmlns:xi=\"http://www.w3.org/2001/XInclude\" href=\"file:///" + serviceXml.getAbsolutePath()  +"\"/>";
    XincludePreprocessor preprocessor = new XincludePreprocessor();
    String s = preprocessor.execute(XML);
    System.out.println(s);
  }

}