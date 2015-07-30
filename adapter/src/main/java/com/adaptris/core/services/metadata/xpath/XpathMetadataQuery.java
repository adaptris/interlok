package com.adaptris.core.services.metadata.xpath;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;

/**
 * Base interface for generating metadata from an xpath.
 * 
 * @author lchan
 * 
 */
public interface XpathMetadataQuery {
  /**
   * Get the key that this Xpath query will be associated with.
   * 
   */
  public String getMetadataKey();

  /**
   * Verify that everything associated with the query is as it should be.
   * 
   * @throws CoreException wrapping any underlying exception
   */
  public void verify() throws CoreException;

  /**
   * Create an Xpath from the {@link AdaptrisMessage} object.
   * 
   * @param msg the {@link AdaptrisMessage} that will be queried.
   * @return the xpath.
   */
  String createXpathQuery(AdaptrisMessage msg) throws Exception;
}
