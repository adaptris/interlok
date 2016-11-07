package com.adaptris.tester.runtime.services.sources;

import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import org.junit.Test;
import org.w3c.dom.Document;

public class InlineSourceTest extends SourceCase {
  public InlineSourceTest(String name) {
    super(name);
  }

  @Test
  public void testGetSource() throws Exception {
    Source source = createSource();
    Document document = XmlHelper.createDocument(source.getSource(), new DocumentBuilderFactoryBuilder());
    assertEquals("add-metadata-service", document.getDocumentElement().getNodeName());
  }

  @Override
  protected Source createSource() {
    InlineSource source = new InlineSource();
    source.setXml(
        "\n" +
            "<add-metadata-service>\n" +
            "  <unique-id>Add2</unique-id>\n" +
            "  <metadata-element>\n" +
            "    <key>key2</key>\n" +
            "    <value>val2</value>\n" +
            "  </metadata-element>\n" +
            "</add-metadata-service>\n" +
            ""
    );
    return source;
  }
}