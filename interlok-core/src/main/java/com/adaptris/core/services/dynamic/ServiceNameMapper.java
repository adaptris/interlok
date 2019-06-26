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

package com.adaptris.core.services.dynamic;

import javax.validation.constraints.NotNull;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.StringUtils;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.Removal;
import com.adaptris.core.TradingRelationship;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Maps a name for a <code>Service</code> to a <code>TradingRelationship</code>. Used by
 * {@link ConfiguredServiceNameProvider}.
 * </p>
 * 
 * @config configured-service-name-mapper
 * @deprecated since 3.8.4 use {@link DynamicServiceExecutor} with a URL based
 *             {@link ServiceExtractor} instead.
 */
@Deprecated
@XStreamAlias("configured-service-name-mapper")
@DisplayOrder(order = {"serviceName", "tradingRelationship"})
@Removal(version = "3.11.0")
public class ServiceNameMapper {

  @NotNull
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

  @Override
  public boolean equals(Object other) {
    if (other == null)
      return false;
    if (other == this)
      return true;
    if (other instanceof ServiceNameMapper) {
      ServiceNameMapper item = (ServiceNameMapper) other;
      return new EqualsBuilder()
          .append(item.getTradingRelationship(), this.getTradingRelationship()).isEquals();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getTradingRelationship().hashCode();
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
    if (StringUtils.isBlank(serviceName)) {
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
    tradingRelationship = Args.notNull(t, "tradingRelationship");
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
