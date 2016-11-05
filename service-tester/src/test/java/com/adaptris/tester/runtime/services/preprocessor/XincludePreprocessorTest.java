package com.adaptris.tester.runtime.services.preprocessor;

import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.File;

public class XincludePreprocessorTest extends PreprocessorCase {

  public XincludePreprocessorTest(String name) {
    super(name);
  }

  @Test
  public void testExecute() throws Exception {
    File serviceXml = new File(this.getClass().getClassLoader().getResource("service.xml").getFile());
    String XML = "<xi:include xmlns:xi=\"http://www.w3.org/2001/XInclude\" href=\"file:///" + serviceXml.getAbsolutePath()  +"\"/>";
    String result = createPreprocessor().execute(XML);
    Document document = XmlHelper.createDocument(result, new DocumentBuilderFactoryBuilder());
    assertEquals("service-collection", document.getDocumentElement().getNodeName());
  }

  @Test
  public void testExecuteNoFile() throws Exception {
    try {
      final String testFile = "service.xml";
      File parentDir = new File(this.getClass().getClassLoader().getResource(testFile).getFile()).getParentFile();
      String XML = "<xi:include xmlns:xi=\"http://www.w3.org/2001/XInclude\" href=\"file:///" + parentDir.getAbsolutePath() + "/doesnotexist.xml\"/>";
      createPreprocessor().execute(XML);
      fail();
    } catch (PreprocessorException e){
      assertTrue(e.getMessage().contains("Failed to perform xinclude"));
    }
  }

  @Override
  protected Preprocessor createPreprocessor() {
    return new XincludePreprocessor();
  }
}