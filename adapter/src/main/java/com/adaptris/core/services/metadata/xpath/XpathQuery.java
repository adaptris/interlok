package com.adaptris.core.services.metadata.xpath;

import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Document;

import com.adaptris.core.MetadataElement;

/**
 * Interface for creating string metadata from an Xpath.
 * 
 * @author lchan
 * 
 */
public interface XpathQuery extends XpathObjectQuery {

  /**
   * <p>
   * Executes an Xpath query.
   * </p>
   *
   * @param doc The XML document
   * @param ctx any Namespace context
   * @return a {@link MetadataElement} with the configured key and the extracted text value
   */
  MetadataElement resolveXpath(Document doc, NamespaceContext ctx, String expression) throws Exception;

}
