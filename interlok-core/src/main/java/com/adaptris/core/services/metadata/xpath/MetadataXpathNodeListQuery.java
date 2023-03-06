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
import org.w3c.dom.NodeList;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.CoreException;
import com.adaptris.util.text.xml.XPath;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@linkplain XpathQuery} implementation that retuns a {@link NodeList} from an xpath derived from metadata.
 * <p>
 * Note that depending on the XPath engine; it is possible that {@link #setAllowEmptyResults(Boolean)} may have no effect, as it may
 * return a zero length NodeList.
 * </p>
 * 
 * @config metadata-xpath-nodelist-query
 * @see com.adaptris.core.services.metadata.XpathObjectMetadataService
 */
@JacksonXmlRootElement(localName = "metadata-xpath-nodelist-query")
@XStreamAlias("metadata-xpath-nodelist-query")
@DisplayOrder(order = {"metadataKey", "xpathMetadataKey"})
public class MetadataXpathNodeListQuery extends MetadataXpathQueryImpl implements XpathObjectQuery {

  public MetadataXpathNodeListQuery() {
  }

  public MetadataXpathNodeListQuery(String metadataKey, String xpathMetadataKey) {
    this();
    setMetadataKey(metadataKey);
    setXpathMetadataKey(xpathMetadataKey);
  }

  @Override
  public NodeList resolveXpath(Document doc, XPath xpath, String expr) throws CoreException {
    return XpathQueryHelper.resolveNodeList(doc, xpath, expr, allowEmptyResults());
  }
}
