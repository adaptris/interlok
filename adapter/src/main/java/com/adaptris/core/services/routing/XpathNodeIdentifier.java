package com.adaptris.core.services.routing;

import java.util.List;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;

import com.adaptris.core.ServiceException;
import com.adaptris.util.text.xml.XPath;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * SyntaxIdentifier implementation using XPATH.
 * <p>
 * This differs from the standard {@link XpathSyntaxIdentifier} in that, rather than attempting to resolve your XPath to a text
 * value, it simply uses the XPath to try and resolve to a Node (or NodeList). This is more useful if you have an XML document that
 * <b>doesn't contain any values for elements</b>
 * </p>
 * 
 * @config routing-xpath-node-syntax-identifier
 * 
 * @author $Author: lchan $
 */
@XStreamAlias("routing-xpath-node-syntax-identifier")
public class XpathNodeIdentifier extends XmlSyntaxIdentifierImpl {

  private Boolean resolveAsNodeset;

  public XpathNodeIdentifier() {
    super();
  }

  public XpathNodeIdentifier(List<String> xpaths, String dest) {
    this();
    setDestination(dest);
    setPatterns(xpaths);
  }

  /**
   * @see SyntaxIdentifier#isThisSyntax(java.lang.String)
   */
  @Override
  public boolean isThisSyntax(String message) throws ServiceException {
    boolean rc = true;

    try {
      Document d = createDocument(message);
      if (d == null) {
        return false;
      }
      XPath xp = createXPath();
      for (String xpath : getPatterns()) {
        Object result = null;
        if (resolveAsNodeset()) {
          result = xp.selectNodeList(d, xpath);
        }
        else {
          result = xp.selectSingleNode(d, xpath);
        }
        if (result == null) {
          rc = false;
          break;
        }
      }
    }
    catch (Exception e) {
      throw new ServiceException(e);
    }
    return rc;
  }

  public Boolean getResolveAsNodeset() {
    return resolveAsNodeset;
  }

  /**
   * Specify whether to attempt to resolve the XPath as a Nodeset or as a single node.
   *
   * @param b true to attempt resolve as a Nodeset, default is false.
   * @see XPathConstants#NODE
   * @see XPathConstants#NODESET
   */
  public void setResolveAsNodeset(Boolean b) {
    resolveAsNodeset = b;
  }

  protected boolean resolveAsNodeset() {
    return getResolveAsNodeset() != null ? getResolveAsNodeset().booleanValue() : false;
  }
}
