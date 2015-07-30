package com.adaptris.core.services.metadata.xpath;

import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@linkplain XpathQuery} implementation that retuns a {@link Node} from an xpath derived from metadata.
 * 
 * @config metadata-xpath-node-query
 * @see com.adaptris.core.services.metadata.XpathObjectMetadataService
 * 
 */
@XStreamAlias("metadata-xpath-node-query")
public class MetadataXpathNodeQuery extends MetadataXpathQueryImpl implements XpathObjectQuery {

  public MetadataXpathNodeQuery() {
  }

  public MetadataXpathNodeQuery(String metadataKey, String xpathMetadataKey) {
    this();
    setMetadataKey(metadataKey);
    setXpathMetadataKey(xpathMetadataKey);
  }

  @Override
  public Node resolveXpath(Document doc, NamespaceContext ctx, String expr) throws CoreException {
    return XpathQueryHelper.resolveSingleNode(doc, ctx, expr, allowEmptyResults());
  }
}
