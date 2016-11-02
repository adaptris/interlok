package com.adaptris.tester.runtime.services.preprocessor;

import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

@XStreamAlias("remove-node-preprocessor")
public class RemoveNodePreprocessor extends XpathPreprocessor{

  @Override
  public String execute(String input) throws PreprocessorException {
    try {
      Node document = XmlHelper.createDocument(input, new DocumentBuilderFactoryBuilder());
      Node node =  selectSingleNode(document, getXpath());
      node.getParentNode().removeChild(node);
      return nodeToString(document);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      throw new PreprocessorException(e);
    }
  }
}
