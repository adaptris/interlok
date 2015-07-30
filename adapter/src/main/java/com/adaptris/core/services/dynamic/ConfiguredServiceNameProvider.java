/*
 * $RCSfile: ConfiguredServiceNameProvider.java,v $
 * $Revision: 1.5 $
 * $Date: 2008/07/14 17:05:42 $
 * $Author: lchan $
 */
package com.adaptris.core.services.dynamic;

import java.util.HashSet;
import java.util.Set;

import com.adaptris.core.TradingRelationship;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link ServiceNameProvider} that has static mappings for trading relationships and service names
 * <p>
 * This is primiarily a basic implementation of <code>ServiceNameProvider</code> for testing. We do not recommend it for production
 * use as additional dynamic services will require you to restart the adapter every time you add one.
 * </p>
 * 
 * @config configured-service-name-provider
 */
@XStreamAlias("configured-service-name-provider")
public class ConfiguredServiceNameProvider extends ServiceNameProviderImp {

  private Set<ServiceNameMapper> serviceNameMappers;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public ConfiguredServiceNameProvider() {
    serviceNameMappers = new HashSet<ServiceNameMapper>();
  }

  public ConfiguredServiceNameProvider(Set<ServiceNameMapper> set) {
    this();
    setServiceNameMappers(set);
  }

  @Override
  protected String retrieveName(TradingRelationship t) {
    if (t == null) {
      throw new IllegalArgumentException("null param");
    }

    String result = null;
    for (ServiceNameMapper candidate : serviceNameMappers) {
      if (candidate.getTradingRelationship().equals(t)) {
        result = candidate.getServiceName();
        break;
      }
    }

    return result;
  }

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer(this.getClass().getName());
    result.append(" ");
    result.append(this.getServiceNameMappers());

    return result.toString();
  }

  // properties

  /**
   * <p>
   * Returns the <code>Set</code> of <code>ServiceNameMapper</code>s.
   * </p>
   *
   * @return the <code>Set</code> of <code>ServiceNameMapper</code>s
   */
  public Set<ServiceNameMapper> getServiceNameMappers() {
    return serviceNameMappers;
  }

  /**
   * <p>
   * Adds a <code>ServiceNameMapper</code> to the underlying store.
   * </p>
   *
   * @param s the <code>ServiceNameMapper</code> to add to the underlying store
   * @return true if <code>s</code> is added, otherwise false
   */
  public boolean addServiceNameMapper(ServiceNameMapper s) {
    return serviceNameMappers.add(s);
  }

  /**
   *
   * @param s the set of <code>ServiceNameMapper</code>s
   */
  public void setServiceNameMappers(Set<ServiceNameMapper> s) {
    serviceNameMappers = s;
  }
}
