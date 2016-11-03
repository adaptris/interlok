package com.adaptris.tester.runtime.services.preprocessor;

import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.XmlUtils;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class XpathPreprocessorTest extends PreprocessorCase {

  private static final String XML = "<root><xml><id>blah</id></xml></root>";

  public XpathPreprocessorTest(String name) {
    super(name);
  }

  @Test
  public void testExecute() throws Exception {
    String result  = createPreprocessor().execute(XML);
    Document document = XmlHelper.createDocument(result, new DocumentBuilderFactoryBuilder());
    assertEquals("xml", document.getDocumentElement().getNodeName());
  }

  @Test
  public void testExecuteNoMatch() throws Exception {
    try {
      XpathPreprocessor preprocessor = new XpathPreprocessor();
      preprocessor.setXpath("//nomatch");
      preprocessor.execute(XML);
      fail();
    } catch (PreprocessorException e) {
      assertEquals("xpath [//nomatch] didn't return a match", e.getMessage());
    }
  }

  @Override
  protected Preprocessor createPreprocessor() {
    Map<String, String> namespace = new HashMap<>();
    namespace.put("xhtml", "http://www.w3.org/1999/xhtml");
    XpathPreprocessor preprocessor = new XpathPreprocessor();
    preprocessor.setNamespaceContext(new KeyValuePairSet(namespace));
    preprocessor.setXpath("//xml[./id= 'blah']");
    return preprocessor;
  }
}