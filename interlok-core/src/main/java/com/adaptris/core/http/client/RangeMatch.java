/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.http.client;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.http.client.net.BranchingHttpRequestService;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link StatusEvaluator} for use with {@link BranchingHttpRequestService}.
 * <p>
 * Matches a service id against range of values (inclusive) e.g. lower=200, upper=299 to match all "success" codes.
 * </p>
 * 
 * @config http-status-range-match
 *
 */
@XStreamAlias("http-status-range-match")
@DisplayOrder(order = {"serviceId", "lower", "upper"})
public class RangeMatch extends StatusEvaluatorImpl {

  private int lower;
  private int upper;

  public RangeMatch() {
    super();
  }

  public RangeMatch(int lower, int upper, String serviceId) {
    this();
    setLower(lower);
    setUpper(upper);
    setServiceId(serviceId);
  }

  @Override
  public boolean matches(int responseCode) {
    return lower <= responseCode && responseCode <= upper;
  }

  /**
   * @return the lowerBoundary
   */
  public int getLower() {
    return lower;
  }

  /**
   * @param lowerBoundary the lowerBoundary to set
   */
  public void setLower(int lowerBoundary) {
    this.lower = lowerBoundary;
  }

  /**
   * @return the upperBoundary
   */
  public int getUpper() {
    return upper;
  }

  /**
   * @param upperBoundary the upperBoundary to set
   */
  public void setUpper(int upperBoundary) {
    this.upper = upperBoundary;
  }


}
