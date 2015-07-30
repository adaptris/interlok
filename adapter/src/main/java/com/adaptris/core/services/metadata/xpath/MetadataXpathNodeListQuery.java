package com.adaptris.core.services.metadata.xpath;

import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.adaptris.core.CoreException;
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
@XStreamAlias("metadata-xpath-nodelist-query")
public class MetadataXpathNodeListQuery extends MetadataXpathQueryImpl implements XpathObjectQuery {

  public MetadataXpathNodeListQuery() {
  }

  public MetadataXpathNodeListQuery(String metadataKey, String xpathMetadataKey) {
    this();
    setMetadataKey(metadataKey);
    setXpathMetadataKey(xpathMetadataKey);
  }

  @Override
  public NodeList resolveXpath(Document doc, NamespaceContext ctx, String expr) throws CoreException {
    return XpathQueryHelper.resolveNodeList(doc, ctx, expr, allowEmptyResults());
  }
}
