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

package com.adaptris.core.services.metadata.xpath;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.Removal;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.text.xml.XPath;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * {@linkplain XpathQuery} implementation that retuns a single text item from an xpath derived from metadata.
 * 
 * @config metadata-xpath-query
 *
 * @deprecated Use ConfiguredXpathQuery with %message{metadata} syntax to extract XPath from metadata.
 *
 */
@Deprecated(since = "4.1.0")
@Removal(message = "Use ConfiguredXpathQuery with %message{metadata} syntax to extract XPath from metadata.")
@XStreamAlias("metadata-xpath-query")
@DisplayOrder(order = {"metadataKey", "xpathMetadataKey"})
public class MetadataXpathQuery extends MetadataXpathQueryImpl implements XpathQuery {

  public MetadataXpathQuery() {
  }

  public MetadataXpathQuery(String metadataKey, String xpathMetadataKey) {
    this();
    setMetadataKey(metadataKey);
    setXpathMetadataKey(xpathMetadataKey);
  }

  @Override
  public MetadataElement resolveXpath(Document doc, XPath xpath, String expr) throws Exception {
    String result;
    if (asXmlString()) {
      Node node = XpathQueryHelper.resolveSingleNode(doc, xpath, expr, allowEmptyResults());
      result = XmlHelper.nodeToString(node);
    } else {
      result = XpathQueryHelper.resolveSingleTextItem(doc, xpath, expr, allowEmptyResults());
    }
    return new MetadataElement(getMetadataKey(), result);

  }
}
