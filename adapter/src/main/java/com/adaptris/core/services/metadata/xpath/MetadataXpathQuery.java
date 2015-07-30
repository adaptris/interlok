package com.adaptris.core.services.metadata.xpath;

import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Document;

import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@linkplain XpathQuery} implementation that retuns a single text item from an xpath derived from metadata.
 * 
 * @config metadata-xpath-query
 * 
 */
@XStreamAlias("metadata-xpath-query")
public class MetadataXpathQuery extends MetadataXpathQueryImpl implements XpathQuery {

  public MetadataXpathQuery() {
  }

  public MetadataXpathQuery(String metadataKey, String xpathMetadataKey) {
    this();
    setMetadataKey(metadataKey);
    setXpathMetadataKey(xpathMetadataKey);
  }

  @Override
  public MetadataElement resolveXpath(Document doc, NamespaceContext ctx, String expr) throws CoreException {
    return new MetadataElement(getMetadataKey(), XpathQueryHelper.resolveSingleTextItem(doc, ctx, expr,
        allowEmptyResults()));
  }

}
