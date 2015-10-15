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

import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link XpathObjectQuery} implementation that returns a {@link NodeList}.
 * 
 * <p>
 * Note that depending on the XPath engine; it is possible that
 * {@link #setAllowEmptyResults(Boolean)} may have no effect, as it may return a zero length
 * NodeList.
 * </p>
 * 
 * @author lchan
 * @config configured-xpath-nodelist-query
 * @see com.adaptris.core.services.metadata.XpathObjectMetadataService
 */
@XStreamAlias("configured-xpath-nodelist-query")
public class ConfiguredXpathNodeListQuery extends ConfiguredXpathQueryImpl implements XpathObjectQuery {

  public ConfiguredXpathNodeListQuery() {
  }

  public ConfiguredXpathNodeListQuery(String metadataKey, String xpath) {
    this();
    setXpathQuery(xpath);
    setMetadataKey(metadataKey);
  }

  @Override
  public NodeList resolveXpath(Document doc, NamespaceContext ctx, String expression) throws Exception {
    return XpathQueryHelper.resolveNodeList(doc, ctx, expression, allowEmptyResults());
  }

}
