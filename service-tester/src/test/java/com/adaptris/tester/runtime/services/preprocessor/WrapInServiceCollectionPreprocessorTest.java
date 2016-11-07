package com.adaptris.tester.runtime.services.preprocessor;

import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import org.junit.Test;
import org.w3c.dom.Document;

public class WrapInServiceCollectionPreprocessorTest extends PreprocessorCase {

  private static final String XML = "<root><xml><id>blah</id></xml></root>";

  public WrapInServiceCollectionPreprocessorTest(String name) {
    super(name);
  }

  @Test
  public void testExecute() throws Exception {
    String result  = createPreprocessor().execute(XML);
    Document document = XmlHelper.createDocument(result, new DocumentBuilderFactoryBuilder());
    assertEquals("service-collection", document.getDocumentElement().getNodeName());
  }

  @Override
  protected Preprocessor createPreprocessor() {
    return new WrapInServiceCollectionPreprocessor();
  }
}