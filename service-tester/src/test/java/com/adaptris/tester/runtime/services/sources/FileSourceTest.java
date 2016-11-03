package com.adaptris.tester.runtime.services.sources;

import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.tester.runtime.services.preprocessor.PreprocessorException;
import com.adaptris.tester.runtime.services.preprocessor.VarSubPreprocessor;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.File;

import static org.junit.Assert.*;


public class FileSourceTest extends SourceCase{

  public FileSourceTest(String name) {
    super(name);
  }

  @Test
  public void testGetSource() throws Exception {
    final String serviceFile = "service.xml";
    File testFile = new File(this.getClass().getClassLoader().getResource(serviceFile).getFile());
    Source source = new FileSource("file:///" + testFile.getAbsolutePath());
    Document document = XmlHelper.createDocument(source.getSource(), new DocumentBuilderFactoryBuilder());
    assertEquals("service-collection", document.getDocumentElement().getNodeName());
  }

  @Test
  public void testGetSourceNoFiles() throws Exception {
    try {
      final String testFile = "service.xml";
      File parentDir = new File(this.getClass().getClassLoader().getResource(testFile).getFile()).getParentFile();
      Source source = new FileSource("file:///" + parentDir.getAbsolutePath() + "/doesnotexist.xml");
      source.getSource();
      fail();
    } catch (SourceException e){
      assertTrue(e.getMessage().contains("Failed to read file"));
    }
  }

  @Override
  protected Source createSource() {
    return new FileSource("file:///home/users/service.xml");
  }
}