package com.adaptris.core.services.routing;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.List;

import org.w3c.dom.Document;

import com.adaptris.core.ServiceException;
import com.adaptris.util.text.xml.XPath;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * SyntaxIdentifier implementation using XPATH.
 * 
 * @config routing-xpath-syntax-identifier
 * 
 * @author sellidge
 * @author $Author: lchan $
 */
@XStreamAlias("routing-xpath-syntax-identifier")
public class XpathSyntaxIdentifier extends XmlSyntaxIdentifierImpl {
  public XpathSyntaxIdentifier() {
    super();
  }

  public XpathSyntaxIdentifier(List<String> xpaths, String dest) {
    this();
    setDestination(dest);
    setPatterns(xpaths);
  }

  /**
   * @see SyntaxIdentifier#isThisSyntax(java.lang.String)
   */
  @Override
  public boolean isThisSyntax(String message) throws ServiceException {
    try {
      Document d = createDocument(message);
      if (d == null) {
        return false;
      }
      XPath xp = createXPath();
      for (String xpath : getPatterns()) {
        if (isEmpty(xp.selectSingleTextItem(d, xpath))) {
          return false;
        }
      }
    }
    catch (Exception e) {
      throw new ServiceException(e);
    }
    return true;
  }
}
