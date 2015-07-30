package com.adaptris.util.text.xml;

import org.w3c.dom.Document;

import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.XmlUtils;

public abstract class XpathMergeImpl extends MergeImpl {

  private KeyValuePairSet namespaceContext;

  XpathMergeImpl() {
    super();
  }

  /**
   * @return the namespaceContext
   */
  public KeyValuePairSet getNamespaceContext() {
    return namespaceContext;
  }

  /**
   * Set the namespace context for resolving namespaces.
   * <ul>
   * <li>The key is the namespace prefix</li>
   * <li>The value is the namespace uri</li>
   * </ul>
   * 
   * @param kvps the namespace context
   * @see SimpleNamespaceContext#create(KeyValuePairSet)
   */
  public void setNamespaceContext(KeyValuePairSet kvps) {
    this.namespaceContext = kvps;
  }

  protected XPath createXPath() {
    return new XPath(SimpleNamespaceContext.create(getNamespaceContext()));
  }

  protected XmlUtils create(Document doc) throws Exception {
    XmlUtils xml = new XmlUtils(SimpleNamespaceContext.create(getNamespaceContext()));
    xml.setSource(doc);
    return xml;
  }
}
