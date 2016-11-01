package com.adaptris.tester.runtime.services.sources;

import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.File;

import static org.junit.Assert.*;


public class FileSourceTest {
  @Test
  public void getSource() throws Exception {
    final String testFile = "service.xml";
    File parentDir = new File(this.getClass().getClassLoader().getResource(testFile).getFile());
    Source source = new FileSource("file:///" + parentDir.getAbsolutePath());
    Document document = XmlHelper.createDocument(source.getSource(), new DocumentBuilderFactoryBuilder());
    assertEquals("service-collection", document.getDocumentElement().getNodeName());
  }

}