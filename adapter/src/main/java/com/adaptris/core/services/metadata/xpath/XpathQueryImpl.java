package com.adaptris.core.services.metadata.xpath;

import static org.apache.commons.lang.StringUtils.isEmpty;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for Metadata Xpath Queries.
 *
 * @author lchan
 *
 */
public abstract class XpathQueryImpl implements XpathMetadataQuery {

  protected transient Logger logR = LoggerFactory.getLogger(this.getClass());

  private Boolean allowEmptyResults;
  @NotBlank
  private String metadataKey;

  public XpathQueryImpl() {
  }


  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * Set the metadata key that will be associated with the resolved xpath expression.
   *
   * @param key the key.
   */
  public void setMetadataKey(String key) {
    if (isEmpty(key)) {
      throw new IllegalArgumentException("Configured Metadata Key may not be null.");
    }
    metadataKey = key;
  }

  public Boolean getAllowEmptyResults() {
    return allowEmptyResults;
  }

  /**
   * Specify whether or not an xpath that does not resolve should throw an exception.
   *
   *
   * @param b true to allow no results (which may result in an empty string being added as metadata), default false.
   */
  public void setAllowEmptyResults(Boolean b) {
    allowEmptyResults = b;
  }

  protected boolean allowEmptyResults() {
    return getAllowEmptyResults() != null ? getAllowEmptyResults().booleanValue() : false;
  }
}
