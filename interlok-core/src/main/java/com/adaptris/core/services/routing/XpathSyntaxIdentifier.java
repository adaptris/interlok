/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.services.routing;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import java.util.List;
import org.w3c.dom.Document;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.conditional.Condition;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.util.text.xml.XPath;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * SyntaxIdentifier implementation using XPATH.
 * <p>
 * If the {@code DocumentBuilderFactoryBuilder} has been explicitly set to be not namespace aware and the document does in fact
 * contain namespaces, then Saxon can cause merry havoc in the sense that {@code //NonNamespaceXpath} doesn't work if the document
 * has namespaces in it. We have included a shim so that behaviour can be toggled based on what you have configured.
 * </p>
 * 
 * <p>
 * Since <strong>3.10.0</strong> this class implements {@link Condition} which means that it can be used as part of the conditional
 * services; if used in such a manner, then configuration is contextual, get/setDestination will be ignored (but may still have to
 * be configured due to validation
 * </p>
 * 
 * @see XPath#newXPathInstance(DocumentBuilderFactoryBuilder, NamespaceContext)
 * @config routing-xpath-syntax-identifier
 * @author sellidge
 */
@JacksonXmlRootElement(localName = "routing-xpath-syntax-identifier")
@XStreamAlias("routing-xpath-syntax-identifier")
@DisplayOrder(order = {"destination", "patterns", "namespaceContext"})
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
