/*
 * $RCSfile: DefaultServiceNameProvider.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/09/23 00:56:55 $
 * $Author: hfraser $
 */
package com.adaptris.core.services.dynamic;

import com.adaptris.core.CoreException;
import com.adaptris.core.TradingRelationship;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of {@link ServiceNameProvider} which returns the passed {@link TradingRelationship} source, destination and type
 * separated by an (optional) configurable character.
 * </p>
 * 
 * @config default-service-name-provider
 */
@XStreamAlias("default-service-name-provider")
public class DefaultServiceNameProvider extends ServiceNameProviderImp {

  private String separator;

  /**
   * <p>
   * Creates a new instance.  Default separator is "-".
   * </p>
   */
  public DefaultServiceNameProvider() {
    this.setSeparator("-");
  }

  @Override
  protected String retrieveName(TradingRelationship t) throws CoreException {
    if (t == null) {
      throw new IllegalArgumentException("null param");
    }

    StringBuffer result = new StringBuffer();
    result.append(t.getSource());
    result.append(this.getSeparator());
    result.append(t.getDestination());
    result.append(this.getSeparator());
    result.append(t.getType());

    return result.toString();
  }

  /**
   * <p>
   * Gets the separator used to delineate source, destination and type. May not
   * be null.
   * </p>
   *
   * @return the separator
   */
  public String getSeparator() {
    return separator;
  }

  /**
   * <p>
   * Sets the separator used to delineate source, destination and type. May not
   * be null.
   * </p>
   *
   * @param s separator, default is '-'
   */
  public void setSeparator(String s) {
    if (s == null) {
      throw new IllegalArgumentException("null param");
    }
    this.separator = s;
  }
}
