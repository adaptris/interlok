/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.jms;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
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
  public boolean connectionEquals(VendorImplementationBase vendorImp) {
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
