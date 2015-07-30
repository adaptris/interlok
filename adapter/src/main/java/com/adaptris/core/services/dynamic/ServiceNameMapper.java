/*
 * $RCSfile: ServiceNameMapper.java,v $
 * $Revision: 1.3 $
 * $Date: 2008/02/20 18:10:56 $
 * $Author: lchan $
 */
package com.adaptris.core.services.dynamic;

import com.adaptris.core.TradingRelationship;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Maps a name for a <code>Service</code> to a <code>TradingRelationship</code>. Used by {@link ConfiguredServiceNameProvider}.
 * </p>
 * 
 * @config configured-service-name-mapper
 */
@XStreamAlias("configured-service-name-mapper")
public class ServiceNameMapper {

  private TradingRelationship tradingRelationship;
  private String serviceName;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public ServiceNameMapper() {
    // default...
    setTradingRelationship(new TradingRelationship());
  }

  /**
   * <p>
   * Creates a new instance.
   * </p>
   *
   * @param src the source identifier
   * @param dst the destination identifier
   * @param typ the message type identifier
   * @param name the name of the <code>Service</code> to use for this
   *          <code>ServiceId</code>
   */
  public ServiceNameMapper(String src, String dst, String typ, String name) {

    this();

    getTradingRelationship().setSource(src);
    getTradingRelationship().setDestination(dst);
    getTradingRelationship().setType(typ);
    setServiceName(name);
  }

  /**
   * <p>
   * <code>ServiceIdToNameMapper</code>s are semantically equal if there
   * underlying <code>TradingRelationship</code>s are equal.
   * </p>
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    return tradingRelationship.equals(((ServiceNameMapper) o).getTradingRelationship());
  }

  /** @see java.lang.Object#hashCode() */
  @Override
  public int hashCode() {
    return tradingRelationship.hashCode();
  }

  /**
   * <p>
   * If a Service name has been explicitly configured it is returned. If the
   * Service name is null or empty the default service name is returned. The
   * default service name is <code>source-destination-type</code>, where none of
   * source, destination or type may be null or empty.
   * </p>
   *
   * @return the name of the service to associated with the given
   *         <code>TradingRelationship</code>
   */
  public String getServiceName() {
    String result = null;
    if (serviceName == null || "".equals(serviceName)) {
      StringBuffer defaultName = new StringBuffer();
      defaultName.append(getTradingRelationship().getSource());
      defaultName.append("-");
      defaultName.append(getTradingRelationship().getDestination());
      defaultName.append("-");
      defaultName.append(getTradingRelationship().getType());

      result = defaultName.toString();
    }
    else {
      result = serviceName;
    }
    return result;
  }

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer(this.getClass().getName());
    result.append(" name [");
    result.append(getServiceName());
    result.append("[ trading relationship - ");
    result.append(getServiceName());

    return result.toString();
  }

  // getters & setters...

  /**
   * <p>
   * Returns the <code>TradingRelationship</code>.
   * </p>
   *
   * @return the <code>TradingRelationship</code>
   */
  public TradingRelationship getTradingRelationship() {
    return tradingRelationship;
  }

  /**
   * <p>
   * Sets the <code>TradingRelationship</code>. May not be null or empty.
   * </p>
   *
   * @param t the <code>TradingRelationship</code>
   */
  public void setTradingRelationship(TradingRelationship t) {
    if (t == null) {
      throw new IllegalArgumentException("null param");
    }
    tradingRelationship = t;
  }

  /**
   * <p>
   * Sets the name of the <code>Service</code>.
   * </p>
   *
   * @param s the name of the <code>Service</code>
   */
  public void setServiceName(String s) { // may be null or empty
    serviceName = s;
  }
}
