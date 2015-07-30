package com.adaptris.core.services.metadata.xpath;

import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Document;

import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@linkplain XpathQuery} implementation that retuns a single text item from the configured xpath.
 * 
 * @config configured-xpath-query
 * 
 */
@XStreamAlias("configured-xpath-query")
public class ConfiguredXpathQuery extends ConfiguredXpathQueryImpl implements XpathQuery {

  public ConfiguredXpathQuery() {
  }

  public ConfiguredXpathQuery(String metadataKey, String xpath) {
    this();
    setXpathQuery(xpath);
    setMetadataKey(metadataKey);
  }

  @Override
  public MetadataElement resolveXpath(Document doc, NamespaceContext ctx, String expr) throws CoreException {
    return new MetadataElement(getMetadataKey(), XpathQueryHelper.resolveSingleTextItem(doc, ctx, expr,
        allowEmptyResults()));
  }
}
