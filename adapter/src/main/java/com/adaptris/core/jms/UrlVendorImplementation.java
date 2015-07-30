package com.adaptris.core.jms;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.validator.constraints.NotBlank;


/**
 * <p>
 * Partial implementation with common or default behaviour.
 * </p>
 */
public abstract class UrlVendorImplementation extends VendorImplementationImp {

  @NotNull
  @NotBlank
  private String brokerUrl;

  public UrlVendorImplementation() {
  }



  /**
   * <p>
   * Returns the broker URL.
   * 
   * @return the broker URL
   */
  public String getBrokerUrl() {
    return brokerUrl;
  }

  /**
   * <p>
   * Sets the broker URL.
   * </p>
   * 
   * @param s the broker URL
   */
  public void setBrokerUrl(String s) {
    brokerUrl = s;
  }

  @Override
  public boolean connectionEquals(VendorImplementation vendorImp) {
    if (vendorImp instanceof UrlVendorImplementation) {
      return new EqualsBuilder().append(getBrokerUrl(), ((UrlVendorImplementation) vendorImp).getBrokerUrl()).isEquals();
    }
    return false;
  }

  @Override
  public String retrieveBrokerDetailsForLogging() {
    return getBrokerUrl();
  }

}
