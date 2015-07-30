package com.adaptris.core.services.metadata.xpath;

import javax.validation.constraints.NotNull;
import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Document;

import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@linkplain XpathQuery} implementation that retuns a multiple text items from an xpath derived from metadata.
 * 
 * @config multi-item-metadata-xpath-query
 * 
 */
@XStreamAlias("multi-item-metadata-xpath-query")
public class MultiItemMetadataXpathQuery extends MetadataXpathQueryImpl implements XpathQuery {

  @NotNull
  private String separator;

  public MultiItemMetadataXpathQuery() {
    setSeparator("|");
  }

  public MultiItemMetadataXpathQuery(String metadataKey, String xpathMetadataKey) {
    this();
    setMetadataKey(metadataKey);
    setXpathMetadataKey(xpathMetadataKey);
  }

  public MultiItemMetadataXpathQuery(String metadataKey, String xpathMetadataKey, String separator) {
    this(metadataKey, xpathMetadataKey);
    setSeparator(separator);
  }

  @Override
  public MetadataElement resolveXpath(Document doc, NamespaceContext ctx, String expr) throws CoreException {
    return new MetadataElement(getMetadataKey(), XpathQueryHelper.resolveMultipleTextItems(doc, ctx, expr, allowEmptyResults(),
        getSeparator()));
  }

  public String getSeparator() {
    return separator;
  }

  /**
   * Set the separator used to separate items.
   *
   * @param s the separator, default '|'
   */
  public void setSeparator(String s) {
    if (s == null) {
      throw new IllegalArgumentException("Configured Separator may not be null.");
    }
    separator = s;
  }

}
