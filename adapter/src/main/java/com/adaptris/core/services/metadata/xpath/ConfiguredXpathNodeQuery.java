package com.adaptris.core.services.metadata.xpath;

import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link XpathObjectQuery} implementation that returns a {@link Node}
 * 
 * @author lchan
 * @config configured-xpath-node-query
 * @see com.adaptris.core.services.metadata.XpathObjectMetadataService
 */
@XStreamAlias("configured-xpath-node-query")
public class ConfiguredXpathNodeQuery extends ConfiguredXpathQueryImpl implements XpathObjectQuery {

  public ConfiguredXpathNodeQuery() {
  }

  public ConfiguredXpathNodeQuery(String metadataKey, String xpath) {
    this();
    setXpathQuery(xpath);
    setMetadataKey(metadataKey);
  }

  @Override
  public Node resolveXpath(Document doc, NamespaceContext ctx, String expression) throws Exception {
    return XpathQueryHelper.resolveSingleNode(doc, ctx, expression, allowEmptyResults());
  }

}
