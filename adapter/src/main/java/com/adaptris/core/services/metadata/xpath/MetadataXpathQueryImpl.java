package com.adaptris.core.services.metadata.xpath;

import static org.apache.commons.lang.StringUtils.isEmpty;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;

/**
 * Abstract base class for {@linkplain XpathQuery} implementations that derive their xpath query from metadata.
 *
 * @author lchan
 *
 */
public abstract class MetadataXpathQueryImpl extends XpathQueryImpl {

  @NotBlank
  private String xpathMetadataKey;

  public MetadataXpathQueryImpl() {
  }

  public String getXpathMetadataKey() {
    return xpathMetadataKey;
  }

  /**
   * Set the xpath.
   *
   * @param expr
   */
  public void setXpathMetadataKey(String expr) {
    if (isEmpty(expr)) {
      throw new IllegalArgumentException("Configured Xpath Metadata may not be null.");
    }
    xpathMetadataKey = expr;
  }

  @Override
  public String createXpathQuery(AdaptrisMessage msg) throws CoreException {
    String xpath = msg.getMetadataValue(getXpathMetadataKey());
    if (isEmpty(xpath)) {
      throw new CoreException(getXpathMetadataKey() + " does not exist as metadata or is null");
    }
    return xpath;
  }

  public void verify() throws CoreException {
    if (isEmpty(getXpathMetadataKey())) {
      throw new CoreException("Configured Xpath Metadata is null.");
    }
  }
}
