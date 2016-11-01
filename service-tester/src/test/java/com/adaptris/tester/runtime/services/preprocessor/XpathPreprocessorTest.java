package com.adaptris.tester.runtime.services.preprocessor;

import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.XmlUtils;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;

import static org.junit.Assert.*;

public class XpathPreprocessorTest {

  private static final String XML = "<root><xml><id>blah</id></xml></root>";
  @Test
  public void execute() throws Exception {
    XpathPreprocessor preprocessor = new XpathPreprocessor();
    preprocessor.setXpath("//xml[./id= 'blah']");
    String result  = preprocessor.execute(XML);
    Document document = XmlHelper.createDocument(result, new DocumentBuilderFactoryBuilder());
    assertEquals("xml", document.getDocumentElement().getNodeName());
  }

  @Test
  public void executeNoMatch() throws Exception {
    try {
      XpathPreprocessor preprocessor = new XpathPreprocessor();
      preprocessor.setXpath("//nomatch");
      preprocessor.execute(XML);
      fail();
    } catch (PreprocessorException e) {
      assertEquals("xpath [//nomatch] didn't return a match", e.getMessage());
    }
  }

}