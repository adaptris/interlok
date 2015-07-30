package com.adaptris.core.services.routing;

import org.w3c.dom.Document;

import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.adaptris.util.text.xml.XPath;

public abstract class XmlSyntaxIdentifierImpl extends SyntaxIdentifierImpl {

  private KeyValuePairSet namespaceContext;

  public XmlSyntaxIdentifierImpl() {
    namespaceContext = new KeyValuePairSet();
  }

  protected Document createDocument(String message) {
    Document result = null;
    try {
      result = XmlHelper.createDocument(message, SimpleNamespaceContext.create(namespaceContext));
    }
    catch (Exception e) {
      // Can't be an XML Document
      result = null;
    }
    return result;
  }

  protected XPath createXPath() {
    return new XPath(SimpleNamespaceContext.create(namespaceContext));
  }

  public KeyValuePairSet getNamespaceContext() {
    return namespaceContext;
  }

  public void setNamespaceContext(KeyValuePairSet kvps) {
    this.namespaceContext = kvps;
  }
}
