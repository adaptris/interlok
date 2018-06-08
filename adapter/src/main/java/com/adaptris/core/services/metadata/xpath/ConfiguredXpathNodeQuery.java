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

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.util.text.xml.XPath;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link XpathObjectQuery} implementation that returns a {@link Node}
 * 
 * @author lchan
 * @config configured-xpath-node-query
 * @see com.adaptris.core.services.metadata.XpathObjectMetadataService
 */
@XStreamAlias("configured-xpath-node-query")
@DisplayOrder(order = {"metadataKey", "xpathQuery"})
public class ConfiguredXpathNodeQuery extends ConfiguredXpathQueryImpl implements XpathObjectQuery {

  public ConfiguredXpathNodeQuery() {
  }

  public ConfiguredXpathNodeQuery(String metadataKey, String xpath) {
    this();
    setXpathQuery(xpath);
    setMetadataKey(metadataKey);
  }

  @Override
  public Node resolveXpath(Document doc, XPath xpath, String expression) throws Exception {
    return XpathQueryHelper.resolveSingleNode(doc, xpath, expression, allowEmptyResults());
  }

}
