package com.adaptris.core.services.metadata.xpath;

import static org.apache.commons.lang.StringUtils.isEmpty;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;

/**
 * Abstract base class for {@linkplain XpathQuery} implementations that are statically configured.
 *
 * @author lchan
 *
 */
public abstract class ConfiguredXpathQueryImpl extends XpathQueryImpl {

  @NotBlank
  private String xpathQuery;

  public ConfiguredXpathQueryImpl() {
  }

  public String getXpathQuery() {
    return xpathQuery;
  }

  /**
   * Set the xpath.
   *
   * @param expr
   */
  public void setXpathQuery(String expr) {
    if (isEmpty(expr)) {
      throw new IllegalArgumentException("Configured Xpath Query may not be null.");
    }
    xpathQuery = expr;
  }

  @Override
  public String createXpathQuery(AdaptrisMessage msg) {
    return xpathQuery;
  }

  public void verify() throws CoreException {
    if (isEmpty(getXpathQuery())) {
      throw new CoreException("Configured Xpath is null.");
    }
  }
}
