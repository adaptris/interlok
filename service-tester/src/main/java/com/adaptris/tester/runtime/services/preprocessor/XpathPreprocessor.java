package com.adaptris.tester.runtime.services.preprocessor;

import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.text.xml.XPath;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.StringWriter;

@XStreamAlias("xpath-preprocessor")
public class XpathPreprocessor implements Preprocessor {

  private String xpath;

  @Override
  public String execute(String input) throws PreprocessorException {
    return nodeToString(selectSingleNode(input, getXpath()));
  }

  final Node selectSingleNode(Node document, String xpath) throws PreprocessorException {
    try {
      XPath xpathUtils = new XPath();
      Node node = xpathUtils.selectSingleNode(document, getXpath());
      if (node == null){
        throw new PreprocessorException(String.format("xpath [%s] didn't return a match", xpath));
      }
      return node;
    } catch (XPathExpressionException e) {
      throw new PreprocessorException(e);
    }
  }

  final Node selectSingleNode(String input, String xpath) throws PreprocessorException {
    try {
      return selectSingleNode(XmlHelper.createDocument(input, new DocumentBuilderFactoryBuilder()), xpath);
    } catch (ParserConfigurationException | IOException | SAXException e) {
      throw new PreprocessorException(e);
    }
  }

  final String nodeToString(Node node) throws PreprocessorException {
    StringWriter sw = new StringWriter();
    try {
      Transformer t = TransformerFactory.newInstance().newTransformer();
      t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      t.transform(new DOMSource(node), new StreamResult(sw));
    } catch (TransformerException e) {
      throw new PreprocessorException(e);
    }
    return sw.toString();
  }

  public String getXpath() {
    return xpath;
  }

  public void setXpath(String xpath) {
    this.xpath = xpath;
  }
}
