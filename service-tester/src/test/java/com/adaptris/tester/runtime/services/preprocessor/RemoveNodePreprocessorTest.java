package com.adaptris.tester.runtime.services.preprocessor;

import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class RemoveNodePreprocessorTest extends PreprocessorCase {

  private static final String XML = "<root><xml><id>blah</id></xml></root>";

  public RemoveNodePreprocessorTest(String name) {
    super(name);
  }


  @Test
  public void testExecute() throws Exception {
    String result  = createPreprocessor().execute(XML);
    Document document = XmlHelper.createDocument(result, new DocumentBuilderFactoryBuilder());
    assertEquals("root", document.getDocumentElement().getNodeName());
    assertEquals(0, document.getElementsByTagName("id").getLength());
  }


  @Override
  protected RemoveNodePreprocessor createPreprocessor(){
    RemoveNodePreprocessor preprocessor = new RemoveNodePreprocessor();
    preprocessor.setXpath("/root/xml/id");
    return preprocessor;
  }
}