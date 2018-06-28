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
import com.adaptris.core.CoreException;
import com.adaptris.util.text.xml.XPath;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@linkplain XpathQuery} implementation that retuns a {@link Node} from an xpath derived from metadata.
 * 
 * @config metadata-xpath-node-query
 * @see com.adaptris.core.services.metadata.XpathObjectMetadataService
 * 
 */
@XStreamAlias("metadata-xpath-node-query")
@DisplayOrder(order = {"metadataKey", "xpathMetadataKey"})
public class MetadataXpathNodeQuery extends MetadataXpathQueryImpl implements XpathObjectQuery {

  public MetadataXpathNodeQuery() {
  }

  public MetadataXpathNodeQuery(String metadataKey, String xpathMetadataKey) {
    this();
    setMetadataKey(metadataKey);
    setXpathMetadataKey(xpathMetadataKey);
  }

  @Override
  public Node resolveXpath(Document doc, XPath xpath, String expr) throws CoreException {
    return XpathQueryHelper.resolveSingleNode(doc, xpath, expr, allowEmptyResults());
  }
}
