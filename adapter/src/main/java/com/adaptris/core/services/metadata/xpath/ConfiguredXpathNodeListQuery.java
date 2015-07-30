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
