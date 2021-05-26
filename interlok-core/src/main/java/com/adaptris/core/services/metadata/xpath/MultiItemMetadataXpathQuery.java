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

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.annotation.Removal;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.text.xml.XPath;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.validation.constraints.NotNull;

/**
 * {@linkplain XpathQuery} implementation that retuns a multiple text items from an xpath derived from metadata.
 * 
 * @config multi-item-metadata-xpath-query
 *
 * @deprecated Use MultiItemConfiguredXpathQuery with %message{metadata} syntax to extract XPath from metadata.
 *
 */
@Deprecated(since = "4.1.0")
@Removal(message = "Use MultiItemConfiguredXpathQuery with %message{metadata} syntax to extract XPath from metadata.")
@XStreamAlias("multi-item-metadata-xpath-query")
@DisplayOrder(order = {"metadataKey", "xpathMetadataKey"})
public class MultiItemMetadataXpathQuery extends MetadataXpathQueryImpl implements XpathQuery {

  /**
   * The separator used to separate items.
   */
  @NotNull
  @AdvancedConfig
  @AutoPopulated
  @Getter
  @InputFieldHint(style = "BLANKABLE")
  private String separator;

  public MultiItemMetadataXpathQuery()
  {
    setSeparator("|");
  }

  public MultiItemMetadataXpathQuery(String metadataKey, String xpathMetadataKey)
  {
    this();
    setMetadataKey(metadataKey);
    setXpathMetadataKey(xpathMetadataKey);
  }

  public MultiItemMetadataXpathQuery(String metadataKey, String xpathMetadataKey, String separator)
  {
    this(metadataKey, xpathMetadataKey);
    setSeparator(separator);
  }

  @Override
  public MetadataElement resolveXpath(Document doc, XPath xpath, String expr) throws CoreException {
    String items = "";
    if (asXmlString()) {
      NodeList nodes = XpathQueryHelper.resolveNodeList(doc, xpath, expr, allowEmptyResults());
      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        items += XmlHelper.nodeToString(node) + "\n";
      }
    } else {
      items = XpathQueryHelper.resolveMultipleTextItems(doc, xpath, expr, allowEmptyResults(), getSeparator());
    }
    return new MetadataElement(getMetadataKey(), items);
  }

  /**
   * Set the separator used to separate items.
   *
   * @param s the separator, default '|'
   */
  public void setSeparator(String s) {
    separator = Args.notNull(s, "separator");
  }
}
