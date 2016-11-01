package com.adaptris.tester.runtime.services.preprocessor;

import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class RemoveNodePreprocessorTest {

  private static final String XML = "<root><xml><id>blah</id></xml></root>";
  @Test
  public void execute() throws Exception {
    RemoveNodePreprocessor preprocessor = new RemoveNodePreprocessor();
    preprocessor.setXpath("/root/xml/id");
    String result  = preprocessor.execute(XML);
    Document document = XmlHelper.createDocument(result, new DocumentBuilderFactoryBuilder());
    assertEquals("root", document.getDocumentElement().getNodeName());
    assertEquals(0, document.getElementsByTagName("id").getLength());
  }

}