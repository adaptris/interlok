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
import com.adaptris.core.MetadataElement;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.text.xml.XPath;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * {@linkplain XpathQuery} implementation that retuns a single text item from the configured xpath.
 * 
 * @config configured-xpath-query
 * 
 */
@XStreamAlias("configured-xpath-query")
@DisplayOrder(order = {"metadataKey", "xpathQuery"})
public class ConfiguredXpathQuery extends ConfiguredXpathQueryImpl implements XpathQuery {

  public ConfiguredXpathQuery() {
  }

  public ConfiguredXpathQuery(String metadataKey, String xpath) {
    this();
    setXpathQuery(xpath);
    setMetadataKey(metadataKey);
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
