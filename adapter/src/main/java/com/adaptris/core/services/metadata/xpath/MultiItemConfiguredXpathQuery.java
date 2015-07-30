package com.adaptris.core.services.metadata.xpath;

import javax.validation.constraints.NotNull;
import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Document;

import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@linkplain XpathQuery} implementation that retuns a multiple text items from the configured xpath.
 * 
 * @config multi-item-configured-xpath-query
 * 
 */
@XStreamAlias("multi-item-configured-xpath-query")
public class MultiItemConfiguredXpathQuery extends ConfiguredXpathQueryImpl implements XpathQuery {

  @NotNull
  private String separator;

  public MultiItemConfiguredXpathQuery() {
    setSeparator("|");
  }

  public MultiItemConfiguredXpathQuery(String metadataKey, String xpath) {
    this();
    setXpathQuery(xpath);
    setMetadataKey(metadataKey);
  }

  public MultiItemConfiguredXpathQuery(String metadataKey, String xpath, String separator) {
    this(metadataKey, xpath);
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
